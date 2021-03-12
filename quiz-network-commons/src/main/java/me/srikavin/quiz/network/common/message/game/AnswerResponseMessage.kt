package me.srikavin.quiz.network.common.message.game

import me.srikavin.quiz.network.common.message.ANSWER_RESPONSE_PACKET_ID
import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.message.MessageSerializer
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.countBytes
import me.srikavin.quiz.network.common.model.data.deserializeQuizAnswer
import me.srikavin.quiz.network.common.model.data.serialize
import me.srikavin.quiz.network.common.model.game.GamePlayer
import me.srikavin.quiz.network.common.model.game.countBytes
import me.srikavin.quiz.network.common.model.game.deserializeGamePlayer
import me.srikavin.quiz.network.common.model.game.serialize
import java.nio.ByteBuffer


data class AnswerResponseMessage(val player: GamePlayer, val quizAnswerModel: QuizAnswerModel) :
    MessageBase(ANSWER_RESPONSE_PACKET_ID)

class AnswerResponseSerializer : MessageSerializer<AnswerResponseMessage> {
    override fun toBytes(t: AnswerResponseMessage): ByteBuffer {
        val buffer = ByteBuffer.allocate(t.player.countBytes() + t.quizAnswerModel.countBytes())
        t.player.serialize(buffer)
        t.quizAnswerModel.serialize(buffer)
        return buffer
    }

    override fun fromBytes(buffer: ByteBuffer): AnswerResponseMessage {
        return AnswerResponseMessage(deserializeGamePlayer(buffer), deserializeQuizAnswer(buffer))
    }
}