package me.srikavin.quiz.network.client

import kotlinx.coroutines.*
import me.srikavin.quiz.network.common.MessageRouter
import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.model.data.UserID
import me.srikavin.quiz.network.common.model.game.BackingClient
import me.srikavin.quiz.network.common.model.game.GameClient
import me.srikavin.quiz.network.common.model.network.RejoinToken
import me.srikavin.quiz.network.common.put
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


private class InternalBackingClient(override val id: UUID) : BackingClient {
    override fun kick() {
    }

    override fun send(message: MessageBase) {
    }

    override fun isConnected(): Boolean {
        return true
    }

}

private val DefaultExceptionHandler = CoroutineExceptionHandler { _, exception ->
    exception.printStackTrace()
    throw exception
}

private class InternalGameClient(id: UUID) : GameClient(InternalBackingClient(id))

class NetworkClient(
        private val remote: InetAddress,
        private val packetRouter: MessageRouter,
        private val bufferSize: Int = 4096,
        private val exceptionHandler: CoroutineExceptionHandler = DefaultExceptionHandler
) {
    private lateinit var socket: Socket
    lateinit var networkScope: CoroutineScope
    lateinit var input: BufferedInputStream
    lateinit var output: BufferedOutputStream
    private lateinit var gameClient: InternalGameClient
    private var shutdown = false

    private var retryCount = 0

    var rejoinToken: RejoinToken = RejoinToken(UUID.randomUUID())
    var userId: UserID = UserID(UUID.randomUUID())

    var connected: Boolean = false

    private val queue = ConcurrentLinkedQueue<MessageBase>()

    fun start(networkScope: CoroutineScope, rejoinToken: RejoinToken? = null) {
        start(networkScope, rejoinToken?.token)
    }

    fun sendMessage(message: MessageBase) {
        queue.add(message)
    }

    fun shutdown() {
        this.networkScope.coroutineContext.cancel()
        this.queue.clear()
        this.shutdown = false
    }

    fun start(networkScope: CoroutineScope, rejoinToken: UUID? = null) {
        this.networkScope = networkScope

        //Initialize connection
        networkScope.launch(exceptionHandler) {
            connect(rejoinToken)
        }
    }

    private fun connect(rejoinToken: UUID? = null) {
        socket = Socket(remote, 1200)
        socket.soTimeout = 15000
        input = socket.getInputStream().buffered()
        output = socket.getOutputStream().buffered()

        val welcome = ByteBuffer.allocate(20)

        //Length
        welcome.putInt(16)

        if (rejoinToken != null) {
            welcome.put(rejoinToken)
        }

        output.write(welcome.array())
        output.flush()

        val response = ByteArray(32)
        input.read(response)
        val responseBuffer = ByteBuffer.wrap(response)

        this@NetworkClient.rejoinToken = RejoinToken(responseBuffer)
        this@NetworkClient.userId = UserID(responseBuffer)

        gameClient = InternalGameClient(this@NetworkClient.userId.id)

        println(this@NetworkClient)
        println(this@NetworkClient.rejoinToken)
        println(this@NetworkClient.userId)

        connected = true
        retryCount = 0

        this.networkScope.launch(exceptionHandler) {
            messageHandler()
        }
    }

    private suspend fun messageHandler() {
        var buffer: ByteBuffer = ByteBuffer.allocate(0)
        val lengthWriteBuffer: ByteBuffer = ByteBuffer.allocate(4)
        var inProgress = -1
        var total = 0
        try {
            while (!shutdown) {
                if (connected) {
                    while (queue.isNotEmpty()) {
                        val base = queue.poll()
                        println("Sending $base")
                        val serialized = packetRouter.serializeMessage(base)
                        val serializedArray = serialized.array()

                        lengthWriteBuffer.putInt(serializedArray.size - serialized.arrayOffset() + 1)
                        output.write(lengthWriteBuffer.array())
                        lengthWriteBuffer.flip()

                        //Endianness of the identifier does not matter, as it is a single byte
                        val id = ByteArray(1)
                        id[0] = base.identifier.value
                        output.write(id)

                        //Send the serialized packet
                        output.write(serializedArray, serialized.arrayOffset(), serialized.position())

                        output.flush()
                    }
                    if (inProgress == total) {
                        buffer.flip()
                        //Hand-off to packet router
                        packetRouter.handlePacket(gameClient, buffer)
                        inProgress = -1
                        total = 0
                    }


                    //Read the available bytes in chunks, allowing simultaneous sending of packets on a single thread
                    if (input.available() > 0) {
                        if (inProgress != -1) {
                            if (inProgress < total) {
                                val readAmount = if (total - inProgress < bufferSize) total - inProgress else bufferSize
                                val buf = ByteArray(readAmount)
                                val bytesRead = input.read(buf)
                                inProgress += bytesRead
                                buffer.put(buf, 0, bytesRead)
                                println("inProgress: $inProgress")
                            }
                        } else {
                            val length = ByteArray(4)
                            input.read(length)
                            total = ByteBuffer.wrap(length).int
                            buffer = ByteBuffer.allocate(total)
                            inProgress = 0
                            println("total: $total")
                        }
                    }
                }
                delay(200)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            connect(rejoinToken.token)
            retryCount++
            if (retryCount > 3) {
                throw e
            }

        } finally {
            this.connected = false
            this.socket.close()
        }
    }
}