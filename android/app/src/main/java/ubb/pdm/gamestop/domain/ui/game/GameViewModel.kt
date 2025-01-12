package ubb.pdm.gamestop.domain.ui.game

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import ubb.pdm.gamestop.App
import ubb.pdm.gamestop.core.util.Result
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.domain.data.game.Game
import ubb.pdm.gamestop.domain.data.game.GameRepository
import ubb.pdm.gamestop.domain.data.game.SyncOperation
import ubb.pdm.gamestop.domain.data.game.SyncStatus

data class GameState(
    val gameId: String? = null,
    val game: Game = Game(),
    val loadResult: Result<Game>? = null,
    val submitResult: Result<Game>? = null
)

class GameViewModel(
    private val gameId: String?,
    private val gameRepository: GameRepository
) : ViewModel() {
    private var _gameState = mutableStateOf(GameState(loadResult = Result.Loading))
    val gameState: MutableState<GameState> = _gameState

    fun loadGame() {
        viewModelScope.launch {
            gameRepository.gameStream.collect { games ->
                if (_gameState.value.loadResult !is Result.Loading) {
                    return@collect
                }
                val game = games.find { it.id == gameId } ?: Game()
                _gameState.value =
                    _gameState.value.copy(game = game, loadResult = Result.Success(game))
            }
        }
    }

    init {
        Log.d(TAG, "init")

        // Load existing game or leave the default empty game
        if (gameId != null) {
            loadGame()
        } else {
            _gameState.value = _gameState.value.copy(loadResult = Result.Success(Game()))
        }
    }

    fun saveOrUpdateGame(
        game: Game
    ) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateGame...")
            try {
                _gameState.value = _gameState.value.copy(submitResult = Result.Loading)
                
                // Inserting the game into the database with
                // the sync operation and PENDING as sync status
                game.syncOperation = if (gameId != null) {
                    SyncOperation.UPDATE
                } else {
                    SyncOperation.CREATE
                }
                game.syncStatus = SyncStatus.PENDING
                gameRepository.insertPending(game)

                _gameState.value = _gameState.value.copy(
                    game = game,
                    submitResult = Result.Success(game)
                )
            } catch (e: Exception) {
                Log.w(TAG, "saveOrUpdateGame failed", e)
                _gameState.value = _gameState.value.copy(submitResult = Result.Error(e))
            }
        }
    }
    
    fun deleteGame(
        game: Game
    ) {
        viewModelScope.launch {
            Log.d(TAG, "deleteGame...")
            try {
                _gameState.value = _gameState.value.copy(submitResult = Result.Loading)
                
                // Inserting the game into the database with
                // the sync operation and PENDING as sync status
                game.syncOperation = SyncOperation.DELETE
                game.syncStatus = SyncStatus.PENDING
                gameRepository.insertPending(game)

                _gameState.value = _gameState.value.copy(
                    game = game,
                    submitResult = Result.Success(game)
                )
            } catch (e: Exception) {
                Log.w(TAG, "deleteGame failed", e)
                _gameState.value = _gameState.value.copy(submitResult = Result.Error(e))
            }
        }
    }

    companion object {
        fun Factory(gameId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                GameViewModel(gameId, app.container.gameRepository)
            }
        }
    }
}