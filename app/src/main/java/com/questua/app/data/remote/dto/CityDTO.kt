package com.questua.app.data.remote.dto

import com.questua.app.domain.model.BoundingPolygon
import com.questua.app.domain.model.UnlockRequirement
import kotlinx.serialization.Serializable

@Serializable
data class CityRequestDTO(
    val cityName: String,
    val countryCode: String,
    val descriptionCity: String,
    val languageId: String,
    val boundingPolygon: BoundingPolygon? = null,
    val lat: Double,
    val lon: Double,
    val imageUrl: String? = null,
    val iconUrl: String? = null,
    val isPremium: Boolean = false,
    val unlockRequirement: UnlockRequirement? = null,
    val isAiGenerated: Boolean = false,
    val isPublished: Boolean = false
)

@Serializable
data class CityResponseDTO(
    val id: String,
    val cityName: String,
    val countryCode: String,
    val descriptionCity: String,
    val languageId: String,
    val boundingPolygon: BoundingPolygon?,
    val lat: Double,
    val lon: Double,
    val imageUrl: String?,
    val iconUrl: String?,
    val isPremium: Boolean,
    val unlockRequirement: UnlockRequirement?,
    val isAiGenerated: Boolean,
    val isPublished: Boolean,
    val createdAt: String,
    val isLocked: Boolean = false,
    val lockMessage: String? = null
)