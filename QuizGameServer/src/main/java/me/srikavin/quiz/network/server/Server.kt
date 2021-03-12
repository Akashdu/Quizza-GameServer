package me.srikavin.quiz.network.server


import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.srikavin.quiz.network.common.MessageRouter
import me.srikavin.quiz.network.common.model.game.GameClient
import me.srikavin.quiz.network.common.model.network.RejoinToken
import me.srikavin.quiz.network.common.put
import me.srikavin.quiz.network.server.game.MatchmakerGameListener
import me.srikavin.quiz.network.server.game.NetworkMatchmaker
import me.srikavin.quiz.network.server.model.DBQuiz
import me.srikavin.quiz.network.server.model.QuizRepository
import mu.KotlinLogging
import org.litote.kmongo.getCollection
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList


private data class TemporaryClient(val socket: Socket, val kickTime: Instant) {
    val inputStream: BufferedInputStream = socket.getInputStream().buffered()
    val outputStream: BufferedOutputStream = socket.getOutputStream().buffered()

    fun toNetworkClient(rejoinToken: RejoinToken, uuid: UUID = UUID.randomUUID()): NetworkClient {
        return NetworkClient(uuid, socket, inputStream, outputStream)
    }
}

const val MAX_PACKET_SIZE = 1024 * 10 // 10 MB

class Server(private val socket: ServerSocket) {
    private val logger = KotlinLogging.logger {}
    private val gameClientMap = ConcurrentHashMap<UUID, GameClient>()
    private val clients = ArrayList<NetworkClient>()
    private val newClients = ArrayList<NetworkClient>()
    private val newClientsMutex = Mutex()
    private val temporaryClientsMutex = Mutex()
    private val temporaryClients = ArrayList<TemporaryClient>()
    private val clientsToRemoveMutex = Mutex()
    private val clientsToRemove = ArrayList<TemporaryClient>()
    private val messageRouter = MessageRouter()
    private val authService = AuthService()
    private lateinit var matchmakerGameListener: MatchmakerGameListener
    private lateinit var quizRepository: QuizRepository

