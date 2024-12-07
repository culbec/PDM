package ubb.pdm.gamestop.domain.data.photo.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ubb.pdm.gamestop.domain.data.photo.Photo

@Dao
interface PhotoDao {
    @Query("SELECT * FROM Photos")
    fun getAll(): Flow<List<Photo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: Photo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photos: List<Photo>)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM photos")
    suspend fun deleteAll()
}