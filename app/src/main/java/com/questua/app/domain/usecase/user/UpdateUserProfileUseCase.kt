package com.questua.app.domain.usecase.user

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(
        user: UserAccount,
        password: String? = null,
        avatarFile: File? = null
    ): Flow<Resource<UserAccount>> {
        return repository.updateUserProfile(user, password, avatarFile)
    }
}