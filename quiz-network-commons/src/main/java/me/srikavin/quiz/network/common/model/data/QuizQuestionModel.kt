package me.srikavin.quiz.network.common.model.data

import me.srikavin.quiz.network.common.getString
import me.srikavin.quiz.network.common.put
import java.nio.ByteBuffer
import java.util.*

interface QuizQuestionModel {
    val id: ResourceId
    val answers: List<QuizAnswerModel>
    val contents: String
}

data class NetworkQuizQuestion(
        override val id: ResourceId,
        override val answers: List<QuizAnswerModel>,
        override val contents: String
) : QuizQuestionModel

fun QuizQuestionModel.countBytes(): Int {
    val contentsArray = this.contents.toByteArray(Charsets.UTF_8)
    var answerLength = 0
    for (e in this.answers) {
        answerLength += e.countBytes()
    }

    return id.countBytes() + // UUID
            4 + // Int for size of description
            contentsArray.size + // The length of description
            4 + // The number of answers
            answerLength // The answers
}

fun QuizQuestionModel.serialize(buffer: ByteBuffer) {
    buffer.put(this.id)
    buffer.put(this.contents)
    buffer.putInt(this.answers.size)
    for (e in this.answers) {
        e.serialize(buffer)
    }
}

fun deserializeQuizQuestion(buffer: ByteBuffer): QuizQuestionModel {
    val id = buffer.getResourceId()
    val contents = buffer.getString()
    val answersSize = buffer.int

    val answers = ArrayList<QuizAnswerModel>(answersSize)

    repeat(answersSize) {
        answers.add(deserializeQuizAnswer(buffer))
    }
    return NetworkQuizQuestion(id, answers, contents)
}
