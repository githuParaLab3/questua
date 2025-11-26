package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.TransactionRecordResponseDTO
import com.questua.app.domain.model.TransactionRecord

fun TransactionRecordResponseDTO.toDomain(): TransactionRecord {
    return TransactionRecord(
        id = this.id,
        userId = this.userId,
        productId = this.productId,
        stripePaymentIntentId = this.stripePaymentIntentId,
        stripeChargeId = this.stripeChargeId,
        amountCents = this.amountCents,
        currency = this.currency,
        status = this.statusTransaction,
        receiptUrl = this.receiptUrl,
        metadata = this.metadata,
        createdAt = this.createdAt,
        completedAt = this.completedAt
    )
}