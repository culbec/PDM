package ubb.pdm.gamestop

import android.util.Log
import android.app.Application
import androidx.work.Configuration
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.core.data.remote.Api
import ubb.pdm.gamestop.domain.data.game.worker.GameSyncWorkerFactory

class App : Application(), Configuration.Provider {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "init")
        container = AppContainer(this)

        Api.init(container.userPreferencesRepository)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(GameSyncWorkerFactory(container.gameRepository))
            .build()
}