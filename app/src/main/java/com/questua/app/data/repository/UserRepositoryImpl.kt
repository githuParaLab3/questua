package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.*
import com.questua.app.data.remote.dto.ReportRequestDTO
import com.questua.app.data.remote.dto.UserAccountRequestDTO
import com.questua.app.data.remote.dto.UserLanguageRequestDTO
import com.questua.app.data.remote.dto.UserLanguageResponseDTO
import com.questua.app.domain.enums.ReportType
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.enums.StatusLanguage
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.model.UserAchievement
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.Instant
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserAccountApi,
    private val userLanguageApi: UserLanguageApi,
    private val achievementApi: UserAchievementApi,
    private val reportApi: ReportApi,
    private val uploadApi: UploadApi
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

    override fun updateUserProfile(
        user: UserAccount,
        password: String?,
        avatarFile: File?
    ): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())

        var finalAvatarUrl = user.avatarUrl

        if (avatarFile != null) {
            try {
                val requestFile = avatarFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", avatarFile.name, requestFile)

                val uploadResult = safeApiCall { uploadApi.uploadArchive(body, "avatars") }

                if (uploadResult is Resource.Success) {
                    finalAvatarUrl = uploadResult.data?.get("url") ?: user.avatarUrl
                } else {
                    emit(Resource.Error("Falha no upload da imagem: ${uploadResult.message}"))
                    return@flow
                }
            } catch (e: Exception) {
                emit(Resource.Error("Erro ao preparar imagem: ${e.message}"))
                return@flow
            }
        }

        val dto = UserAccountRequestDTO(
            email = user.email,
            displayName = user.displayName,
            password = password ?: "",
            avatarUrl = finalAvatarUrl,
            nativeLanguageId = user.nativeLanguageId,
            userRole = user.role
        )

        val result = safeApiCall { userApi.update(user.id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar"))
    }

    override fun getUserStats(userId: String): Flow<Resource<UserLanguage>> = flow {
        emit(Resource.Loading())

        val result = safeApiCall { userLanguageApi.getByUserId(userId) }

        if (result is Resource.Success && !result.data?.content.isNullOrEmpty()) {
            val userLanguages = result.data!!.content.map { it.toDomain() }

            val activeLanguage = userLanguages.firstOrNull { it.status == StatusLanguage.ACTIVE }

            if (activeLanguage != null) {
                emit(Resource.Success(activeLanguage))
            } else {
                emit(Resource.Success(userLanguages.first()))
            }
        } else {
            emit(Resource.Error("Nenhuma linguagem encontrada."))
        }
    }

    override fun getUserLanguages(userId: String): Flow<Resource<List<UserLanguage>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userLanguageApi.getByUserId(userId) }
        if (result is Resource.Success) {
            val languages = result.data?.content?.map { it.toDomain() } ?: emptyList()
            emit(Resource.Success(languages))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar idiomas"))
        }
    }

    override fun getAllUsers(): Flow<Resource<List<UserAccount>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userApi.list() }
        if (result is Resource.Success) {
            val users = result.data?.content?.map { it.toDomain() } ?: emptyList()
            emit(Resource.Success(users))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao listar usuários"))
        }
    }

    override fun changePassword(userId: String, currentPass: String, newPass: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
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
            else emit(Resource.Error("Erro ao mudar permissão"))
        } else {
            emit(Resource.Error("Usuário não encontrado"))
        }
    }

    override fun setLearningLanguage(userId: String, languageId: String): Flow<Resource<UserLanguage>> = flow {
        emit(Resource.Loading())

        val existingResult = safeApiCall { userLanguageApi.getByUserId(userId) }

        if (existingResult is Resource.Success && !existingResult.data?.content.isNullOrEmpty()) {
            val allUserLanguages = existingResult.data!!.content
            var targetLanguageDTO = allUserLanguages.firstOrNull { it.languageId == languageId }
            var successfulUpdate: UserLanguageResponseDTO? = null

            if (targetLanguageDTO == null) {
                emit(Resource.Error("Não foi possível encontrar o idioma em seus registros para ativar."))
                return@flow
            }

            for (currentLanguageDTO in allUserLanguages) {
                if (currentLanguageDTO.statusLanguage == StatusLanguage.ACTIVE && currentLanguageDTO.languageId != languageId) {
                    val deactivateDto = currentLanguageDTO.run {
                        UserLanguageRequestDTO(
                            userId = this.userId,
                            languageId = this.languageId,
                            statusLanguage = StatusLanguage.RESUMED,
                            cefrLevel = this.cefrLevel,
                            gamificationLevel = this.gamificationLevel,
                            xpTotal = this.xpTotal,
                            streakDays = this.streakDays,
                            unlockedContent = this.unlockedContent,
                            startedAt = this.startedAt,
                            lastActiveAt = Instant.now().toString()
                        )
                    }
                    safeApiCall { userLanguageApi.update(currentLanguageDTO.id, deactivateDto) }
                }
            }

            if (targetLanguageDTO.statusLanguage != StatusLanguage.ACTIVE) {
                val activateDto = targetLanguageDTO.run {
                    UserLanguageRequestDTO(
                        userId = this.userId,
                        languageId = this.languageId,
                        statusLanguage = StatusLanguage.ACTIVE,
                        cefrLevel = this.cefrLevel,
                        gamificationLevel = this.gamificationLevel,
                        xpTotal = this.xpTotal,
                        streakDays = this.streakDays,
                        unlockedContent = this.unlockedContent,
                        startedAt = this.startedAt,
                        lastActiveAt = Instant.now().toString()
                    )
                }

                val updateResult = safeApiCall { userLanguageApi.update(targetLanguageDTO.id, activateDto) }

                if (updateResult is Resource.Success) {
                    successfulUpdate = updateResult.data
                } else {
                    emit(Resource.Error(updateResult.message ?: "Erro ao definir idioma ativo"))
                    return@flow
                }
            } else {
                successfulUpdate = targetLanguageDTO
            }

            if (successfulUpdate != null) {
                emit(Resource.Success(successfulUpdate.toDomain()))
                return@flow
            }
        }

        emit(Resource.Error("Não foi possível encontrar o idioma em seus registros."))
    }

    override fun abandonLanguage(userLanguageId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userLanguageApi.delete(userLanguageId) }
        if (result is Resource.Success) emit(Resource.Success(true))
        else emit(Resource.Error(result.message ?: "Erro"))
    }

    override fun startNewLanguage(userId: String, languageId: String): Flow<Resource<UserLanguage>> = flow {
        emit(Resource.Loading())
        val dto = UserLanguageRequestDTO(userId, languageId, startedAt = Instant.now().toString())
        val result = safeApiCall { userLanguageApi.create(dto) }
        if(result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro"))
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
            emit(Resource.Error(result.message ?: "Erro"))
        }
    }

    override fun sendReport(userId: String, type: String, description: String, screenshotUrl: String?): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        var finalScreenshotUrl = screenshotUrl

        if (!screenshotUrl.isNullOrBlank()) {
            try {
                val file = File(screenshotUrl)
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val uploadResult = safeApiCall { uploadApi.uploadArchive(body, "reports") }

                    if (uploadResult is Resource.Success) {
                        finalScreenshotUrl = uploadResult.data?.get("url")
                    } else {
                        finalScreenshotUrl = null
                    }
                } else {
                    finalScreenshotUrl = null
                }
            } catch (e: Exception) {
                finalScreenshotUrl = null
            }
        }

        val reportType = try { ReportType.valueOf(type) } catch (e: Exception) { ReportType.ERROR }
        val dto = ReportRequestDTO(userId, reportType, description, finalScreenshotUrl)

        val res = safeApiCall { reportApi.create(dto) }
        if (res is Resource.Success) emit(Resource.Success(true)) else emit(Resource.Error(res.message ?: "Erro ao enviar relatório"))
    }
}