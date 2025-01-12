package ubb.pdm.gamestop.core.ws

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.StateFlow
import ubb.pdm.gamestop.App

class WsViewModel(
    private val wsClient: WsClient
) : ViewModel() {
    val latestEvent: StateFlow<WsEvent?> = wsClient.latestEvent

    init {
        wsClient.connectWebSocket()
    }

    fun setCredentials(username: String, token: String) {
        wsClient.setCredentials(username, token)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                WsViewModel(app.container.wsClient)
            }
        }
    }
}