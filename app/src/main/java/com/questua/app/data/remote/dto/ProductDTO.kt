package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.TargetType
import kotlinx.serialization.Serializable

@Serializable
data class ProductRequestDTO(
    val sku: String,
    val title: String,
    val descriptionProduct: String? = null,
    val priceCents: Int,
    val currency: String? = "BRL",
    val targetType: TargetType,
    val targetId: String
)

@Serializable
data class ProductResponseDTO(
    val id: String,
    val sku: String,
    val title: String,
    val descriptionProduct: String?,
    val priceCents: Int,
    val currency: String,
    val targetType: TargetType,
    val targetId: String,
    val createdAt: String
)