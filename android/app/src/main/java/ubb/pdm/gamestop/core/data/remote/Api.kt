package ubb.pdm.gamestop.core.data.remote

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.bson.types.ObjectId
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ubb.pdm.gamestop.core.ObjectIdDeserializer
import ubb.pdm.gamestop.core.data.UserPreferencesRepository

object Api {
    private const val URL = "172.30.249.45"
    private const val HTTP_URL = "http://$URL:3000/"
    const val WS_URL = "ws://$URL:3000/"

    private lateinit var userPreferencesRepository: UserPreferencesRepository

    fun init(userPreferencesRepository: UserPreferencesRepository) {
        this.userPreferencesRepository = userPreferencesRepository
        this.authInterceptor = AuthInterceptor { this.authInterceptor.token }
        this.okHttpClient = OkHttpClient.Builder()
            .addInterceptor(this.authInterceptor)
            .build()
    }

    fun getBearerToken() = "Bearer ${this.authInterceptor.token}"

    suspend fun clearTokenAndPreferences() {
        withContext(Dispatchers.IO) {
            this@Api.authInterceptor.token = null
            userPreferencesRepository.clear()
        }
    }

    // JSON converter by Google
    private var gson = GsonBuilder()
        .registerTypeAdapter(ObjectId::class.java, ObjectIdDeserializer())
        .create()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(HTTP_URL)
        .addConverterFactory(GsonConverterFactory.create(gson)) // Add the JSON converter
        .build()

    lateinit var authInterceptor: AuthInterceptor
    lateinit var okHttpClient: OkHttpClient
}