package com.questua.app.domain.usecase.admin.selectors

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuestPointsSelectorUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(): Flow<Resource<List<QuestPoint>>> {
        return repository.getAllQuestPoints(page = 0, size = 100)
    }
}