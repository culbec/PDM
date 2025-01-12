package ubb.pdm.gamestop.domain.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ubb.pdm.gamestop.core.util.TAG
import java.io.File
import java.io.FileOutputStream

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey
    @SerializedName("_id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("game_id") val gameId: String,
    val filepath: String,
    var data: String,
    @Expose(deserialize = false, serialize = false) var localFilePath: String? = null,
    @Expose(deserialize = false, serialize = false) var savedLocally: Boolean = false
) {
    override fun toString(): String {
        return "Photo(id='$id', userId='$userId', gameId='$gameId', filepath='$filepath', localFilePath='$localFilePath', savedLocally='$savedLocally')"
    }
}

private fun Photo.getBitmap(): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(this.data, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "Failed to decode base64 string: ${e.message}")
        null
    }
}

fun Photo.saveImageToFile(context: Context) {
    val filename = filepath
    val file = File(context.filesDir, filename)
    val bitmap = this.getBitmap()

    if (bitmap != null) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        this.localFilePath = file.absolutePath
        this.savedLocally = true
    } else {
        Log.e(TAG, "Failed to save image: Bitmap is null")
    }
}

fun Photo.loadImageFromFile(context: Context): Bitmap? {
    // searching the file by its filepath
    val file = File(context.filesDir, filepath)

    if (file.exists()) {
        this.localFilePath = file.absolutePath
        this.savedLocally = true
        return BitmapFactory.decodeFile(file.absolutePath)
    } else {
        Log.e(TAG, "Failed to load image: File does not exist")
        return null
    }
}