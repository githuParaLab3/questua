package com.questua.app.domain.usecase.exploration

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.repository.ContentRepository
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetWorldMapUseCase @Inject constructor(
    private val contentRepository: ContentRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String, languageId: String): Flow<Resource<List<City>>> {
        return contentRepository.getCities(languageId)
    }
}