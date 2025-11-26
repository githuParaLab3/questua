package com.questua.app.domain.model

import com.questua.app.domain.enums.ProgressStatus
import kotlinx.serialization.Serializable

@Serializable
data class UserQuest(
    val id: String,
    val userId: String,
    val questId: String,
    val status: ProgressStatus,
    val xpEarned: Int,
    val score: Int,
    val percentComplete: Float,
    val lastDialogueId: String,
    val lastActivityAt: String,
    val completedAt: String? = null,
    val responses: List<Response>? = null,
    val overallAssessment: List<SkillAssessment>? = null
)

@Serializable
data class Response(
    val questionId: String,
    val answer: String,
    val correct: Boolean? = null,
    val feedback: String? = null
)

@Serializable
data class SkillAssessment(
    val skill: String,
    val score: Int,
    val feedback: String? = null
)