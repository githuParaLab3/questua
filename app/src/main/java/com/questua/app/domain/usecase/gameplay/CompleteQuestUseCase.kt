package com.questua.app.domain.usecase.gameplay

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserQuest
import com.questua.app.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CompleteQuestUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(userQuestId: String): Flow<Resource<UserQuest>> {
        return repository.completeQuest(userQuestId)
    }
}