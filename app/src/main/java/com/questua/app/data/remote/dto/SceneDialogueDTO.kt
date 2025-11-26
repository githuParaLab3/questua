package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.CharacterState
import com.questua.app.domain.model.Choice
import com.questua.app.domain.model.SceneEffect
import kotlinx.serialization.Serializable

@Serializable
data class SceneDialogueRequestDTO(
    val descriptionDialogue: String,
    val backgroundUrl: String,
    val bgMusicUrl: String? = null,
    val characterStates: List<CharacterState>? = null,
    val sceneEffects: List<SceneEffect>? = null,
    val speakerCharacterId: String? = null,
    val textContent: String,
    val audioUrl: String? = null,
    val expectsUserResponse: Boolean = false,
    val inputMode: InputMode,
    val expectedResponse: String? = null,
    val choices: List<Choice>? = null,
    val nextDialogueId: String? = null,
    val isAiGenerated: Boolean = false
)

@Serializable
data class SceneDialogueResponseDTO(
    val id: String,
    val descriptionDialogue: String,
    val backgroundUrl: String,
    val bgMusicUrl: String?,
    val characterStates: List<CharacterState>?,
    val sceneEffects: List<SceneEffect>?,
    val speakerCharacterId: String?,
    val textContent: String,
    val audioUrl: String?,
    val expectsUserResponse: Boolean,
    val inputMode: InputMode,
    val expectedResponse: String?,
    val choices: List<Choice>?,
    val nextDialogueId: String?,
    val isAiGenerated: Boolean,
    val createdAt: String
)