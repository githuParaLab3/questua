package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PaymentRequestDTO
import com.questua.app.data.remote.dto.PaymentResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApi {
    @POST("payments/initiate")
    suspend fun initiatePayment(@Body request: PaymentRequestDTO): Response<PaymentResponseDTO>
}