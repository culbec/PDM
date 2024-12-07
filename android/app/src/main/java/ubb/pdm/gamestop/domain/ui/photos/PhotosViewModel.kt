package ubb.pdm.gamestop.domain.ui.photos

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ubb.pdm.gamestop.App
import ubb.pdm.gamestop.core.TAG
import ubb.pdm.gamestop.core.data.UserPreferencesRepository
import ubb.pdm.gamestop.domain.data.photo.Photo
import ubb.pdm.gamestop.domain.data.photo.PhotoRepository

data class PhotosState(
    val isLoading: Boolean = false
)

class PhotosViewModel(
    private val photoRepository: PhotoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val photosStream: Flow<List<Photo>> = photoRepository.photoStream

    private val _photosState = mutableStateOf(PhotosState())
    val photosState = _photosState

    init {
        Log.d(TAG, "init")
        this.loadPhotos()
    }

    private fun loadPhotos() {
        Log.d(TAG, "load photos...")
        _photosState.value = _photosState.value.copy(isLoading = true)

        viewModelScope.launch {
            val userId = userPreferencesRepository.get("user_id")

            if (userId.isNotEmpty()) {
                photoRepository.refresh(userId)
            }

            _photosState.value = _photosState.value.copy(isLoading = false)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                PhotosViewModel(
                    app.container.photoRepository,
                    app.container.userPreferencesRepository
                )
            }
        }
    }
}