package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterEntity(
    val id: String,
    val name: String,
    val persona: Persona? = null,
    val avatarUrl: String,
    val spriteSheet: SpriteSheet? = null,
    val voiceUrl: String? = null,
    val isAiGenerated: Boolean,
    val createdAt: String
)


@Serializable
data class Persona(
    val description: String? = null,
    val traits: List<String> = emptyList(),
    val speakingStyle: String? = null,
    val voiceTone: String? = null,
    val background: String? = null
)

@Serializable
data class SpriteSheet(
    val urls: List<String> = emptyList()
)