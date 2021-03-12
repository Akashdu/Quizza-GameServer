package me.srikavin.quiz.network.server

import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.model.game.BackingClient
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

data class NetworkClient(
    override val id: UUID,
    val socket: Socket,
    val reader: BufferedInputStream,
    val writer: BufferedOutputStream,
    val buffer: ByteArrayOutputStream = ByteArrayOutputStream(),
    var inProgress: Int = -1,
    var total: Int = -1
) : BackingClient {
    internal val messageQueue: Queue<MessageBase> = ConcurrentLinkedQueue()
    internal val shouldKick = AtomicBoolean(false)
    internal var isBusy = AtomicBoolean(false)
    internal var isConnected = AtomicBoolean(true)

    override fun kick() {
        shouldKick.set(true)
    }

    override fun send(message: MessageBase) {
        messageQueue.offer(message)
    }

    override fun isConnected(): Boolean {
        return isConnected.get()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkClient

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
