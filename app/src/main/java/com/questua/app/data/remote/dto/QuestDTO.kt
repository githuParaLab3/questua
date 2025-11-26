package com.questua.app.data.remote.dto

import com.questua.app.domain.model.LearningFocus
import com.questua.app.domain.model.UnlockRequirement
import kotlinx.serialization.Serializable

@Serializable
data class QuestRequestDTO(
    val questPointId: String,
    val firstDialogueId: String? = null,
    val title: String,
    val descriptionQuest: String,
    val difficulty: Short = 1,
    val orderIndex: Short = 1,
    val xpValue: Int = 0,
    val unlockRequirement: UnlockRequirement? = null,
    val learningFocus: LearningFocus? = null,
    val isPremium: Boolean = false,
    val isAiGenerated: Boolean = false,
    val isPublished: Boolean = false
)

@Serializable
data class QuestResponseDTO(
    val id: String,
    val questPointId: String,
    val firstDialogueId: String?,
    val title: String,
    val descriptionQuest: String?,
    val difficulty: Short,
    val orderIndex: Short,
    val xpValue: Int,
    val unlockRequirement: UnlockRequirement?,
    val learningFocus: LearningFocus?,
    val isPremium: Boolean,
    val isAiGenerated: Boolean,
    val isPublished: Boolean,
    val createdAt: String,
    val isLocked: Boolean = false,
    val lockMessage: String? = null
)