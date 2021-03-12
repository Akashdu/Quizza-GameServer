package me.srikavin.quiz.network.common.model.data

import me.srikavin.quiz.network.common.getString
import me.srikavin.quiz.network.common.put
import java.nio.ByteBuffer


interface QuizAnswerModel {
    val id: ResourceId
    val contents: String
    val isCorrect: Boolean
}

data class NetworkQuizAnswer(
        override val id: ResourceId,
        override val contents: String,
        override val isCorrect: Boolean
) : QuizAnswerModel

fun QuizAnswerModel.countBytes(): Int {
    val contentsArray = this.contents.toByteArray(Charsets.UTF_8)
    return id.countBytes() +  // UUID representing question
            1 + // Bit for if the value is correct
            4 +  // Int for size of description
            contentsArray.size // Size of description

}

fun QuizAnswerModel.serialize(buffer: ByteBuffer) {
    buffer.put(this.id)
    buffer.put(if (this.isCorrect) 1.toByte() else 0)
    buffer.put(this.contents)
}

fun deserializeQuizAnswer(buffer: ByteBuffer): QuizAnswerModel {
    val id = buffer.getResourceId()
    val isCorrect = buffer.get() == 1.toByte()
    val contents = buffer.getString()

    return NetworkQuizAnswer(id, contents, isCorrect)
}
