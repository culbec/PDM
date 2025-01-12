package ubb.pdm.gamestop.core.ws

data class WsEvent(val type: String, val payload: Any, val sender: String)
