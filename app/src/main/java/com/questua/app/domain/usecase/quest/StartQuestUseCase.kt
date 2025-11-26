package com.questua.app.domain.usecase.quest

import com.questua.app.domain.repository.GameRepository
import javax.inject.Inject

class StartQuestUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(userId: String, questId: String) = repository.startQuest(userId, questId)
}