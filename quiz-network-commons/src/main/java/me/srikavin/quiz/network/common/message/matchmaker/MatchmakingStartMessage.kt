package me.srikavin.quiz.network.common.message.matchmaker

import me.srikavin.quiz.network.common.message.MATCHMAKER_START_PACKET_ID
import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.message.MessageSerializer
import me.srikavin.quiz.network.common.model.data.ResourceId
import me.srikavin.quiz.network.common.model.data.countBytes
import me.srikavin.quiz.network.common.model.data.getResourceId
import me.srikavin.quiz.network.common.model.data.put
import java.nio.ByteBuffer

class MatchmakingStartMessage(val quizId: ResourceId) : MessageBase(MATCHMAKER_START_PACKET_ID)

class MatchmakingStartMessageSerializer : MessageSerializer<MatchmakingStartMessage> {
    override fun toBytes(t: MatchmakingStartMessage): ByteBuffer {
        val buffer = ByteBuffer.allocate(t.quizId.countBytes())
        buffer.put(t.quizId)
        return buffer
    }

    override fun fromBytes(buffer: ByteBuffer): MatchmakingStartMessage {
        val resourceId = buffer.getResourceId()
        return MatchmakingStartMessage(resourceId)
    }

}