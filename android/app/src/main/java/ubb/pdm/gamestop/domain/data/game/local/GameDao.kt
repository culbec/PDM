package ubb.pdm.gamestop.domain.data.game.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ubb.pdm.gamestop.domain.data.game.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    fun getAll(): Flow<List<Game>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: Game)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(games: List<Game>)

    @Update
    suspend fun update(game: Game): Int

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM games")
    suspend fun deleteAll()
}