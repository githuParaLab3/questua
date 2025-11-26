package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.CharacterEntityRequestDTO
import com.questua.app.data.remote.dto.CharacterEntityResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface CharacterEntityApi {
    @GET("api/characters")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<CharacterEntityResponseDTO>>

    @GET("api/characters/{id}")
    suspend fun getById(@Path("id") id: String): Response<CharacterEntityResponseDTO>

    @POST("api/characters")
    suspend fun create(@Body dto: CharacterEntityRequestDTO): Response<CharacterEntityResponseDTO>

    @PUT("api/characters/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: CharacterEntityRequestDTO): Response<CharacterEntityResponseDTO>

    @DELETE("api/characters/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}