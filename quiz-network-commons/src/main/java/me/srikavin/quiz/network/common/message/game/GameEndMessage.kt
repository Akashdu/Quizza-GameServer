package me.srikavin.quiz.network.common.message.game

import me.srikavin.quiz.network.common.message.GAME_END_PACKET_ID
import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.message.MessageSerializer
import java.nio.ByteBuffer


class GameEndMessage : MessageBase(GAME_END_PACKET_ID)

class GameEndMessageSerializer : MessageSerializer<GameEndMessage> {
    override fun toBytes(t: GameEndMessage): ByteBuffer {
        return ByteBuffer.allocate(0)
    }

    override fun fromBytes(buffer: ByteBuffer): GameEndMessage {
        return GameEndMessage()
    }
}