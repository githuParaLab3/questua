package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.QuestPointRequestDTO
import com.questua.app.data.remote.dto.QuestPointResponseDTO
import retrofit2.http.*

interface QuestPointApi {

    @GET("api/quest-points")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("userId") userId: String? = null
    ): PageResponse<QuestPointResponseDTO>

    @GET("api/quest-points/{id}")
    suspend fun getById(@Path("id") id: String): QuestPointResponseDTO

    @POST("api/quest-points")
    suspend fun create(@Body dto: QuestPointRequestDTO): QuestPointResponseDTO

    @PUT("api/quest-points/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: QuestPointRequestDTO
    ): QuestPointResponseDTO

    @DELETE("api/quest-points/{id}")
    suspend fun delete(@Path("id") id: String)
}