package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.ReportApi
import com.questua.app.data.remote.api.UserAccountApi
import com.questua.app.data.remote.api.UserAchievementApi
import com.questua.app.data.remote.api.UserLanguageApi
import com.questua.app.data.remote.dto.ReportRequestDTO
import com.questua.app.data.remote.dto.UserAccountRequestDTO
import com.questua.app.data.remote.dto.UserLanguageRequestDTO
import com.questua.app.domain.enums.ReportType
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.model.UserAchievement
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserAccountApi,
    private val userLanguageApi: UserLanguageApi,
    private val achievementApi: UserAchievementApi,
    private val reportApi: ReportApi
) : UserRepository, SafeApiCall() {

    override fun getUserProfile(userId: String): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userApi.getById(userId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar perfil"))
        }
    }

    override fun updateUserProfile(user: UserAccount): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())
        val dto = UserAccountRequestDTO(
            email = user.email,
            displayName = user.displayName,
            password = "",
            avatarUrl = user.avatarUrl,
            nativeLanguageId = user.nativeLanguageId,
            userRole = user.role
        )
        val result = safeApiCall { userApi.update(user.id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar"))
    }

    override fun changePassword(userId: String, currentPass: String, newPass: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
    }

    override fun getUserStats(userId: String): Flow<Resource<UserLanguage>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userLanguageApi.getByUserId(userId) }
        if (result is Resource.Success && result.data!!.content.isNotEmpty()) {
            emit(Resource.Success(result.data!!.content.first().toDomain()))
        } else {
            emit(Resource.Error("Estatísticas não encontradas"))
        }
    }

    override fun toggleAdminMode(userId: String, enabled: Boolean): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        val get = safeApiCall { userApi.getById(userId) }
        if (get is Resource.Success) {
            val user = get.data!!
            val newRole = if (enabled) UserRole.ADMIN else UserRole.USER
            val dto = UserAccountRequestDTO(user.email, user.displayName, "", user.avatarUrl, user.nativeLanguageId, newRole)
            val update = safeApiCall { userApi.update(userId, dto) }
            if (update is Resource.Success) emit(Resource.Success(true))
            else emit(Resource.Error("Erro"))
        } else {
            emit(Resource.Error("User not found"))
        }
    }

    override fun getAllUsers(): Flow<Resource<List<UserAccount>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userApi.list() }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        else emit(Resource.Error(result.message ?: "Erro"))
    }

    override fun setLearningLanguage(userId: String, languageId: String): Flow<Resource<UserLanguage>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall {
            userLanguageApi.create(UserLanguageRequestDTO(userId, languageId, startedAt = Instant.now().toString()))
        }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error("Erro"))
    }

    override fun getUserLanguages(userId: String): Flow<Resource<List<UserLanguage>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userLanguageApi.getByUserId(userId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar idiomas"))
        }
    }

    override fun abandonLanguage(userLanguageId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userLanguageApi.delete(userLanguageId) }
        if (result is Resource.Success) {
            emit(Resource.Success(true))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao abandonar idioma"))
        }
    }

    override fun startNewLanguage(userId: String, languageId: String): Flow<Resource<UserLanguage>> = flow {
        emit(Resource.Loading())
        val dto = UserLanguageRequestDTO(
            userId = userId,
            languageId = languageId,
            startedAt = Instant.now().toString()
        )
        val result = safeApiCall { userLanguageApi.create(dto) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao iniciar idioma"))
        }
    }

    override fun resumeLanguage(userLanguageId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Success(true))
    }

    override fun getUserAchievements(userId: String): Flow<Resource<List<UserAchievement>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { achievementApi.listByUser(userId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar conquistas"))
        }
    }

    override fun sendReport(
        userId: String,
        type: String,
        description: String,
        screenshotUrl: String?
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        val reportType = try {
            ReportType.valueOf(type)
        } catch (e: Exception) {
            ReportType.ERROR // CORRIGIDO: Fallback para ERROR, pois BUG não existe
        }

        val dto = ReportRequestDTO(userId, reportType, description, screenshotUrl)
        val result = safeApiCall { reportApi.create(dto) }
        if (result is Resource.Success) emit(Resource.Success(true))
        else emit(Resource.Error(result.message ?: "Erro ao enviar report"))
    }
}