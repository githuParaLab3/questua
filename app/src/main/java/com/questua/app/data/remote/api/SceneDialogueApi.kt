package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.SceneDialogueRequestDTO
import com.questua.app.data.remote.dto.SceneDialogueResponseDTO
import retrofit2.http.*

interface SceneDialogueApi {

    @GET("api/scene-dialogues")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<SceneDialogueResponseDTO>

    @GET("api/scene-dialogues/{id}")
    suspend fun getById(@Path("id") id: String): SceneDialogueResponseDTO

    @POST("api/scene-dialogues")
    suspend fun create(@Body dto: SceneDialogueRequestDTO): SceneDialogueResponseDTO

    @PUT("api/scene-dialogues/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: SceneDialogueRequestDTO
    ): SceneDialogueResponseDTO

    @DELETE("api/scene-dialogues/{id}")
    suspend fun delete(@Path("id") id: String)
}