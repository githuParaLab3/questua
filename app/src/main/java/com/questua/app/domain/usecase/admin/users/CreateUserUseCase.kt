package com.questua.app.domain.usecase.admin.users

import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(
        email: String,
        displayName: String,
        password: String,
        nativeLanguageId: String,
        role: UserRole,
        avatarFile: File? = null
    ): Flow<Resource<UserAccount>> {
        return repository.createUser(email, displayName, password, nativeLanguageId, role, avatarFile)
    }
}