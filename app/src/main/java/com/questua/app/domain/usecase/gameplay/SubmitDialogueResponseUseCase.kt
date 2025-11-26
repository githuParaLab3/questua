package com.questua.app.domain.usecase.gameplay

import com.questua.app.domain.repository.GameRepository
import javax.inject.Inject

class SubmitDialogueResponseUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(userQuestId: String, questionId: String, answer: String) =
        repository.submitResponse(userQuestId, questionId, answer)
}