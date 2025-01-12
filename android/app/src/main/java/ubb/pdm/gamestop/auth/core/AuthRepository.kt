package ubb.pdm.gamestop.auth.core

import android.util.Log
import ubb.pdm.gamestop.auth.core.remote.AuthDataSource
import ubb.pdm.gamestop.auth.core.remote.TokenHolder
import ubb.pdm.gamestop.auth.core.remote.User
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.core.data.remote.Api

class AuthRepository(private val authDataSource: AuthDataSource) {
    init {
        Log.d(TAG, "init")
    }

    // repository call on the data source
    // the repository is responsible for accessing the data source and keeping the data intact and updated
    suspend fun login(username: String, password: String): Result<TokenHolder> {
        val user = User(username, password)
        Log.i(TAG, "login: $user")
        val result = authDataSource.login(user)
        if (result.isSuccess) {
            Api.authInterceptor.token = result.getOrNull()?.token
        }
        return result
    }

    suspend fun validate(token: String): Result<Boolean> {
        return authDataSource.validate(authorization = token)
    }

    suspend fun logout(): Result<Unit> {
        val result = authDataSource.logout(authorization = Api.getBearerToken())

        if (result.isSuccess) {
            Api.clearTokenAndPreferences()
        }

        return result
    }
}