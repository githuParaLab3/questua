package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.ProgressStatus
import com.questua.app.domain.model.Response
import com.questua.app.domain.model.SkillAssessment
import kotlinx.serialization.Serializable

@Serializable
data class UserQuestRequestDTO(
    val userId: String,
    val questId: String,
    val progressStatus: ProgressStatus,
    val xpEarned: Int,
    val score: Int,
    val percentComplete: Double,
    val lastDialogueId: String,
    val lastActivityAt: String,
    val completedAt: String? = null,
    val responses: List<Response>? = null,
    val overallAssessment: List<SkillAssessment>? = null,
    val startedAt: String? = null
)

@Serializable
data class UserQuestResponseDTO(
    val id: String,
    val userId: String,
    val questId: String,
    val progressStatus: ProgressStatus,
    val xpEarned: Int,
    val score: Int,
    val percentComplete: Double,
    val lastDialogueId: String,
    val lastActivityAt: String,
    val completedAt: String?,
    val responses: List<Response>?,
    val overallAssessment: List<SkillAssessment>?,
    val startedAt: String?
)

@Serializable
data class SubmitResponseRequestDTO(
    val dialogueId: String,
    val answer: String
)

@Serializable
data class SubmitResponseResultDTO(
    val correct: Boolean,
    val feedback: String?,
    val nextDialogueId: String?,
    val xpEarned: Int,
    val isQuestCompleted: Boolean,
    val userQuest: UserQuestResponseDTO
)