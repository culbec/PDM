package ubb.pdm.gamestop.domain.ui.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ubb.pdm.gamestop.domain.data.game.Game

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val _markers = MutableStateFlow<List<Game>>(emptyList())
    val markers: StateFlow<List<Game>> get() = _markers

    fun updateMarkers(newMarkers: List<Game>) {
        _markers.value = newMarkers
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MapViewModel(application)
            }
        }
    }
}