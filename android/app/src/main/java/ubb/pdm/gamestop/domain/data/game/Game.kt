package ubb.pdm.gamestop.domain.data.game

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import ubb.pdm.gamestop.core.Converters

// Sync status
enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    ERROR
}

@Entity(tableName = "games")
@TypeConverters(Converters::class)
data class Game(
    @PrimaryKey
    @SerializedName("_id") val id: String = "",
    val username: String = "",
    val title: String = "GameStop",
    @SerializedName("release_date") val releaseDate: String = "2024-12-01T12:00:00.000Z",
    @SerializedName("rental_price") val rentalPrice: Float = 20.5f,
    val rating: Int = 5,
    val category: String = GameCategory.Action.value,
    @TypeConverters(Converters::class) val location: Location = Location(0.0, 0.0),
    val date: String? = null,
    val version: Int? = null,
    // Sync metadata
    @ColumnInfo(name = "is_dirty") val isDirty: Boolean = false,
    @ColumnInfo(name = "sync_status") val syncStatus: SyncStatus = SyncStatus.PENDING,
    @ColumnInfo(name = "last_sync") val lastSync: Long? = null
)

data class Location(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

enum class GameCategory(val value: String) {
    Action("Action"),
    Adventure("Adventure"),
    Fighting("Fighting"),
    Misc("Misc"),
    Platform("Platform"),
    Puzzle("Puzzle"),
    Racing("Racing"),
    RolePlaying("Role-Playing"),
    Shooter("Shooter"),
    Simulation("Simulation"),
    Sports("Sports"),
    Strategy("Strategy"),
}

//val GameSaver: Saver<Game, Any> = mapSaver(
//    save = { game ->
//        mapOf(
//            "id" to game.id,
//            "username" to game.username,
//            "title" to game.title,
//            "releaseDate" to game.releaseDate,
//            "rentalPrice" to game.rentalPrice,
//            "rating" to game.rating,
//            "category" to game.category,
//            "location" to game.location.let {
//                mapOf("latitude" to it.latitude, "longitude" to it.longitude)
//            },
//            "date" to game.date,
//            "version" to game.version
//        )
//    },
//    restore = { map ->
//        Game(
//            id = map["id"] as String,
//            username = map["username"] as String,
//            title = map["title"] as String,
//            releaseDate = map["releaseDate"] as String,
//            rentalPrice = map["rentalPrice"] as Float,
//            rating = map["rating"] as Int,
//            category = map["category"] as String,
//            location = (map["location"] as? Map<*, *>).let {
//                Location(it?.get("latitude")!! as Double, it["longitude"]!! as Double)
//            },
//            date = map["date"] as? String,
//            version = map["version"] as? Int
//        )
//    }
//)
