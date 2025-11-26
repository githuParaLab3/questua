package com.questua.app.data.remote.dto

import com.questua.app.domain.model.UnlockRequirement
import kotlinx.serialization.Serializable

@Serializable
data class QuestPointRequestDTO(
    val cityId: String,
    val title: String,
    val descriptionQpoint: String,
    val difficulty: Short = 1,
    val lat: Double,
    val lon: Double,
    val imageUrl: String? = null,
    val iconUrl: String? = null,
    val unlockRequirement: UnlockRequirement? = null,
    val isPremium: Boolean = false,
    val isAiGenerated: Boolean = false,
    val isPublished: Boolean = false
)

@Serializable
data class QuestPointResponseDTO(
    val id: String,
    val cityId: String,
    val title: String,
    val descriptionQpoint: String,
    val difficulty: Short,
    val lat: Double?,
    val lon: Double?,
    val imageUrl: String?,
    val iconUrl: String?,
    val unlockRequirement: UnlockRequirement?,
    val isPremium: Boolean,
    val isAiGenerated: Boolean,
    val isPublished: Boolean,
    val createdAt: String,
    val isLocked: Boolean = false,
    val lockMessage: String? = null
)