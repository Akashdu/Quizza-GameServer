package me.srikavin.quiz.network.common.message.matchmaker

import me.srikavin.quiz.network.common.message.MATCHMAKER_STOP_PACKET_ID
import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.message.MessageSerializer
import java.nio.ByteBuffer

class MatchmakingStopMessage : MessageBase(MATCHMAKER_STOP_PACKET_ID)

class MatchmakingStopMessageSerializer : MessageSerializer<MatchmakingStopMessage> {
    override fun toBytes(t: MatchmakingStopMessage): ByteBuffer {
        return ByteBuffer.allocate(0)
    }

    override fun fromBytes(buffer: ByteBuffer): MatchmakingStopMessage {
        return MatchmakingStopMessage()
    }

}