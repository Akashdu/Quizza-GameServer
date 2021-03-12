package me.srikavin.quiz.network.common.message.matchmaker

import me.srikavin.quiz.network.common.game.Game
import me.srikavin.quiz.network.common.model.data.ResourceId
import me.srikavin.quiz.network.common.model.game.GameClient

interface Matchmaker {
    var onGameCreate: (Game) -> Unit
    fun addPlayer(resourceId: ResourceId, client: GameClient)
    fun removePlayer(client: GameClient)
}