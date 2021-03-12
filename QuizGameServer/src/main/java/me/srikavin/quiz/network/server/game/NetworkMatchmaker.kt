package me.srikavin.quiz.network.server.game

import me.srikavin.quiz.network.common.game.Game
import me.srikavin.quiz.network.common.game.NetworkGamePlayer
import me.srikavin.quiz.network.common.message.matchmaker.Matchmaker
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakerStateUpdateMessage
import me.srikavin.quiz.network.common.model.data.ResourceId
import me.srikavin.quiz.network.common.model.game.GameClient
import me.srikavin.quiz.network.common.model.game.GamePlayer
import me.srikavin.quiz.network.common.model.matchmaker.MatchmakerStates
import me.srikavin.quiz.network.common.model.matchmaker.MatchmakingState
import me.srikavin.quiz.network.server.model.QuizRepository
import mu.KotlinLogging


const val PLAYERS_PER_GAME: Short = 2

class NetworkMatchmaker(private val quizRepository: QuizRepository) :
    Matchmaker {
    override var onGameCreate: (Game) -> Unit = {}
    private val map: MutableMap<ResourceId, MutableList<GameClient>> = mutableMapOf()
    private val logger = KotlinLogging.logger("Matchmaker")

    private fun sendStateUpdate(resourceId: ResourceId) {
        sendStateUpdate(resourceId, MatchmakerStates.SEARCHING)
    }

    private fun sendStateUpdate(resourceId: ResourceId, state: MatchmakerStates) {
        val numberConnected = map[resourceId]?.size?.toShort() ?: return

        val update = MatchmakerStateUpdateMessage(MatchmakingState(state, numberConnected, PLAYERS_PER_GAME))
        map[resourceId]?.forEach { cl ->
            cl.backing.send(update)
        }
    }

    override fun addPlayer(resourceId: ResourceId, client: GameClient) {
        logger.info { "Client has begun matchmaking: ${client.backing.id}" }
        if (!map.containsKey(resourceId)) {
            map[resourceId] = ArrayList()
            logger.info { "New group has been created (quiz = $resourceId) for ${client.backing.id}" }
        }

        map[resourceId]?.add(client)

        sendStateUpdate(resourceId)

        val numberConnected = map[resourceId]?.size?.toShort() ?: return

        if (numberConnected == PLAYERS_PER_GAME) {
            val players = map[resourceId]

            logger.info { "Game has been created for (quiz = $resourceId) with ${players?.size} players" }
            sendStateUpdate(resourceId, MatchmakerStates.MATCH_FOUND)

            map.remove(resourceId)

            val networkGamePlayers = players
                ?.map { p -> NetworkGamePlayer(p, GamePlayer(p.backing.id, "Testing", 0, "http://testing.com")) }!!
            val createdGame = Game(quizRepository.getQuiz(resourceId)!!, networkGamePlayers)

            createdGame.start()

            onGameCreate(createdGame)
        }
    }

    override fun removePlayer(client: GameClient) {
        logger.info { "Client has stopped matchmaking: ${client.backing.id}" }
        val toRemove = map.filter { e -> e.value.contains(client) }.toList()
        toRemove.forEach { remove ->
            map[remove.first]?.remove(client)
            sendStateUpdate(remove.first)
        }
    }

}