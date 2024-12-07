package ubb.pdm.gamestop.domain.data.game.remote

import ubb.pdm.gamestop.domain.data.game.Game

data class GameEvent(val type: String, val payload: Game)
