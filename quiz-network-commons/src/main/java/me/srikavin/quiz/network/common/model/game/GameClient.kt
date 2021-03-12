package me.srikavin.quiz.network.common.model.game

import me.srikavin.quiz.network.common.message.MessageBase
import java.util.*


interface BackingClient {
    val id: UUID
    fun kick()
    fun send(message: MessageBase)
    fun isConnected(): Boolean
}

open class GameClient(var backing: BackingClient) {
    override fun equals(other: Any?): Boolean {
        if (other !is GameClient) {
            return false
        }
        return backing.id == other.backing.id
    }

    override fun hashCode(): Int {
        return backing.hashCode()
    }
}