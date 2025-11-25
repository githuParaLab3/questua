package com.questua.app.domain.model

import com.questua.app.domain.enums.TargetType
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val sku: String,
    val title: String,
    val description: String? = null,
    val priceCents: Int,
    val currency: String,
    val targetType: TargetType,
    val targetId: String,
    val createdAt: String
)