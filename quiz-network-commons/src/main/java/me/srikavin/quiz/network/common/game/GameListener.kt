package me.srikavin.quiz.network.common.game

import me.srikavin.quiz.network.common.MessageHandler
import me.srikavin.quiz.network.common.MessageRouter
import me.srikavin.quiz.network.common.message.ANSWER_QUESTION_PACKET_ID
import me.srikavin.quiz.network.common.message.game.AnswerQuestionMessage
import me.srikavin.quiz.network.common.model.game.GameClient

/**
 * Handles forwarding server packets to the appropriate room or matchmaker
 */
open class GameListener(messageRouter: MessageRouter) {
    protected val clientRoomMap: MutableMap<GameClient, Game> = mutableMapOf()

    init {
        messageRouter.registerHandler(ANSWER_QUESTION_PACKET_ID, object : MessageHandler<AnswerQuestionMessage> {
            override fun handle(client: GameClient, message: AnswerQuestionMessage) {
                println(clientRoomMap)
                println(clientRoomMap[client])
                clientRoomMap[client]?.onAnswer(client, message)
            }
        })
    }
}