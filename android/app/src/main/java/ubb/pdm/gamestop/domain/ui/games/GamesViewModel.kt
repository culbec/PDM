package ubb.pdm.gamestop.domain.ui.games

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ubb.pdm.gamestop.App
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.domain.data.game.Game
import ubb.pdm.gamestop.domain.data.game.GameRepository

data class GamesState(
    val isLoading: Boolean = false,
)

class GamesViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {
    val gamesStream: Flow<List<Game>> = gameRepository.gameStream
    private val _gamesState = mutableStateOf(GamesState())
    val gamesState: MutableState<GamesState> = _gamesState

    init {
        Log.d(TAG, "init")
    }

    private fun loadGames() {
        Log.d(TAG, "load games...")
        _gamesState.value = _gamesState.value.copy(isLoading = true)

        // refresh -> clear local cache and fetch data from the server
        viewModelScope.launch {
            gameRepository.refresh()
            _gamesState.value = gamesState.value.copy(isLoading = false)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                GamesViewModel(app.container.gameRepository)
            }
        }
    }
}