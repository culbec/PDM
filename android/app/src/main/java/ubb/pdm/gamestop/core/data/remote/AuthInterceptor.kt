package ubb.pdm.gamestop.core.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ubb.pdm.gamestop.core.util.SessionManager
import ubb.pdm.gamestop.core.util.TAG

class AuthInterceptor(
    private val tokenProvider: () -> String?,
) : Interceptor {
    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val origin = chain.request()
        val originUrl = origin.url

        val request = origin.newBuilder()
            .apply {
                tokenProvider()?.let { token ->
                    addHeader("Authorization", "Bearer $token")
                }
            }.url(originUrl)
            .build()

        val response = chain.proceed(request)
        token = tokenProvider()

        // unauthorized
        // clear preferences (clear token also)
        if (response.code == 401) {
            Log.d(TAG, "Unauthorized")
            token = null

            // blocking call to clear preferences
            // caused because the datastore of the preferences is modified
            runBlocking(Dispatchers.IO) {
                Api.clearTokenAndPreferences()
                SessionManager.invalidateSession()
            }
        }

        return response
    }
}