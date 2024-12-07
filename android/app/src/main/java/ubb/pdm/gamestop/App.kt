package ubb.pdm.gamestop

import android.util.Log
import ubb.pdm.gamestop.core.TAG
import ubb.pdm.gamestop.core.data.remote.Api

class App : android.app.Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "init")
        container = AppContainer(this)

        Api.init(container.userPreferencesRepository)
    }
}