package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.*
import com.questua.app.data.remote.dto.CityRequestDTO
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor(
    private val cityApi: CityApi,
    private val questPointApi: QuestPointApi,
    private val questApi: QuestApi,
    private val sceneDialogueApi: SceneDialogueApi,
    private val characterEntityApi: CharacterEntityApi,
    private val userLanguageApi: UserLanguageApi
) : ContentRepository, SafeApiCall() {

    override fun getCities(languageId: String): Flow<Resource<List<City>>> = flow {
        emit(Resource.Loading())
        // Filtra por languageId no backend
        val result = safeApiCall { cityApi.list(filter = mapOf("languageId" to languageId)) }
        when (result) {
            is Resource.Success -> emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro ao buscar cidades"))
            else -> Unit
        }
    }

    override fun getQuestPoints(cityId: String): Flow<Resource<List<QuestPoint>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { questPointApi.list(filter = mapOf("cityId" to cityId)) }
        when (result) {
            is Resource.Success -> emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro ao buscar pontos"))
            else -> Unit
        }
    }

    override fun getCityDetails(cityId: String): Flow<Resource<City>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { cityApi.getById(cityId) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao buscar cidade"))
    }

    override fun getQuestPointDetails(pointId: String): Flow<Resource<QuestPoint>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { questPointApi.getById(pointId) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao buscar ponto"))
    }

    override fun getQuests(questPointId: String): Flow<Resource<List<Quest>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { questApi.getByQuestPoint(questPointId) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        else emit(Resource.Error(result.message ?: "Erro ao buscar missões"))
    }

    override fun getQuestDetails(questId: String): Flow<Resource<Quest>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { questApi.getById(questId) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao buscar missão"))
    }

    override fun getSceneDialogue(dialogueId: String): Flow<Resource<SceneDialogue>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { sceneDialogueApi.getById(dialogueId) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao buscar diálogo"))
    }

    // Implementação do método que faltava
    override fun getCharacterDetails(characterId: String): Flow<Resource<CharacterEntity>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { characterEntityApi.getById(characterId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao buscar detalhes do personagem"))
        }
    }

    override fun getUnlockPreview(contentId: String, type: String): Flow<Resource<UnlockRequirement>> = flow {
        // Como não há endpoint específico de preview, buscamos o objeto e retornamos seu requisito
        emit(Resource.Loading())
        val req = when (type.uppercase()) {
            "CITY" -> {
                val res = safeApiCall { cityApi.getById(contentId) }
                if(res is Resource.Success) res.data!!.unlockRequirement else null
            }
            "QUEST_POINT" -> {
                val res = safeApiCall { questPointApi.getById(contentId) }
                if(res is Resource.Success) res.data!!.unlockRequirement else null
            }
            "QUEST" -> {
                val res = safeApiCall { questApi.getById(contentId) }
                if(res is Resource.Success) res.data!!.unlockRequirement else null
            }
            else -> null
        }

        if (req != null) emit(Resource.Success(req))
        else emit(Resource.Error("Requisito não encontrado ou erro ao carregar"))
    }

    override fun unlockContent(contentId: String, type: String): Flow<Resource<Boolean>> = flow {
        // O desbloqueio real ocorre via pagamento ou gameplay (backend).
        // Este método pode servir para checar se já está desbloqueado ou forçar refresh.
        // Por enquanto, simulamos sucesso se o item for acessível.
        emit(Resource.Success(true))
    }

    // --- Métodos de Escrita (Admin/Creator) ---

    override fun createCity(city: City): Flow<Resource<City>> = flow {
        emit(Resource.Loading())
        // Mapping manual rápido Domain -> DTO
        val dto = CityRequestDTO(
            cityName = city.name,
            countryCode = city.countryCode,
            descriptionCity = city.description,
            languageId = city.languageId,
            lat = city.lat,
            lon = city.lon,
            imageUrl = city.imageUrl,
            iconUrl = city.iconUrl,
            isPremium = city.isPremium,
            unlockRequirement = city.unlockRequirement,
            isPublished = city.isPublished
        )
        val result = safeApiCall { cityApi.create(dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao criar cidade"))
    }

    override fun updateCity(city: City): Flow<Resource<City>> = flow {
        emit(Resource.Loading())
        val dto = CityRequestDTO(
            cityName = city.name,
            countryCode = city.countryCode,
            descriptionCity = city.description,
            languageId = city.languageId,
            lat = city.lat,
            lon = city.lon,
            imageUrl = city.imageUrl,
            iconUrl = city.iconUrl,
            isPremium = city.isPremium,
            unlockRequirement = city.unlockRequirement,
            isPublished = city.isPublished
        )
        val result = safeApiCall { cityApi.update(city.id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar cidade"))
    }

    override fun deleteContent(contentId: String, type: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = when(type.uppercase()) {
            "CITY" -> safeApiCall { cityApi.delete(contentId) }
            "QUEST_POINT" -> safeApiCall { questPointApi.delete(contentId) }
            "QUEST" -> safeApiCall { questApi.delete(contentId) }
            else -> Resource.Error("Tipo desconhecido")
        }

        if (result is Resource.Success) emit(Resource.Success(Unit))
        else emit(Resource.Error(result.message ?: "Erro ao apagar conteúdo"))
    }

    override fun publishContent(contentId: String, type: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        // Lógica: Buscar, mudar flag, atualizar.
        val success = try {
            when (type.uppercase()) {
                "CITY" -> {
                    val get = safeApiCall { cityApi.getById(contentId) }
                    if (get is Resource.Success) {
                        val current = get.data!!
                        // Precisa converter ResponseDTO -> RequestDTO. Simplificado aqui:
                        val req = CityRequestDTO(current.cityName, current.countryCode, current.descriptionCity, current.languageId, current.boundingPolygon, current.lat, current.lon, current.imageUrl, current.iconUrl, current.isPremium, current.unlockRequirement, current.isAiGenerated, true)
                        safeApiCall { cityApi.update(contentId, req) } is Resource.Success
                    } else false
                }
                else -> false // Implementar para outros tipos se necessário
            }
        } catch (e: Exception) { false }

        if (success) emit(Resource.Success(true)) else emit(Resource.Error("Falha ao publicar"))
    }

    override fun archiveContent(contentId: String, type: String): Flow<Resource<Boolean>> = flow {
        // Similar ao publish, mas setando false
        emit(Resource.Success(true)) // Placeholder
    }

    override suspend fun syncProgress(languageId: String): Resource<Unit> {
        return try {
            val response = userLanguageApi.syncProgress(languageId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Falha ao sincronizar: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
}