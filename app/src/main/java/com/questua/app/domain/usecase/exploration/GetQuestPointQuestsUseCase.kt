package com.questua.app.domain.usecase.exploration

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Quest
import com.questua.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuestPointQuestsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(questPointId: String): Flow<Resource<List<Quest>>> {
        return repository.getQuests(questPointId)
    }
}