package com.questua.app.domain.usecase.user

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    // Retorna o UserLanguage, que contém xpTotal, gamificationLevel, etc.
    operator fun invoke(userId: String): Flow<Resource<UserLanguage>> {
        return repository.getUserStats(userId)
    }
}