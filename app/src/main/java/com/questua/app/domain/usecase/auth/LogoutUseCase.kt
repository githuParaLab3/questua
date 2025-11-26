package com.questua.app.domain.usecase.auth

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> {
        return repository.logout()
    }
}