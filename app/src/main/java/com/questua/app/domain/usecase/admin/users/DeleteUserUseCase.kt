package com.questua.app.domain.usecase.admin.users

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(userId: String): Flow<Resource<Unit>> {
        return repository.deleteUser(userId)
    }
}