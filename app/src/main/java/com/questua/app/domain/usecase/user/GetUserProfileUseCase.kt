package com.questua.app.domain.usecase.user

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Resource<UserAccount>> {
        return repository.getUserProfile(userId)
    }
}