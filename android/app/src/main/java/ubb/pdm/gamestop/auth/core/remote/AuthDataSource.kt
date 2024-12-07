package ubb.pdm.gamestop.auth.core.remote

import android.util.Log
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import ubb.pdm.gamestop.core.TAG
import ubb.pdm.gamestop.core.data.remote.Api

class AuthDataSource {
    interface AuthService {
        @Headers(
            "Content-type: application/json",
            "Accept: application/json"
        )
        @POST("gamestop/api/auth/login")
        suspend fun login(@Body user: User): TokenHolder

        @Headers("Accept: application/json")
        @POST("gamestop/api/auth/logout")
        suspend fun logout(@Header("Authorization") authorization: String)

        @POST("gamestop/api/auth/validate")
        suspend fun validate(@Header("Authorization") authorization: String)
    }

    private val authService: AuthService = Api.retrofit.create(AuthService::class.java)

    // login operation
    // the data source is responsible for accessing the data and only the data source
    suspend fun login(user: User): Result<TokenHolder> {
        try {
            Log.d(TAG, "login: $user")
            val result = authService.login(user)
            result.username = user.username

            return Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "login failed: ${e.message}")
            return Result.failure(e)
        }
    }

    suspend fun validate(authorization: String): Result<Boolean> {
        try {
            authService.validate("Bearer $authorization")
            return Result.success(true)
        } catch (e: Exception) {
            Log.d(TAG, "validate failed: ${e.message}")
            return Result.failure(e)
        }
    }

    // logout operation
    suspend fun logout(authorization: String): Result<Unit> {
        try {
            Log.d(TAG, "logout")
            return Result.success(authService.logout(authorization))
        } catch (e: Exception) {
            Log.d(TAG, "logout failed: ${e.message}")

            // Check if unauthorized
            if (e.message?.contains("401") == true) {
                Api.authInterceptor.token = null
            }

            return Result.failure(e)
        }
    }
}