package com.questua.app.data.remote.dto

import com.questua.app.domain.model.Persona
import com.questua.app.domain.model.SpriteSheet
import kotlinx.serialization.Serializable

@Serializable
data class CharacterEntityRequestDTO(
    val nameCharacter: String,
    val persona: Persona? = null,
    val avatarUrl: String,
    val spriteSheet: SpriteSheet? = null,
    val voiceUrl: String? = null,
    val isAiGenerated: Boolean = false
)

@Serializable
data class CharacterEntityResponseDTO(
    val id: String,
    val nameCharacter: String,
    val persona: Persona?,
    val avatarUrl: String,
    val spriteSheet: SpriteSheet?,
    val voiceUrl: String?,
    val isAiGenerated: Boolean,
    val createdAt: String
)