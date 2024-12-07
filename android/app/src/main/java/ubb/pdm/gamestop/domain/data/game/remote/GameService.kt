package ubb.pdm.gamestop.domain.data.game.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ubb.pdm.gamestop.domain.data.game.Game

interface GameService {
    @GET("/gamestop/api/games")
    suspend fun findAll(@Header("Authorization") authorization: String): List<Game>

    @GET("/gamestop/api/games/{id}")
    suspend fun read(
        @Header("Authorization") authorization: String,
        @Path("id") gameId: String?
    ): Game

    @Headers("Content-Type: application/json")
    @POST("/gamestop/api/games")
    suspend fun create(@Header("Authorization") authorization: String, @Body game: Game): Game

    @Headers("Content-Type: application/json")
    @PUT("/gamestop/api/games")
    suspend fun update(
        @Header("Authorization") authorization: String,
        @Body game: Game
    ): Game

    @DELETE("/gamestop/api/games/{id}")
    suspend fun delete(
        @Header("Authorization") authorization: String,
        @Path("id") gameId: String?
    ): Game
}