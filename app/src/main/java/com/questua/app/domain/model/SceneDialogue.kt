package com.questua.app.domain.model

import com.questua.app.domain.enums.InputMode
import kotlinx.serialization.Serializable

@Serializable
data class SceneDialogue(
    val id: String,
    val description: String,
    val backgroundUrl: String,
    val bgMusicUrl: String? = null,
    val characterStates: List<CharacterState>? = null,
    val sceneEffects: List<SceneEffect>? = null,
    val speakerCharacterId: String? = null,
    val textContent: String,
    val audioUrl: String? = null,
    val expectsUserResponse: Boolean,
    val inputMode: InputMode,
    val expectedResponse: String? = null,
    val choices: List<Choice>? = null,
    val nextDialogueId: String? = null,
    val isAiGenerated: Boolean,
    val createdAt: String
)


@Serializable
data class CharacterState(
    val characterId: String,
    val expression: String?,
    val position: String?
)

@Serializable
data class SceneEffect(
    val type: String,
    val intensity: Double?,
    val duration: Double?,
    val soundUrl: String?
)

@Serializable
data class Choice(
    val text: String,
    val nextDialogueId: String?
)