package ubb.pdm.gamestop

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ubb.pdm.gamestop.domain.data.game.Game
import ubb.pdm.gamestop.domain.data.game.local.GameDao
import ubb.pdm.gamestop.domain.data.photo.Photo
import ubb.pdm.gamestop.domain.data.photo.local.PhotoDao

@Database(entities = [Game::class, Photo::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}