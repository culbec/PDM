package ubb.pdm.gamestop.domain.ui.location

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import ubb.pdm.gamestop.core.location.LocationMonitor
import ubb.pdm.gamestop.core.util.TAG

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf<Location?>(null)
        private set

    init {
        collectLocation()
    }

    private fun collectLocation() {
        viewModelScope.launch {
            LocationMonitor(getApplication()).currentLocation.collect {
                Log.d(TAG, "new location: $it")
                uiState = it
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LocationViewModel(application)
            }
        }
    }
}