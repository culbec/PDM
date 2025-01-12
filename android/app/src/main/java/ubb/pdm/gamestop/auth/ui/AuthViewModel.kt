package ubb.pdm.gamestop.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ubb.pdm.gamestop.App
import ubb.pdm.gamestop.auth.core.AuthRepository
import ubb.pdm.gamestop.auth.core.remote.TokenHolder
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.core.data.UserPreferences
import ubb.pdm.gamestop.core.data.UserPreferencesRepository

data class LoginState(
    val isAuthenticating: Boolean = false,
    val authenticationError: Throwable? = null,
    val authenticationCompleted: Boolean = false,
    val tokenHolder: TokenHolder? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    var loginState: LoginState = LoginState()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                AuthViewModel(
                    app.container.authRepository,
                    app.container.userPreferencesRepository
                )
            }
        }
    }

    init {
        Log.d(TAG, "init")
    }

    // the view model interacts with the repository to perform the login operation
    fun login(username: String, password: String) {
        // Run login operation on DEFAULT thread pool
        viewModelScope.launch(context = Dispatchers.Default) {
            Log.v(TAG, "login...")

            loginState = loginState.copy(isAuthenticating = true, authenticationError = null)

            // repository call to access the auth data source
            val result = authRepository.login(username, password)
            Log.d(TAG, result.toString())

            // update the login state based on the repository call
            if (result.isSuccess) {
                userPreferencesRepository.save(
                    UserPreferences(
                        username = result.getOrNull()?.username ?: "",
                        userId = result.getOrNull()?.user_id ?: "",
                        token = result.getOrNull()?.token ?: ""
                    )
                )
                loginState =
                    loginState.copy(isAuthenticating = false, authenticationCompleted = true)
            } else {
                loginState = loginState.copy(
                    isAuthenticating = false,
                    authenticationError = result.exceptionOrNull()
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch(context = Dispatchers.Default) {
            Log.v(TAG, "logout...")

            loginState = loginState.copy(isAuthenticating = true, authenticationError = null)

            val result = authRepository.logout()

            loginState = if (result.isSuccess) {
                loginState.copy(isAuthenticating = false, authenticationCompleted = true)
            } else {
                loginState.copy(
                    isAuthenticating = false,
                    authenticationError = result.exceptionOrNull()
                )
            }

            userPreferencesRepository.save(
                UserPreferences(
                    userId = "",
                    token = ""
                )
            )
        }
    }
}