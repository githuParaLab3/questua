package com.questua.app.domain.usecase.exploration

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UnlockContentUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(contentId: String, type: String): Flow<Resource<Boolean>> {
        return repository.unlockContent(contentId, type)
    }
}