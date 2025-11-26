package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.UserQuestResponseDTO
import com.questua.app.domain.model.UserQuest

fun UserQuestResponseDTO.toDomain(): UserQuest {
    return UserQuest(
        id = this.id,
        userId = this.userId,
        questId = this.questId,
        status = this.progressStatus,
        xpEarned = this.xpEarned,
        score = this.score,
        percentComplete = this.percentComplete.toFloat(),
        lastDialogueId = this.lastDialogueId,
        lastActivityAt = this.lastActivityAt,
        completedAt = this.completedAt,
        responses = this.responses,
        overallAssessment = this.overallAssessment
    )
}