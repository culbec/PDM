package ubb.pdm.gamestop.core.ws.ui


import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import ubb.pdm.gamestop.core.notifications.MyNotifications
import ubb.pdm.gamestop.core.ws.WsEvent
import ubb.pdm.gamestop.core.ws.WsViewModel

const val tag = "WsNotifications"
const val CHANNEL_ID = "GameStopChannel"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WsNotifications() {
    val wsViewModel = viewModel<WsViewModel>(factory = WsViewModel.Factory)
    val wsEvent by wsViewModel.latestEvent.collectAsState()
    
    @Composable
    fun onEvent(wsEvent: WsEvent) {
        Log.i("WsNotifications", "Received notification: $wsEvent")
        
        val message = wsEvent.payload
        val sender = wsEvent.sender

        MyNotifications.showSimpleNotification(
            context = LocalContext.current,
            channelId = CHANNEL_ID,
            2,
            textTitle = "New notification",
            textContent = "From: $sender\n$message"
        )
    }
    
    @Composable
    fun onError(wsEvent: WsEvent) {
        Log.e("WsNotifications", "Received error: $wsEvent")
        
        val message = wsEvent.payload
        val sender = wsEvent.sender

        MyNotifications.showSimpleNotification(
            context = LocalContext.current,
            channelId = CHANNEL_ID,
            2,
            textTitle = "Error",
            textContent = "From: $sender\n$message"
        )
    }

    wsEvent?.let {
        Log.i("WsNotifications", "Received event: $it")

        when (wsEvent!!.type) {
            "notification" -> {
                onEvent(wsEvent!!)
            }
            "error" -> {
                onError(wsEvent!!)
            }
        }

    }
}

