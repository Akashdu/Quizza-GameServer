package me.srikavin.quiz.network.server.game

import me.srikavin.quiz.network.common.MessageHandler
import me.srikavin.quiz.network.common.MessageRouter
import me.srikavin.quiz.network.common.game.GameListener
import me.srikavin.quiz.network.common.message.MATCHMAKER_START_PACKET_ID
import me.srikavin.quiz.network.common.message.MATCHMAKER_STOP_PACKET_ID
import me.srikavin.quiz.network.common.message.matchmaker.Matchmaker
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakingStartMessage
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakingStopMessage
import me.srikavin.quiz.network.common.model.game.GameClient

/**
 * Handles forwarding server packets to the appropriate room or matchmaker
 */
class MatchmakerGameListener(private val matchmaker: Matchmaker, messageRouter: MessageRouter) :
    GameListener(messageRouter) {
    init {
        this.matchmaker.onGameCreate = { game ->
            println(game)
            game.players.forEach { player ->
                clientRoomMap[player.gameClient] = game
                println(player)
                println(clientRoomMap)
            }
        }
        messageRouter.registerHandler(MATCHMAKER_START_PACKET_ID, object : MessageHandler<MatchmakingStartMessage> {
            override fun handle(client: GameClient, message: MatchmakingStartMessage) {
                matchmaker.addPlayer(message.quizId, client)
            }
        })
        messageRouter.registerHandler(MATCHMAKER_STOP_PACKET_ID, object : MessageHandler<MatchmakingStopMessage> {
            override fun handle(client: GameClient, message: MatchmakingStopMessage) {
                matchmaker.removePlayer(client)
            }
        })
    }
}