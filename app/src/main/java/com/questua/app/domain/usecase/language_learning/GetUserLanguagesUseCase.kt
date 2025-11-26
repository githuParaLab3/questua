package com.questua.app.domain.usecase.language_learning

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserLanguagesUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<UserLanguage>>> {
        return repository.getUserLanguages(userId)
    }
}