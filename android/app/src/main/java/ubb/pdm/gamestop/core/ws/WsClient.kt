package ubb.pdm.gamestop.core.ws

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.core.data.remote.Api

class WsClient(private val okHttpClient: OkHttpClient) {
    private val moshi = Moshi.Builder().build()
    private val scope = CoroutineScope(Dispatchers.IO)

    // MutableStateFlow to hold the latest event
    private val _latestEvent = MutableStateFlow<WsEvent?>(null)
    val latestEvent: StateFlow<WsEvent?> = _latestEvent.asStateFlow()

    private var webSocket: WebSocket? = null

    private val wsListener = object : WebSocketListener() {
        private val wsEventJsonAdapter: JsonAdapter<WsEvent> =
            moshi.adapter(WsEvent::class.java)

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connection opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received message: $text")
            val wsEvent = wsEventJsonAdapter.fromJson(text)

            // Update the latest event
            scope.launch {
                _latestEvent.value = wsEvent
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: code=$code, reason=$reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: code=$code, reason=$reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}")
        }
    }

    fun connectWebSocket() {
        val request = Request.Builder().url(Api.WS_URL).build()
        webSocket = okHttpClient.newWebSocket(request, wsListener)
    }

    fun setCredentials(username: String, token: String) {
        val auth = """
            {
                "type": "authorization",
                "payload": "$token",
                "sender": "$username"
            }
        """.trimIndent()

        Log.d(TAG, "Sending authorization")
        webSocket?.send(auth)
    }

    fun closeWebSocket() {
        webSocket?.close(1000, "Client closing connection")
        webSocket = null
    }
}