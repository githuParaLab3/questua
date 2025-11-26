package com.questua.app.domain.usecase.exploration

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UnlockRequirement
import com.questua.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnlockPreviewUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(contentId: String, type: String): Flow<Resource<UnlockRequirement>> {
        return repository.getUnlockPreview(contentId, type)
    }
}