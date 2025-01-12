package ubb.pdm.gamestop.core.network.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ubb.pdm.gamestop.App
import ubb.pdm.gamestop.core.network.ConnectivityObserver
import ubb.pdm.gamestop.core.ws.WsClient

data class NetworkStatusState(
    var isConnected: StateFlow<Boolean> = MutableStateFlow(false)
)

@Composable
fun MyNetworkStatus() {
    val myNetworkStatusViewModel = viewModel<MyNetworkStatusViewModel>(
        factory = MyNetworkStatusViewModel.Factory
    )

    val isConnected by myNetworkStatusViewModel.networkStatusState
        .isConnected
        .collectAsState()
    
    if (isConnected) {
        myNetworkStatusViewModel.connectWebSocket()
    } else {
        myNetworkStatusViewModel.closeWebSocket()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(if (isConnected) Color.Green else Color.Red)
            .border(1.dp, Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isConnected) "Connected" else "Disconnected",
            style = TextStyle(
                color = if (isConnected) Color.Black else Color.White,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            ),
            modifier = Modifier
                .padding(16.dp)
        )
    }
}

@Preview
@Composable
fun MyNetworkStatusPreview() {
    MyNetworkStatus()
}

class MyNetworkStatusViewModel(
    private val connectivityObserver: ConnectivityObserver,
    private val wsClient: WsClient
) :
    ViewModel() {
    var networkStatusState by mutableStateOf(NetworkStatusState())
        private set

    init {
        collectNetworkStatus()
    }

    private fun collectNetworkStatus() {
        networkStatusState.isConnected = connectivityObserver
            .isConnected
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                false
            )
    }
    
    fun connectWebSocket() = wsClient.connectWebSocket()
    fun closeWebSocket() = wsClient.closeWebSocket()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                MyNetworkStatusViewModel(app.container.connectivityObserver, app.container.wsClient)
            }
        }
    }
}