package com.questua.app.domain.usecase.admin.selectors

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Quest
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuestsSelectorUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(): Flow<Resource<List<Quest>>> {
        return repository.getAllQuests(page = 0, size = 100)
    }
}