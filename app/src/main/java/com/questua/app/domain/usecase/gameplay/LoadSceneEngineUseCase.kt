package com.questua.app.domain.usecase.gameplay

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.SceneDialogue
import com.questua.app.domain.repository.ContentRepository // Content repo tem getSceneDialogue
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadSceneEngineUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(dialogueId: String): Flow<Resource<SceneDialogue>> {
        return repository.getSceneDialogue(dialogueId)
    }
}