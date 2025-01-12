package ubb.pdm.gamestop.domain.data.photo

import android.util.Log
import ubb.pdm.gamestop.core.util.TAG
import ubb.pdm.gamestop.core.data.remote.Api
import ubb.pdm.gamestop.domain.data.photo.local.PhotoDao
import ubb.pdm.gamestop.domain.data.photo.remote.PhotoService

class PhotoRepository(
    private val photoService: PhotoService,
    private val photoDao: PhotoDao,
) {

    val photoStream by lazy { photoDao.getAll() }

    init {
        Log.d(TAG, "init")
    }

    suspend fun refresh(userId: String) {
        Log.d(TAG, "refresh started")

        try {
            val photos =
                photoService.getForUser(authorization = Api.getBearerToken(), userId = userId)
            photoDao.insert(photos)
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed: ${e.message}")

            // check for unauthorized
            if (e.message?.contains("401") == true) {
                Log.d(TAG, "Unauthorized, clearing token")
                Api.clearTokenAndPreferences()
            }
        }
    }

    private suspend fun handlePhotoCreated(photo: Photo) {
        Log.d(TAG, "handlePhotoCreated: $photo")
        photoDao.insert(photo)
    }

    suspend fun save(photo: Photo): Photo {
        Log.d(TAG, "save: $photo")
        val photo: Photo = photoService.create(authorization = Api.getBearerToken(), photo = photo)

        Log.d(TAG, "save photo: $photo")
        handlePhotoCreated(photo)
        return photo
    }

    private suspend fun handlePhotoDeleted(photo: Photo) {
        Log.d(TAG, "handlePhotoDeleted: $photo")
        photoDao.deleteById(photo.id)
    }

    suspend fun delete(photo: Photo): Photo {
        Log.d(TAG, "delete: $photo")
        photoService.delete(authorization = Api.getBearerToken(), filePath = photo.filepath)
        handlePhotoDeleted(photo)
        return photo
    }
}