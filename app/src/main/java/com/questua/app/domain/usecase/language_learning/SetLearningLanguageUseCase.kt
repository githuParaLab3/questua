package com.questua.app.domain.usecase.language_learning

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetLearningLanguageUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(userId: String, languageId: String): Flow<Resource<UserLanguage>> {
        return repository.setLearningLanguage(userId, languageId)
    }
}