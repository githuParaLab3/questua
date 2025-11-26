package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.QuestRequestDTO
import com.questua.app.data.remote.dto.QuestResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface QuestApi {
    @GET("api/quests")
    suspend fun getAll(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<QuestResponseDTO>>

    @GET("api/quests/{id}")
    suspend fun getById(@Path("id") id: String): Response<QuestResponseDTO>

    @GET("api/quests/point/{questPointId}")
    suspend fun getByQuestPoint(
        @Path("questPointId") questPointId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<QuestResponseDTO>>

    @POST("api/quests")
    suspend fun create(@Body dto: QuestRequestDTO): Response<QuestResponseDTO>

    @PUT("api/quests/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: QuestRequestDTO): Response<QuestResponseDTO>

    @DELETE("api/quests/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @PUT("api/quests/{id}/sync-xp")
    suspend fun syncXp(@Path("id") id: String): Response<QuestResponseDTO>
}