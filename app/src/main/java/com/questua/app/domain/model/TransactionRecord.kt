package com.questua.app.domain.model

import com.questua.app.domain.enums.TransactionStatus
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRecord(
    val id: String,
    val userId: String,
    val productId: String,
    val stripePaymentIntentId: String,
    val stripeChargeId: String? = null,
    val amountCents: Int,
    val currency: String,
    val status: TransactionStatus,
    val receiptUrl: String? = null,
    val metadata: TransactionMetadata? = null,
    val createdAt: String,
    val completedAt: String? = null
)

@Serializable
data class TransactionMetadata(
    val deviceModel: String? = null,
    val androidVersion: String? = null,
    val ipAddress: String? = null,
    val notes: String? = null
)