    fun start(database: MongoDatabase) {
        logger.info { "Initializing Database Connection" }
        val col = database.getCollection<DBQuiz>("quizzes")
        quizRepository = QuizRepository(col)

        logger.info { "Initializing message router" }
        matchmakerGameListener = MatchmakerGameListener(NetworkMatchmaker(quizRepository), messageRouter)

        logger.info { "Initializing server" }
        GlobalScope.launch {
            logger.info { "Initializing connection coroutine" }
            handleConnections()
        }

        GlobalScope.launch {
            logger.info { "Initializing message handling routine" }
            while (true) {
                processMessages()
                delay(50)
            }
        }

        runBlocking {
            logger.info { "Initializing new client connection routine" }
            processNewClients()
        }

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun processMessages() {
        newClientsMutex.withLock {
            clients.addAll(newClients)
            newClients.forEach { newClient ->
                if (gameClientMap.contains(newClient.id)) {
                    gameClientMap[newClient.id]!!.backing = newClient
                } else {
                    gameClientMap[newClient.id] = GameClient(newClient)
                }
            }
            newClients.clear()
        }

        val toRemove = ArrayList<NetworkClient>()

        gameClientMap.forEach { (_, gameClient) ->
            if (!gameClient.backing.isConnected()) {
                return@forEach
            }

            val client = gameClient.backing as NetworkClient
            try {
                val reader = client.reader

                if (client.isBusy.get()) {
                    return@forEach
                }

                if (client.shouldKick.get()) {
                    toRemove.add(client)
                    return@forEach
                }

                // If the message queue is not empty, process the messages asynchronously to avoid blocking the event loop
                if (client.messageQueue.isNotEmpty()) {
                    GlobalScope.launch {
                        while (client.messageQueue.isNotEmpty()) {
                            client.isBusy.set(true)
                            val message = client.messageQueue.poll()
                            if (message != null) {
                                val serialized = messageRouter.serializeMessage(message)
                                val serializedArray = serialized.array()
                                val length = ByteBuffer.allocate(4)
                                        .putInt(serializedArray.size - serialized.arrayOffset() + 1)
                                client.writer.write(length.array())
                                client.writer.write(message.identifier.value.toInt())
                                client.writer.write(serialized.array(), serialized.arrayOffset(), serialized.position())
                                client.writer.flush()
                            }
                        }
                        client.isBusy.set(false)
                    }
                    return@forEach
                }
                if (reader.available() == 0) {
                    return@forEach
                }

                // Start a new transmission
                if (client.inProgress == -1) {
                    val lengthArray = ByteArray(4)

                    if (reader.read(lengthArray) == 4) {
                        client.total = lengthArray.wrap().int
                        client.inProgress = 0
                    } else {
                        logger.warn { "Client kicked for invalid packet size: $client" }
                        throw IOException("Invalid packet size; expecting size 4")
                    }

                    if (client.total > MAX_PACKET_SIZE) {
                        logger.warn { "Client kicked for large packet size: $client" }
                        throw IOException("Packet size is too large ${client.total}")
                    }
                }

                // Continue to read non-blocking
                if (client.inProgress != client.total) {
                    val message = ByteArray(1)
                    while (reader.available() != 0 && client.inProgress != client.total) {
                        client.reader.read(message)
                        client.buffer.writeBytes(message)
                        client.inProgress++
                    }
                }

                // Once a message is done, process it
                if (client.inProgress == client.total) {
                    logger.info { "Received packet from client ($gameClient - ${gameClient.backing.id})" }
                    messageRouter.handlePacket(gameClient, client.buffer.toByteArray().wrap())

                    client.inProgress = -1
                    client.total = 0
                    client.buffer.reset()
                    return@forEach
                }

            } catch (t: Throwable) {
                //Catch all exceptions to prevent termination of the message processing routine
                logger.error(t) { "Exception occurred in processing thread" }
                toRemove.add(client)
                try {
                    client.socket.close()
                } catch (ex: Exception) {
                    logger.error(t) { "Exception occurred when removing client" }
                }
                return@forEach
            }
        }

        clients.removeAll(toRemove)
        toRemove.forEach { client -> client.isConnected.set(false) }
        toRemove.clear()
    }

    private suspend fun handleConnections() {
        while (true) {
            @Suppress("BlockingMethodInNonBlockingContext")
            val accept = socket.accept()
            handleClientConnect(accept)
        }
    }

    private suspend fun handleClientConnect(socket: Socket) {
//        Set a timeout of 20 seconds for reads on the socket
        socket.soTimeout = 20000

//        Give clients 15 seconds to send a connect packet before getting kicked
        val kickTime = Instant.now().plusSeconds(15)
        val newClient = TemporaryClient(socket, kickTime)


        temporaryClientsMutex.withLock {
            temporaryClients.add(newClient)
        }

    }

    private suspend fun removeClient(client: TemporaryClient) {
        clientsToRemoveMutex.withLock {
            clientsToRemove.add(client)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun processNewClients() {
        while (true) {
            if (temporaryClientsMutex.tryLock("Process New Clients")) {
                clientsToRemoveMutex.withLock {
                    clientsToRemove.forEach { it.socket.close() }
                    temporaryClients.removeAll(clientsToRemove)
                }

                val iterator = temporaryClients.iterator()
                for (client in iterator) {
                    iterator.remove()

                    GlobalScope.launch {
                        try {
                            val input = client.inputStream

                            val lengthBuffer = input.readNBytes(4).wrap()
                            val length = lengthBuffer.int

                            //Verify message received is connect packet
                            if (length != 16) {
                                logger.warn { "Connecting client sent wrong data length: $length" }
                                removeClient(client)
                                return@launch
                            }

                            val bufferRaw = input.readNBytes(length)

                            //Verify amount of data read is valid
                            if (bufferRaw.size != length) {
                                logger.warn { "Client sent wrong data length: $length != ${bufferRaw.size}" }
                                removeClient(client)
                                return@launch
                            }

                            val buffer = bufferRaw.wrap()
                            val token = authService.getRejoinToken(buffer)

                            val user = authService.getUUID(token)

                            val networkClient = client.toNetworkClient(user.first, user.second)

                            newClientsMutex.withLock {
                                logger.info { "New client joined: $networkClient" }
                                newClients.add(networkClient)
                            }

                            val response = ByteBuffer.allocate(32)
                            response.put(user.first.token)
                            response.put(user.second)

                            client.outputStream.write(response.array())
                            client.outputStream.flush()


                        } catch (e: IOException) {
                            e.printStackTrace()
                            removeClient(client)
                        }
                    }
                }
                temporaryClientsMutex.unlock("Process New Clients")
            }
            delay(50)
        }
    }
}


private fun ByteArray.wrap(): ByteBuffer {
    return ByteBuffer.wrap(this)
}
