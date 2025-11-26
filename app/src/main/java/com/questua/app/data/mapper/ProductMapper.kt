package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.ProductResponseDTO
import com.questua.app.domain.model.Product

fun ProductResponseDTO.toDomain(): Product {
    return Product(
        id = this.id,
        sku = this.sku,
        title = this.title,
        description = this.descriptionProduct,
        priceCents = this.priceCents,
        currency = this.currency,
        targetType = this.targetType,
        targetId = this.targetId,
        createdAt = this.createdAt
    )
}