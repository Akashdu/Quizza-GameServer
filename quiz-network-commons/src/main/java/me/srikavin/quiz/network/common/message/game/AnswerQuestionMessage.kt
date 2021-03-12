package me.srikavin.quiz.network.common.message.game

import me.srikavin.quiz.network.common.message.ANSWER_QUESTION_PACKET_ID
import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.message.MessageSerializer
import me.srikavin.quiz.network.common.model.data.ResourceId
import me.srikavin.quiz.network.common.model.data.countBytes
import me.srikavin.quiz.network.common.model.data.getResourceId
import me.srikavin.quiz.network.common.model.data.put
import java.nio.ByteBuffer

data class AnswerQuestionMessage(val answerId: ResourceId) : MessageBase(ANSWER_QUESTION_PACKET_ID)

class AnswerQuestionSerializer : MessageSerializer<AnswerQuestionMessage> {
    override fun toBytes(t: AnswerQuestionMessage): ByteBuffer {
        val buffer = ByteBuffer.allocate(t.answerId.countBytes())
        buffer.put(t.answerId)
        return buffer
    }

    override fun fromBytes(buffer: ByteBuffer): AnswerQuestionMessage {
        return AnswerQuestionMessage(buffer.getResourceId())
    }
}