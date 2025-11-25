package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestPoint(
    val id: String,
    val cityId: String,
    val title: String,
    val description: String,
    val difficulty: Int,
    val lat: Double,
    val lon: Double,
    val imageUrl: String? = null,
    val iconUrl: String? = null,
    val unlockRequirement: UnlockRequirement? = null,
    val isPremium: Boolean,
    val isAiGenerated: Boolean,
    val isPublished: Boolean,
    val createdAt: String
)