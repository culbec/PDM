package ubb.pdm.gamestop.domain.data.game

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ubb.pdm.gamestop.core.TAG
import ubb.pdm.gamestop.core.data.remote.Api
import ubb.pdm.gamestop.domain.data.game.local.GameDao
import ubb.pdm.gamestop.domain.data.game.remote.GameEvent
import ubb.pdm.gamestop.domain.data.game.remote.GameService
import ubb.pdm.gamestop.domain.data.game.remote.GameWsClient

class GameRepository(
    private val gameService: GameService,
    private val gameDao: GameDao,
    private val gameWsClient: GameWsClient
) {
    // accessing the games through the data access object
    val gameStream by lazy { gameDao.getAll() }

    init {
        Log.d(TAG, "init")
    }

    // refreshes teh local data source with the remote one
    suspend fun refresh() {
        Log.d(TAG, "refresh started")

        try {
            val games = gameService.findAll(authorization = Api.getBearerToken())
            gameDao.deleteAll()

            games.forEach {
                Log.d(TAG, "refresh game: $it")
                gameDao.insert(it)
            }
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)

            // check for unauthorized
            if (e.message?.contains("401") == true) {
                Log.d(TAG, "Unauthorized, clearing token")
                Api.clearTokenAndPreferences()
            }
        }
    }

    fun getGameEvents(): Flow<Result<GameEvent>> = callbackFlow {
        Log.d(TAG, "getGameEvents started")

        gameWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent: $it")
                if (it != null) {
                    trySend(Result.success(it))
                }
            },
            onClosed = {
                Log.d(TAG, "onClosed")
                close()
            },
            onFailure = {
                Log.d(TAG, "onFailure")
                close()
            }
        )
    }

    fun setToken(userId: String, token: String) {
        Log.d(TAG, "setToken: $token")
        gameWsClient.authorize(userId, token)
    }

    private suspend fun handleGameCreated(game: Game) {
        Log.d(TAG, "handleGameCreated: $game")
        gameDao.insert(game)
    }

    suspend fun save(game: Game): Game {
        val savedGame: Game = gameService.create(authorization = Api.getBearerToken(), game = game)
        Log.d(TAG, "save newGame: $savedGame")
        handleGameCreated(savedGame)
        return savedGame
    }

    private suspend fun handleGameDeleted(game: Game) {
        Log.d(TAG, "handleGameDeleted: $game")
        gameDao.deleteById(game.id)
    }

    suspend fun delete(game: Game): Game {
        Log.d(TAG, "delete: $game")
        gameService.delete(authorization = Api.getBearerToken(), gameId = game.id)
        handleGameDeleted(game)
        return game
    }

    private suspend fun handleGameUpdated(game: Game) {
        Log.d(TAG, "handleGameUpdated: $game")
        gameDao.update(game)
    }

    suspend fun update(game: Game): Game {
        Log.d(TAG, "update: $game")
        val updatedGame: Game =
            gameService.update(authorization = Api.getBearerToken(), game = game)

        Log.d(TAG, "update updatedGame: $updatedGame")
        handleGameUpdated(updatedGame)
        return updatedGame
    }
}