package ubb.pdm.gamestop.domain.ui.sensors

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import ubb.pdm.gamestop.core.sensors.AccelerometerSensorMonitor

class AccelerometerSensorViewModel(application: Application): AndroidViewModel(application) {
    var uiState by mutableStateOf(emptyList<Float>())
        private set

    init {
        viewModelScope.launch {
            AccelerometerSensorMonitor(getApplication()).data.collect {
                uiState = it
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AccelerometerSensorViewModel(application)
            }
        }
    }
}