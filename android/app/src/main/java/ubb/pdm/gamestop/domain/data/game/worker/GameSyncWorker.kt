package ubb.pdm.gamestop.domain.data.game.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.domain.data.game.Game
import ubb.pdm.gamestop.domain.data.game.GameRepository
import ubb.pdm.gamestop.domain.data.game.SyncOperation
import ubb.pdm.gamestop.domain.data.game.SyncStatus

class GameSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val gameRepository: GameRepository,
) : CoroutineWorker(context, params) {
    private var errors: String = ""

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "doing work...")
        errors = ""

        try {
            // Get pending games once, not as a Flow
            val pendingGames = gameRepository.getPendingGames()

            // Refresh after retrieving pending games
            gameRepository.refresh()
            
            pendingGames.forEach { game ->
                Log.d(TAG, "syncing game: $game")
                processGame(game)
            }

            if (errors.isNotEmpty()) {
                gameRepository.deleteErrorGames()
                Result.failure(workDataOf("errors" to errors))
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Work failed", e)
            Result.failure(workDataOf("errors" to e.message.orEmpty()))
        }
    }

    private suspend fun processGame(game: Game) {
        Log.d(TAG, "processing game: $game")
        
        gameRepository.updateSyncStatus(game, SyncStatus.SYNCING)

        try {
            when (game.syncOperation) {
                SyncOperation.CREATE -> gameRepository.save(game)
                SyncOperation.UPDATE -> gameRepository.update(game)
                SyncOperation.DELETE -> gameRepository.delete(game)
                SyncOperation.NONE -> Unit
            }

            if (game.syncOperation != SyncOperation.DELETE) {
                gameRepository.updateSyncStatus(game, SyncStatus.SYNCED)
            }
            Log.d(TAG, "game ${game.syncOperation} successful: $game")
        } catch (e: Exception) {
            Log.w(TAG, "game ${game.syncOperation} failed: $game", e)
            handleError(game, e)
        }
    }

    private suspend fun handleError(game: Game, e: Exception) {
        val newStatus = if (e.message?.contains("Failed to connect to") == true) {
            SyncStatus.PENDING
        } else {
            errors += "Failed to ${game.syncOperation} game: $game\n"
            SyncStatus.ERROR
        }
        gameRepository.updateSyncStatus(game, newStatus)
    }
}