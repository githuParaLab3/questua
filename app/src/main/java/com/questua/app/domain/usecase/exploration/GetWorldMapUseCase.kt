package com.questua.app.domain.usecase.exploration

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorldMapUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(languageId: String): Flow<Resource<List<City>>> {
        return repository.getCities(languageId)
    }
}