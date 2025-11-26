package com.questua.app.domain.usecase.exploration

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCityQuestPointsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(cityId: String): Flow<Resource<List<QuestPoint>>> {
        return repository.getQuestPoints(cityId)
    }
}