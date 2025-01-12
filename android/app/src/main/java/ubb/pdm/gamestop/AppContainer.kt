package ubb.pdm.gamestop

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import ubb.pdm.gamestop.auth.core.AuthRepository
import ubb.pdm.gamestop.auth.core.remote.AuthDataSource
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.core.data.UserPreferencesRepository
import ubb.pdm.gamestop.core.data.remote.Api
import ubb.pdm.gamestop.core.network.ConnectivityObserver
import ubb.pdm.gamestop.core.network.MyConnectivityObserver
import ubb.pdm.gamestop.core.ws.WsClient
import ubb.pdm.gamestop.domain.data.game.GameRepository
import ubb.pdm.gamestop.domain.data.game.remote.GameService
import ubb.pdm.gamestop.domain.data.photo.PhotoRepository
import ubb.pdm.gamestop.domain.data.photo.remote.PhotoService

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    private val authDataSource: AuthDataSource = AuthDataSource()
    private val gameService: GameService = Api.retrofit.create(GameService::class.java)

    private val photoService: PhotoService = Api.retrofit.create(PhotoService::class.java)

    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    
    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    val gameRepository: GameRepository by lazy {
        GameRepository(gameService, database.gameDao())
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }
    
    val connectivityObserver: ConnectivityObserver by lazy {
        MyConnectivityObserver(context)
    }

    init {
        Log.d(TAG, "init")
        Api.init(userPreferencesRepository)
    }

    val photoRepository: PhotoRepository by lazy {
        PhotoRepository(photoService, database.photoDao())
    }
    
    val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    val wsClient: WsClient = WsClient(Api.okHttpClient)
}