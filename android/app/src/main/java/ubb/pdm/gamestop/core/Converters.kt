package ubb.pdm.gamestop.core

import androidx.room.TypeConverter
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.json.JSONObject
import ubb.pdm.gamestop.domain.data.game.Location
import java.lang.reflect.Type

class Converters {
    // Location Converters
    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return location?.let {
            JSONObject()
                .put("latitude", it.latitude)
                .put("longitude", it.longitude)
                .toString()
        }
    }

    @TypeConverter
    fun toLocation(locationString: String?): Location? {
        return locationString?.let {
            try {
                val json = JSONObject(it)
                Location(
                    latitude = json.getDouble("latitude"),
                    longitude = json.getDouble("longitude")
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

// ObjectID deserializer from MongoDB
class ObjectIdDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String {
        return json?.asJsonObject?.get("_id")?.asString ?: ""
    }
}
