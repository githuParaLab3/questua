package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.CharacterEntityResponseDTO
import com.questua.app.domain.model.CharacterEntity

fun CharacterEntityResponseDTO.toDomain(): CharacterEntity {
    return CharacterEntity(
        id = this.id,
        name = this.nameCharacter,
        persona = this.persona,
        avatarUrl = this.avatarUrl,
        spriteSheet = this.spriteSheet,
        voiceUrl = this.voiceUrl,
        isAiGenerated = this.isAiGenerated,
        createdAt = this.createdAt
    )
}