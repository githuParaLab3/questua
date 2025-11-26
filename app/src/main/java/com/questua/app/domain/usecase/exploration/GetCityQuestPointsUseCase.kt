package com.questua.app.domain.usecase.exploration

import com.questua.app.domain.repository.ContentRepository
import javax.inject.Inject

class GetCityQuestPointsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(cityId: String) = repository.getQuestPoints(cityId)
}