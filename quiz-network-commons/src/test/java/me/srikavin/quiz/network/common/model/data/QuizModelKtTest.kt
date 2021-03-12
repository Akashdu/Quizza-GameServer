package me.srikavin.quiz.network.common.model.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

internal class QuizModelKtTest {
    @Test
    fun serializeQuizEmpty() {
        val questions = listOf<QuizQuestionModel>()
        val quiz = NetworkQuiz(ResourceId("quiz1"), "testing quiz serialization", questions, "QuizModel description")

        val serialized = ByteBuffer.allocate(quiz.countBytes())

        quiz.serialize(serialized)

        assertEquals(quiz.countBytes(), serialized.position())

        serialized.rewind()

        val deserialized = deserializeQuiz(serialized)

        assertEquals(quiz, deserialized)
    }

    @Test
    fun serializeQuiz() {
        val answers = listOf(
                NetworkQuizAnswer(ResourceId("a1"), "Answer A", true),
                NetworkQuizAnswer(ResourceId("a2"), "Answer B", true),
                NetworkQuizAnswer(ResourceId("a3"), "Answer C", false),
                NetworkQuizAnswer(ResourceId("a4"), "Answer D", false)
        )
        val questions = listOf(NetworkQuizQuestion(ResourceId("q1"), answers, "Sample Question"))
        val quiz = NetworkQuiz(ResourceId("quiz1"), "testing quiz serialization", questions, "QuizModel description")

        val serialized = ByteBuffer.allocate(quiz.countBytes())

        quiz.serialize(serialized)

        assertEquals(quiz.countBytes(), serialized.position())

        serialized.rewind()

        val deserialized = deserializeQuiz(serialized)

        assertEquals(quiz, deserialized)
    }
}