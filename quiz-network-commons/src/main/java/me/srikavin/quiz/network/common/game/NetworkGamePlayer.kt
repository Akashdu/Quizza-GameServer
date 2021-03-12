package me.srikavin.quiz.network.common.game

import me.srikavin.quiz.network.common.model.game.BackingClient
import me.srikavin.quiz.network.common.model.game.GameClient
import me.srikavin.quiz.network.common.model.game.GamePlayer

class NetworkGamePlayer(
        val gameClient: GameClient,
        var player: GamePlayer
) : BackingClient by gameClient.backing