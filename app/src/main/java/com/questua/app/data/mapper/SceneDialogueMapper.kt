package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.SceneDialogueResponseDTO
import com.questua.app.domain.model.SceneDialogue

fun SceneDialogueResponseDTO.toDomain(): SceneDialogue {
    return SceneDialogue(
        id = this.id,
        description = this.descriptionDialogue,
        backgroundUrl = this.backgroundUrl,
        bgMusicUrl = this.bgMusicUrl,
        characterStates = this.characterStates,
        sceneEffects = this.sceneEffects,
        speakerCharacterId = this.speakerCharacterId,
        textContent = this.textContent,
        audioUrl = this.audioUrl,
        expectsUserResponse = this.expectsUserResponse,
        inputMode = this.inputMode,
        expectedResponse = this.expectedResponse,
        choices = this.choices,
        nextDialogueId = this.nextDialogueId,
        isAiGenerated = this.isAiGenerated,
        createdAt = this.createdAt
    )
}