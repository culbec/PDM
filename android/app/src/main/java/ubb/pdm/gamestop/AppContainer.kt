package ubb.pdm.gamestop

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import ubb.pdm.gamestop.auth.core.AuthRepository
import ubb.pdm.gamestop.auth.core.remote.AuthDataSource
import ubb.pdm.gamestop.core.TAG
import ubb.pdm.gamestop.core.data.UserPreferencesRepository
import ubb.pdm.gamestop.core.data.remote.Api
import ubb.pdm.gamestop.domain.data.game.GameRepository
import ubb.pdm.gamestop.domain.data.game.remote.GameService
import ubb.pdm.gamestop.domain.data.game.remote.GameWsClient
import ubb.pdm.gamestop.domain.data.photo.PhotoRepository
import ubb.pdm.gamestop.domain.data.photo.remote.PhotoService

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    private val authDataSource: AuthDataSource = AuthDataSource()
    private val gameService: GameService = Api.retrofit.create(GameService::class.java)
    private val gameWsClient: GameWsClient by lazy { GameWsClient(Api.okHttpClient) }

    private val photoService: PhotoService = Api.retrofit.create(PhotoService::class.java)

    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    val gameRepository: GameRepository by lazy {
        GameRepository(gameService, database.gameDao(), gameWsClient)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }

    init {
        Log.d(TAG, "init")
        Api.init(userPreferencesRepository)
    }

    val photoRepository: PhotoRepository by lazy {
        PhotoRepository(photoService, database.photoDao())
    }
}