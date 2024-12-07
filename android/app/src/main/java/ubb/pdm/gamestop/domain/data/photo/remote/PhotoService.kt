package ubb.pdm.gamestop.domain.data.photo.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import ubb.pdm.gamestop.domain.data.photo.Photo

interface PhotoService {
    @GET("/gamestop/api/photos/{user_id}")
    suspend fun getForUser(
        @Header("Authorization") authorization: String,
        @Path("user_id") userId: String
    ): List<Photo>

    @POST("/gamestop/api/photos")
    suspend fun create(
        @Header("Authorization") authorization: String,
        @Body photo: Photo
    ): Photo

    @DELETE("/gamestop/api/photos/{filepath}")
    suspend fun delete(
        @Header("Authorization") authorization: String,
        @Path("filepath") filePath: String
    ): Photo
}