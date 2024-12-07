package ubb.pdm.gamestop.auth.core.remote

data class TokenHolder(
    var username: String,
    val user_id: String,
    val token: String
)
