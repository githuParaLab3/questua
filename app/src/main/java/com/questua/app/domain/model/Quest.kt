package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Quest(
    val id: String,
    val questPointId: String,
    val firstDialogueId: String? = null,
    val title: String,
    val description: String,
    val difficulty: Int,
    val orderIndex: Int,
    val xpValue: Int,
    val unlockRequirement: UnlockRequirement? = null,
    val learningFocus: LearningFocus? = null,
    val isPremium: Boolean,
    val isAiGenerated: Boolean,
    val isPublished: Boolean,
    val createdAt: String
)

@Serializable
data class LearningFocus(
    val grammarTopics: List<String>? = null,
    val vocabularyThemes: List<String>? = null,
    val skills: List<String>? = null
)