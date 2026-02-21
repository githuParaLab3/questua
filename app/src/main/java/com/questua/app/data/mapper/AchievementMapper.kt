package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.AchievementRequestDTO
import com.questua.app.data.remote.dto.AchievementResponseDTO
import com.questua.app.domain.model.Achievement

fun AchievementResponseDTO.toDomain(): Achievement {
    return Achievement(
        id = id,
        keyName = keyName,
        name = nameAchievement,
        description = descriptionAchievement,
        iconUrl = iconUrl,
        xpReward = xpReward,
        rarity = rarity,
        isHidden = isHidden,
        category = category,
        isGlobal = isGlobal,
        languageId = languageId,
        conditionType = conditionType,
        targetId = targetId,
        requiredAmount = requiredAmount,
        metadata = metadata,
        createdAt = createdAt
    )
}
