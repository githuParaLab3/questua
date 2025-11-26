package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.CityResponseDTO
import com.questua.app.domain.model.City

fun CityResponseDTO.toDomain(): City {
    return City(
        id = this.id,
        name = this.cityName,
        countryCode = this.countryCode,
        description = this.descriptionCity,
        languageId = this.languageId,
        boundingPolygon = this.boundingPolygon,
        lat = this.lat,
        lon = this.lon,
        imageUrl = this.imageUrl,
        iconUrl = this.iconUrl,
        isPremium = this.isPremium,
        unlockRequirement = this.unlockRequirement,
        isAiGenerated = this.isAiGenerated,
        isPublished = this.isPublished,
        createdAt = this.createdAt
    )
}