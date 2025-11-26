package com.questua.app.domain.usecase.user

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ToggleAdminModeUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(userId: String, enabled: Boolean): Flow<Resource<Boolean>> {
        return repository.toggleAdminMode(userId, enabled)
    }
}