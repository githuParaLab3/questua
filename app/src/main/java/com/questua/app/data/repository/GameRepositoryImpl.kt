package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.QuestApi
import com.questua.app.data.remote.api.SceneDialogueApi
import com.questua.app.data.remote.api.UserQuestApi
import com.questua.app.data.remote.dto.SubmitResponseRequestDTO
import com.questua.app.data.remote.dto.UserQuestRequestDTO
import com.questua.app.domain.enums.ProgressStatus
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.SceneDialogue
import com.questua.app.domain.model.UserQuest
import com.questua.app.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val questApi: QuestApi,
    private val userQuestApi: UserQuestApi,
    private val dialogueApi: SceneDialogueApi
) : GameRepository, SafeApiCall() {

    override fun getQuestById(questId: String): Flow<Resource<Quest>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { questApi.getById(questId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar quest"))
        }
    }

    override fun startQuest(userId: String, questId: String): Flow<Resource<UserQuest>> = flow {
        emit(Resource.Loading())
        // Tenta buscar existente primeiro
        val existing = safeApiCall { userQuestApi.getByQuest(questId) }

        if (existing is Resource.Success && existing.data!!.content.isNotEmpty()) {
            emit(Resource.Success(existing.data!!.content.first().toDomain()))
        } else {
            // Cria novo
            val questResult = safeApiCall { questApi.getById(questId) }
            val firstDialogueId = if(questResult is Resource.Success) questResult.data!!.firstDialogueId else null

            if (firstDialogueId == null) {
                emit(Resource.Error("Quest sem diálogo inicial"))
                return@flow
            }

            val dto = UserQuestRequestDTO(
                userId = userId,
                questId = questId,
                progressStatus = ProgressStatus.IN_PROGRESS,
                xpEarned = 0,
                score = 0,
                percentComplete = 0.0,
                lastDialogueId = firstDialogueId,
                lastActivityAt = Instant.now().toString()
            )

            val createResult = safeApiCall { userQuestApi.create(dto) }
            if (createResult is Resource.Success) {
                emit(Resource.Success(createResult.data!!.toDomain()))
            } else {
                emit(Resource.Error(createResult.message ?: "Erro ao iniciar quest"))
            }
        }
    }

    override fun getUserQuestStatus(questId: String, userId: String): Flow<Resource<UserQuest>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userQuestApi.getByQuest(questId) }
        if (result is Resource.Success && result.data!!.content.isNotEmpty()) {
            emit(Resource.Success(result.data!!.content.first().toDomain()))
        } else {
            emit(Resource.Error("Progresso não encontrado"))
        }
    }

    override fun getDialogue(dialogueId: String): Flow<Resource<SceneDialogue>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { dialogueApi.getById(dialogueId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar cena"))
        }
    }

    override fun submitResponse(userQuestId: String, dialogueId: String, answer: String): Flow<Resource<UserQuest>> = flow {
        emit(Resource.Loading())
        try {
            val request = SubmitResponseRequestDTO(dialogueId, answer)
            val result = safeApiCall { userQuestApi.submitResponse(userQuestId, request) }

            if (result is Resource.Success) {
                // O DTO wrapper tem flags auxiliares, mas vamos ignorar e pegar só o objeto de domínio
                val dtoWrapper = result.data!!

                // Mapeia o UserQuestDTO interno para o UserQuest de domínio
                val domainObject = dtoWrapper.userQuest?.toDomain()

                if (domainObject != null) {
                    emit(Resource.Success(domainObject))
                } else {
                    emit(Resource.Error("Resposta do servidor incompleta"))
                }
            } else {
                emit(Resource.Error(result.message ?: "Erro desconhecido"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de processamento"))
        }
    }

    override fun retryQuest(userQuestId: String): Flow<Resource<UserQuest>> = flow {
        emit(Resource.Loading())
        // Mock implementação: no real, chamaria um endpoint de reset
        val result = safeApiCall { userQuestApi.getById(userQuestId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error("Erro"))
        }
    }

    override fun getNextDialogue(userQuestId: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userQuestApi.getById(userQuestId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.lastDialogueId))
        } else {
            emit(Resource.Error("Erro"))
        }
    }

    override fun completeQuest(userQuestId: String): Flow<Resource<UserQuest>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userQuestApi.getById(userQuestId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error("Erro"))
        }
    }
}