package ubb.pdm.gamestop.domain.data.game

import android.util.Log
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.core.data.remote.Api
import ubb.pdm.gamestop.domain.data.game.local.GameDao
import ubb.pdm.gamestop.domain.data.game.remote.GameService

class GameRepository(
    private val gameService: GameService,
    private val gameDao: GameDao,
) {
    // accessing the games through the data access object
    val gameStream by lazy { gameDao.getAll() }

    init {
        Log.d(TAG, "init")
    }

    // refreshes the local data source with the remote one
    suspend fun refresh() {
        Log.d(TAG, "refresh started")

        try {
            val games = gameService.findAll(authorization = Api.getBearerToken())
            gameDao.insert(games)
            
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

    private suspend fun handleGameCreated(game: Game) {
        Log.d(TAG, "handleGameCreated: $game")
        gameDao.insert(
            game.copy(
                syncOperation = SyncOperation.NONE,
                syncStatus = SyncStatus.SYNCED,
                lastSync = System.currentTimeMillis()
            )
        )
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
        gameDao.update(
            game.copy(
                syncOperation = SyncOperation.NONE,
                syncStatus = SyncStatus.SYNCED,
                lastSync = System.currentTimeMillis()
            )
        )
    }

    suspend fun update(game: Game): Game {
        Log.d(TAG, "update: $game")
        val updatedGame: Game =
            gameService.update(authorization = Api.getBearerToken(), game = game)

        Log.d(TAG, "update updatedGame: $updatedGame")
        handleGameUpdated(updatedGame)
        return updatedGame
    }
    
    suspend fun insertPending(game: Game) {
        Log.d(TAG, "insertPending: $game")
        gameDao.insert(
            game.copy(
                syncStatus = SyncStatus.PENDING
            )
        )
    }
    
    suspend fun updateSyncStatus(game: Game, syncStatus: SyncStatus) {
        Log.d(TAG, "updateSyncStatus: $game, $syncStatus")
        gameDao.update(game.copy(syncStatus = syncStatus))
    }
    
    suspend fun deleteErrorGames() {
        Log.d(TAG, "deleteErrorGames")
        gameDao.deleteErrorGames()
    }
    
   fun getPendingGames(): List<Game> {
        Log.d(TAG, "getPendingGames")
        val pendingGames = gameDao.getPending()
        Log.d(TAG, "getPendingGames: $pendingGames")
        return pendingGames
    }
}