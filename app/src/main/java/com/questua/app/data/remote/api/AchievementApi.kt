package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.AchievementRequestDTO
import com.questua.app.data.remote.dto.AchievementResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.http.*

interface AchievementApi {

    @GET("api/achievements")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null
    ): PageResponse<AchievementResponseDTO>

    @GET("api/achievements/{id}")
    suspend fun getById(@Path("id") id: String): AchievementResponseDTO

    @POST("api/achievements")
    suspend fun create(@Body dto: AchievementRequestDTO): AchievementResponseDTO

    @PUT("api/achievements/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: AchievementRequestDTO
    ): AchievementResponseDTO

    @DELETE("api/achievements/{id}")
    suspend fun delete(@Path("id") id: String)
}