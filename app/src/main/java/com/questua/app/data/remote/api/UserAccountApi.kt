package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.UserAccountRequestDTO
import com.questua.app.data.remote.dto.UserAccountResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface UserAccountApi {
    @GET("api/users")
    suspend fun list(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<UserAccountResponseDTO>>

    @GET("api/users/{id}")
    suspend fun getById(@Path("id") id: String): Response<UserAccountResponseDTO>

    @GET("api/users/email/{email}")
    suspend fun getByEmail(@Path("email") email: String): Response<UserAccountResponseDTO>

    @POST("api/users")
    suspend fun create(@Body dto: UserAccountRequestDTO): Response<UserAccountResponseDTO>

    @PUT("api/users/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: UserAccountRequestDTO): Response<UserAccountResponseDTO>

    @DELETE("api/users/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}