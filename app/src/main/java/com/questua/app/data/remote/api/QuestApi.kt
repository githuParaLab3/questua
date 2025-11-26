package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.QuestRequestDTO
import com.questua.app.data.remote.dto.QuestResponseDTO
import retrofit2.http.*

interface QuestApi {

    @GET("api/quests")
    suspend fun getAll(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<QuestResponseDTO>

    @GET("api/quests/{id}")
    suspend fun getById(@Path("id") id: String): QuestResponseDTO

    @GET("api/quests/point/{questPointId}")
    suspend fun getByQuestPoint(
        @Path("questPointId") questPointId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<QuestResponseDTO>

    @POST("api/quests")
    suspend fun create(@Body dto: QuestRequestDTO): QuestResponseDTO

    @PUT("api/quests/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: QuestRequestDTO
    ): QuestResponseDTO

    @DELETE("api/quests/{id}")
    suspend fun delete(@Path("id") id: String)

    @PUT("api/quests/{id}/sync-xp")
    suspend fun syncXp(@Path("id") id: String): QuestResponseDTO
}