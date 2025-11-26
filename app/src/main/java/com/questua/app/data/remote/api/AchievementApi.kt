package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.AchievementRequestDTO
import com.questua.app.data.remote.dto.AchievementResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface AchievementApi {
    @GET("achievements")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<AchievementResponseDTO>>

    @GET("achievements/{id}")
    suspend fun getById(@Path("id") id: String): Response<AchievementResponseDTO>

    @POST("achievements")
    suspend fun create(@Body dto: AchievementRequestDTO): Response<AchievementResponseDTO>

    @PUT("achievements/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: AchievementRequestDTO): Response<AchievementResponseDTO>

    @DELETE("achievements/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}