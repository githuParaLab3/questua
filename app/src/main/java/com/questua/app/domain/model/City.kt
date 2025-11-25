package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val id: String,
    val name: String,
    val countryCode: String,
    val description: String,
    val languageId: String,
    val boundingPolygon: BoundingPolygon? = null,
    val lat: Double,
    val lon: Double,
    val imageUrl: String? = null,
    val iconUrl: String? = null,
    val isPremium: Boolean,
    val unlockRequirement: UnlockRequirement? = null,
    val isAiGenerated: Boolean,
    val isPublished: Boolean,
    val createdAt: String
)

@Serializable
data class BoundingPolygon(
    val coordinates: List<GeoPoint>
)

@Serializable
data class GeoPoint(
    val lat: Double,
    val lon: Double
)