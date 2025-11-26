package com.questua.app.domain.usecase.gameplay

import com.questua.app.domain.repository.ContentRepository
import javax.inject.Inject

class LoadSceneEngineUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(dialogueId: String) = repository.getSceneDialogue(dialogueId)
}