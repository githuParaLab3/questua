package com.questua.app.domain.usecase.exploration

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCityDetailsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(cityId: String): Flow<Resource<City>> {
        return repository.getCityDetails(cityId)
    }
}