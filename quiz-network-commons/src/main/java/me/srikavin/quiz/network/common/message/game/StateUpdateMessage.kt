package me.srikavin.quiz.network.common.message.game

import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.message.MessageSerializer
import me.srikavin.quiz.network.common.message.STATE_UPDATE_PACKET_ID
import me.srikavin.quiz.network.common.model.data.QuizModel
import me.srikavin.quiz.network.common.model.data.deserializeQuiz
import me.srikavin.quiz.network.common.model.game.GamePlayer
import me.srikavin.quiz.network.common.model.game.countBytes
import me.srikavin.quiz.network.common.model.game.deserializeGamePlayer
import me.srikavin.quiz.network.common.model.game.serialize
import org.threeten.bp.Instant
import java.nio.ByteBuffer

data class GameState(
        val quiz: QuizModel,
        val timeLeft: Instant,
        val players: List<GamePlayer>,
        val currentQuestion: Int
)

data class StateUpdateMessage(val state: GameState) : MessageBase(STATE_UPDATE_PACKET_ID)

class StateUpdateMessageSerializer : MessageSerializer<StateUpdateMessage> {
    override fun toBytes(t: StateUpdateMessage): ByteBuffer {
        var playerLengths = 0

        for (player in t.state.players) {
            playerLengths += player.countBytes()
        }
        val length = 8 +               // Long for epoch
                t.state.quiz.countBytes() +       // QuizModel
                4 +                        // Current question
                4 +                        // Number of players
                playerLengths

        val buffer = ByteBuffer.allocate(length)

        buffer.putLong(t.state.timeLeft.toEpochMilli())
        t.state.quiz.serialize(buffer)
        buffer.putInt(t.state.currentQuestion)
        buffer.putInt(t.state.players.size)

        for (player in t.state.players) {
            player.serialize(buffer)
        }
        return buffer
    }

    override fun fromBytes(buffer: ByteBuffer): StateUpdateMessage {
        val timeEpoch = buffer.long
        val timeLeft = Instant.ofEpochMilli(timeEpoch)

        val quiz = deserializeQuiz(buffer)
        val currentQuestion = buffer.int
        val playerSize = buffer.int
        val players = ArrayList<GamePlayer>(playerSize)

        repeat(playerSize) {
            players.add(deserializeGamePlayer(buffer))
        }

        return StateUpdateMessage(GameState(quiz, timeLeft, players, currentQuestion))
    }
}