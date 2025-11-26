package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.CityRequestDTO
import com.questua.app.data.remote.dto.CityResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.http.*

interface CityApi {

    @GET("api/cities")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("userId") userId: String? = null
    ): PageResponse<CityResponseDTO>

    @GET("api/cities/{id}")
    suspend fun getById(@Path("id") id: String): CityResponseDTO

    @POST("api/cities")
    suspend fun create(@Body dto: CityRequestDTO): CityResponseDTO

    @PUT("api/cities/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: CityRequestDTO
    ): CityResponseDTO

    @DELETE("api/cities/{id}")
    suspend fun delete(@Path("id") id: String)
}