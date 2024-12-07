package ubb.pdm.gamestop.core.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ubb.pdm.gamestop.App
import ubb.pdm.gamestop.core.TAG
import ubb.pdm.gamestop.core.data.UserPreferences
import ubb.pdm.gamestop.core.data.UserPreferencesRepository

class UserPreferencesViewModel(private val userPreferencesRepository: UserPreferencesRepository) :
    ViewModel() {
    val userPreferencesState: Flow<UserPreferences> =
        userPreferencesRepository.userPreferencesStream

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                UserPreferencesViewModel(app.container.userPreferencesRepository)
            }
        }
    }

    init {
        Log.d(TAG, "init")
    }

    fun save(userPreferences: UserPreferences) {
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.d(TAG, "saveUserPreferences...")
            userPreferencesRepository.save(userPreferences)
        }
    }

    suspend fun get(key: String): String {
        return withContext(Dispatchers.IO) {
            userPreferencesRepository.get(key)
        }
    }
}