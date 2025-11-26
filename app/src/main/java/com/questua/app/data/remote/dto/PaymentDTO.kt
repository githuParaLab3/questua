package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequestDTO(
    val userId: String,
    val productId: String,
    val amountCents: Int,
    val currency: String = "BRL"
)

@Serializable
data class PaymentResponseDTO(
    val clientSecret: String,
    val transactionId: String
)