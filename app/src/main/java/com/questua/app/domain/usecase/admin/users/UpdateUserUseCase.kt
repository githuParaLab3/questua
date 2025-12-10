package com.questua.app.domain.usecase.admin.users

import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(
        id: String,
        email: String,
        displayName: String,
        nativeLanguageId: String,
        role: UserRole,
        password: String? = null
    ): Flow<Resource<UserAccount>> {
        return repository.updateUser(id, email, displayName, nativeLanguageId, role, password)
    }
}