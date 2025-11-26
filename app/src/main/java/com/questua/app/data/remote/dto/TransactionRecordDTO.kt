package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.TransactionStatus
import com.questua.app.domain.model.TransactionMetadata
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRecordRequestDTO(
    val userId: String,
    val productId: String,
    val stripePaymentIntentId: String,
    val stripeChargeId: String? = null,
    val amountCents: Int,
    val currency: String = "BRL",
    val statusTransaction: TransactionStatus = TransactionStatus.PENDING,
    val receiptUrl: String? = null,
    val metadata: TransactionMetadata? = null,
    val completedAt: String? = null
)

@Serializable
data class TransactionRecordResponseDTO(
    val id: String,
    val userId: String,
    val productId: String,
    val stripePaymentIntentId: String,
    val stripeChargeId: String?,
    val amountCents: Int,
    val currency: String,
    val statusTransaction: TransactionStatus,
    val receiptUrl: String?,
    val metadata: TransactionMetadata?,
    val createdAt: String,
    val completedAt: String?
)