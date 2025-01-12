package ubb.pdm.gamestop

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ubb.pdm.gamestop.auth.core.AuthRepository
import ubb.pdm.gamestop.auth.core.remote.AuthDataSource
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.core.data.UserPreferencesRepository
import ubb.pdm.gamestop.core.data.remote.Api
import ubb.pdm.gamestop.ui.theme.GameStopTheme

class MainActivity : ComponentActivity() {
    var isAuthenticating = true
    val authRepository: AuthRepository = AuthRepository(AuthDataSource())
    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this.userPreferencesDataStore)
    }
    var startingRoute: String = AppRoutes.LOGIN.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { isAuthenticating }


        runBlocking(Dispatchers.IO) {
            val isAuthenticated = checkAuthentication()

            if (isAuthenticated) {
                Api.authInterceptor.token = userPreferencesRepository.get("token")
                startingRoute = AppRoutes.GAMES.value
            } else {
                Api.clearTokenAndPreferences()
                startingRoute = AppRoutes.LOGIN.value
            }

            isAuthenticating = false
        }


        setContent {
            GameStopTheme {
                App {
                    AppNavHost(startingRoute)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            (application as App).container.wsClient.connectWebSocket()
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            (application as App).container.wsClient.closeWebSocket()
        }
    }

    private suspend fun checkAuthentication(): Boolean {
        return withContext(Dispatchers.IO) {
            val token = userPreferencesRepository.get("token")

            if (token.isEmpty()) {
                return@withContext false
            }

            try {
                val result = authRepository.validate(token)
                result.isSuccess && result.getOrNull() == true
            } catch (e: Exception) {
                Log.e(TAG, "validate failed: ${e.message}")
                false
            }
        }
    }
}

@Composable
fun App(content: @Composable () -> Unit) {
    Log.d("App", "recompose")
    Surface {
        content()
    }
}

@Preview
@Composable
fun PreviewApp() {
    App {}
}