package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.CityRequestDTO
import com.questua.app.data.remote.dto.CityResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface CityApi {
    @GET("cities")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("userId") userId: String? = null
    ): Response<PageResponse<CityResponseDTO>>

    @GET("cities/{id}")
    suspend fun getById(@Path("id") id: String): Response<CityResponseDTO>

    @POST("cities")
    suspend fun create(@Body dto: CityRequestDTO): Response<CityResponseDTO>

    @PUT("cities/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: CityRequestDTO): Response<CityResponseDTO>

    @DELETE("cities/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}