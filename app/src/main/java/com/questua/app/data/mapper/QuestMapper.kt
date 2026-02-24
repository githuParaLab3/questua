package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.QuestResponseDTO
import com.questua.app.domain.model.Quest

fun QuestResponseDTO.toDomain(): Quest {
    return Quest(
        id = this.id,
        questPointId = this.questPointId,
        firstDialogueId = this.firstDialogueId,
        title = this.title,
        description = this.descriptionQuest ?: "",
        difficulty = this.difficulty.toInt(),
        orderIndex = this.orderIndex.toInt(),
        xpValue = this.xpValue,
        xpPerQuestion = this.xpPerQuestion,
        unlockRequirement = this.unlockRequirement,
        learningFocus = this.learningFocus,
        isPremium = this.isPremium,
        isAiGenerated = this.isAiGenerated,
        isPublished = this.isPublished,
        createdAt = this.createdAt
    )
}