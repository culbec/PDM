package ubb.pdm.gamestop.core.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ubb.pdm.gamestop.core.TAG
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    // Which keys are we storing as preferences?
    private object PreferencesKeys {
        val username = stringPreferencesKey("username")
        val userId = stringPreferencesKey("user_id")
        val token = stringPreferencesKey("token")
    }

    init {
        Log.d(TAG, "init")
    }

    // Stream for storing/clearing preferences
    val userPreferencesStream: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { mapUserPreferences(it) }

    private fun mapUserPreferences(preferences: Preferences) =
        UserPreferences(
            username = preferences[PreferencesKeys.username] ?: "",
            userId = preferences[PreferencesKeys.userId] ?: "",
            token = preferences[PreferencesKeys.token] ?: ""
        )

    suspend fun save(userPreferences: UserPreferences) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.username] = userPreferences.username.toString()
            preferences[PreferencesKeys.userId] = userPreferences.userId.toString()
            preferences[PreferencesKeys.token] = userPreferences.token.toString()
        }
    }

    suspend fun get(key: String): String {
        val preferences = dataStore.data.first()
        return preferences[stringPreferencesKey(key)] ?: ""
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}