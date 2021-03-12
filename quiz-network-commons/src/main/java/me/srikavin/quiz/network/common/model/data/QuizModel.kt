package me.srikavin.quiz.network.common.model.data

import me.srikavin.quiz.network.common.getString
import me.srikavin.quiz.network.common.put
import java.nio.ByteBuffer
import java.util.*

interface QuizModel {
    val id: ResourceId
    val title: String
    val questions: List<QuizQuestionModel>
    val description: String
    fun countBytes(): Int {
        val contentsArray = this.description.toByteArray(Charsets.UTF_8)
        val titleArray: ByteArray = this.title.toByteArray(Charsets.UTF_8)
        var questionLength = 0
        for (e in this.questions) {
            questionLength += e.countBytes()
        }

        return id.countBytes() +
                4 + // Int for size of title
                titleArray.size + // The title string
                4 + // Int for size of description
                contentsArray.size + // The description
                4 + // The number of questions
                questionLength // The questions
    }

    fun serialize(buffer: ByteBuffer) {
        buffer.put(this.id)
        buffer.put(this.description)
        buffer.put(this.title)
        buffer.putInt(this.questions.size)

        for (e: QuizQuestionModel in this.questions) {
            e.serialize(buffer)
        }
    }
}

data class NetworkQuiz(
        override val id: ResourceId,
        override val title: String,
        override val questions: List<QuizQuestionModel>,
        override val description: String
) : QuizModel


fun deserializeQuiz(buffer: ByteBuffer): QuizModel {
    val id = buffer.getResourceId()
    val contents = buffer.getString()
    val title = buffer.getString()

    val questionsNumber = buffer.int

    val questions = ArrayList<QuizQuestionModel>(questionsNumber)

    repeat(questionsNumber) {
        questions.add(deserializeQuizQuestion(buffer))
    }

    return NetworkQuiz(id, title, questions, contents)
}
