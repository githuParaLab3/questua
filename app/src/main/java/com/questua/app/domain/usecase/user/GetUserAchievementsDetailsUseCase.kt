package com.questua.app.domain.usecase.user

import com.questua.app.core.common.Resource
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.AchievementApi
import com.questua.app.domain.model.Achievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.questua.app.core.network.SafeApiCall
import javax.inject.Inject

class GetAchievementDetailsUseCase @Inject constructor(
    private val achievementApi: AchievementApi
) : SafeApiCall() {
    operator fun invoke(achievementId: String): Flow<Resource<Achievement>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { achievementApi.getById(achievementId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar detalhes da conquista"))
        }
    }
}