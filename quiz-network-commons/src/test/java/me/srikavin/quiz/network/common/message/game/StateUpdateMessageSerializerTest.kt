package me.srikavin.quiz.network.common.message.game

import me.srikavin.quiz.network.common.model.data.NetworkQuiz
import me.srikavin.quiz.network.common.model.data.NetworkQuizAnswer
import me.srikavin.quiz.network.common.model.data.NetworkQuizQuestion
import me.srikavin.quiz.network.common.model.data.ResourceId
import me.srikavin.quiz.network.common.model.game.GamePlayer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.threeten.bp.Instant
import java.util.*

internal class StateUpdateMessageSerializerTest {
    @Test
    fun messageSerialization() {
        val serializer = StateUpdateMessageSerializer()

        val answers = listOf(
                NetworkQuizAnswer(ResourceId("a1"), "Answer A", true),
                NetworkQuizAnswer(ResourceId("a2"), "Answer B", true),
                NetworkQuizAnswer(ResourceId("a3"), "Answer C", false),
                NetworkQuizAnswer(ResourceId("a4"), "Answer D", false)
        )
        val questions = listOf(NetworkQuizQuestion(ResourceId("q1"), answers, "Sample Question"))
        val quiz = NetworkQuiz(ResourceId("quiz1"), "testing quiz serialization", questions, "QuizModel description")

        val player = GamePlayer(UUID.randomUUID(), "testing player", 1234, "https://some.website.com")

        val message = StateUpdateMessage(GameState(quiz, Instant.now(), listOf(player), 1))

        val serialized = serializer.toBytes(message)

        serialized.flip()

        val unserialized = serializer.fromBytes(serialized)

        assertEquals(message, unserialized)
    }
}