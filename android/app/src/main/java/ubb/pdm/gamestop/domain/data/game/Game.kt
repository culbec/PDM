package ubb.pdm.gamestop.domain.data.game

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import org.bson.types.ObjectId
import ubb.pdm.gamestop.core.util.Converters

// Sync status
enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    ERROR
}

enum class SyncOperation {
    NONE,
    CREATE,
    UPDATE,
    DELETE
}

@Entity(tableName = "games")
@TypeConverters(Converters::class)
data class Game(
    @PrimaryKey
    @SerializedName("_id")
    val id: String = ObjectId().toHexString(),
    val username: String = "",
    val title: String = "GameStop",
    @SerializedName("release_date") val releaseDate: String = "2024-12-01T12:00:00.000Z",
    @SerializedName("rental_price") val rentalPrice: Float = 20.5f,
    val rating: Int = 5,
    val category: String = GameCategory.Action.value,
    @TypeConverters(Converters::class) var location: Location = Location(0.0, 0.0),
    val date: String? = null,
    val version: Int? = null,
    // Sync metadata
    @ColumnInfo(name = "sync_operation") var syncOperation: SyncOperation = SyncOperation.NONE,
    @ColumnInfo(name = "sync_status") var syncStatus: SyncStatus = SyncStatus.SYNCED,
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
