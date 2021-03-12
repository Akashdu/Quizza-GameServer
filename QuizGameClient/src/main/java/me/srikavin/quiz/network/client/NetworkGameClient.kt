package me.srikavin.quiz.network.client

import me.srikavin.quiz.network.common.MessageHandler
import me.srikavin.quiz.network.common.MessageRouter
import me.srikavin.quiz.network.common.message.MATCHMAKER_STATE_UPDATE_PACKET_ID
import me.srikavin.quiz.network.common.message.STATE_UPDATE_PACKET_ID
import me.srikavin.quiz.network.common.message.game.AnswerQuestionMessage
import me.srikavin.quiz.network.common.message.game.GameState
import me.srikavin.quiz.network.common.message.game.StateUpdateMessage
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakerStateUpdateMessage
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakingStartMessage
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakingStopMessage
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.QuizModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import me.srikavin.quiz.network.common.model.game.GameClient
import me.srikavin.quiz.network.common.model.matchmaker.MatchmakingState

class NetworkGameClient(val client: NetworkClient, router: MessageRouter) {
    var onMatchmakingStateUpdate: (MatchmakingState) -> Unit = {}
    var onGameStateUpdate: (GameState) -> Unit = {}

    init {
        router.registerHandler(MATCHMAKER_STATE_UPDATE_PACKET_ID, object : MessageHandler<MatchmakerStateUpdateMessage> {
            override fun handle(client: GameClient, message: MatchmakerStateUpdateMessage) {
                onMatchmakingStateUpdate(message.state)
            }
        })

        router.registerHandler(STATE_UPDATE_PACKET_ID, object : MessageHandler<StateUpdateMessage> {
            override fun handle(client: GameClient, message: StateUpdateMessage) {
                onGameStateUpdate(message.state)
            }
        })

    }

    fun startMatchmaking(quiz: ResourceId) {
        client.sendMessage(MatchmakingStartMessage(quiz))
    }

    fun startMatchmaking(quiz: QuizModel) {
        startMatchmaking(quiz.id)
    }

    fun stopMatchmaking() {
        client.sendMessage(MatchmakingStopMessage())
    }

    fun sendAnswer(quizAnswer: ResourceId) {
        client.sendMessage(AnswerQuestionMessage(quizAnswer))
    }

    fun sendAnswer(quizAnswer: QuizAnswerModel) {
        sendAnswer(quizAnswer.id)
    }

    fun shutdown(){
        client.shutdown()
    }
}