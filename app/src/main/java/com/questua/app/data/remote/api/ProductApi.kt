package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.ProductRequestDTO
import com.questua.app.data.remote.dto.ProductResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    @GET("api/products")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<ProductResponseDTO>>

    @GET("api/products/{id}")
    suspend fun getById(@Path("id") id: String): Response<ProductResponseDTO>

    @POST("api/products")
    suspend fun create(@Body dto: ProductRequestDTO): Response<ProductResponseDTO>

    @PUT("api/products/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: ProductRequestDTO): Response<ProductResponseDTO>

    @DELETE("api/products/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}