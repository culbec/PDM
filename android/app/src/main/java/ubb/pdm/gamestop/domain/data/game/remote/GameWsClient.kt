package ubb.pdm.gamestop.domain.data.game.remote

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ubb.pdm.gamestop.core.TAG
import ubb.pdm.gamestop.core.data.remote.Api

class GameWsClient(private val okHttpClient: OkHttpClient) {
    lateinit var webSocket: WebSocket

    suspend fun openSocket(
        onEvent: (gameEvent: GameEvent?) -> Unit,
        onClosed: () -> Unit,
        onFailure: () -> Unit
    ) {
        // run the openSocket method in the IO context
        withContext(Dispatchers.IO) {
            Log.d(TAG, "openSocket")

            val request = Request.Builder().url(Api.WS_URL).build()
            webSocket = okHttpClient.newWebSocket(
                request,
                GameWebSocketListener(onEvent = onEvent, onClosed = onClosed, onFailure = onFailure)
            )

            // close the executor service after the websocket was created
            okHttpClient.dispatcher.executorService.shutdown()
        }
    }

    fun closeSocket() {
        Log.d(TAG, "closeSocket")
        webSocket.close(1000, "")
    }

    inner class GameWebSocketListener(
        private val onEvent: (gameEvent: GameEvent?) -> Unit,
        private val onClosed: () -> Unit,
        private val onFailure: () -> Unit
    ) : WebSocketListener() {
        private val moshi = Moshi.Builder().build()
        private val gameEventJsonAdapter: JsonAdapter<GameEvent> =
            moshi.adapter(GameEvent::class.java)

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "onOpen")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "onMessage message: $text")

            val gameEvent = gameEventJsonAdapter.fromJson(text)
            onEvent(gameEvent)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {}

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosed")
            onClosed()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d(TAG, "onFailure: ${t.message}")
            onFailure()
        }
    }

    fun authorize(userId: String, token: String) {
        // send the token to the server through the websocket
        val auth = """
            {
                "type": "authorization",
                "payload": "$token",
                "sender": "$userId"
            }
        """.trimIndent()

        Log.d(TAG, "auth $auth")
        webSocket.send(auth)
    }
}