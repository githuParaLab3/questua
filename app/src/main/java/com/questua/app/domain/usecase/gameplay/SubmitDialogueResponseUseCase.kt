package com.questua.app.domain.usecase.gameplay

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubmitDialogueResponseUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(userQuestId: String, questionId: String, answer: String): Flow<Resource<Boolean>> {
        return repository.submitResponse(userQuestId, questionId, answer)
    }
}