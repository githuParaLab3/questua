package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.RegisterResponseDTO
import com.questua.app.data.remote.dto.UserAccountResponseDTO
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.UserAccount

// Mapper existente (para UserAccountResponseDTO)
fun UserAccountResponseDTO.toDomain(): UserAccount {
    return UserAccount(
        id = this.id,
        email = this.email,
        displayName = this.displayName,
        avatarUrl = this.avatarUrl,
        nativeLanguageId = this.nativeLanguageId,
        role = this.userRole,
        createdAt = this.createdAt,
        lastActiveAt = this.lastActiveAt
    )
}

fun RegisterResponseDTO.toDomain(): UserAccount {
    return UserAccount(
        id = this.id,
        email = this.email,
        displayName = this.displayName,
        avatarUrl = this.avatarUrl,
        nativeLanguageId = this.nativeLanguageId,
        role = UserRole.USER,
        createdAt = "",
        lastActiveAt = null
    )
}