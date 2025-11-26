package com.questua.app.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UploadApi {
    @Multipart
    @POST("api/upload/archive")
    suspend fun uploadArchive(
        @Part file: MultipartBody.Part,
        @Query("folder") folder: String? = null
    ): Response<Map<String, String>>

    @DELETE("api/upload/archive")
    suspend fun deleteArchive(@Query("url") url: String): Response<Unit>
}