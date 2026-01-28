package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.*
import com.questua.app.data.remote.dto.*
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val aiApi: AiGenerationApi,
    private val aiLogApi: AiGenerationLogApi,
    private val cityApi: CityApi,
    private val questPointApi: QuestPointApi,
    private val questApi: QuestApi,
    private val characterApi: CharacterEntityApi,
    private val reportApi: ReportApi,
    private val userApi: UserAccountApi,
    private val transactionApi: TransactionRecordApi,
    private val productApi: ProductApi,
    private val uploadApi: UploadApi
) : AdminRepository, SafeApiCall() {

    override fun generateQuestPoint(cityId: String, theme: String): Flow<Resource<QuestPoint>> = flow {
        emit(Resource.Loading())
        val request = GenerateQuestPointRequestDTO(cityId = cityId, theme = theme)
        val result = safeApiCall { aiApi.generateQuestPoint(request) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun generateQuest(questPointId: String, context: String, difficulty: Int): Flow<Resource<Quest>> = flow {
        emit(Resource.Loading())
        val request = GenerateQuestRequestDTO(questPointId = questPointId, context = context, difficultyLevel = difficulty)
        val result = safeApiCall { aiApi.generateQuest(request) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun generateCharacter(archetype: String): Flow<Resource<CharacterEntity>> = flow {
        emit(Resource.Loading())
        val request = GenerateCharacterRequestDTO(archetype = archetype)
        val result = safeApiCall { aiApi.generateCharacter(request) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun generateDialogue(speakerId: String, context: String, inputMode: String): Flow<Resource<SceneDialogue>> = flow {
        emit(Resource.Loading())
        val request = GenerateDialogueRequestDTO(speakerCharacterId = speakerId, context = context, inputMode = inputMode)
        val result = safeApiCall { aiApi.generateDialogue(request) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun generateAchievement(trigger: String, difficulty: String): Flow<Resource<Achievement>> = flow {
        emit(Resource.Loading())
        val request = GenerateAchievementRequestDTO(triggerAction = trigger, difficulty = difficulty)
        val result = safeApiCall { aiApi.generateAchievement(request) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun getAiLogs(page: Int, size: Int): Flow<Resource<List<AiGenerationLog>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { aiLogApi.list(page = page, size = size) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        else emit(Resource.Error(result.message ?: "Erro ao carregar logs"))
    }

    override fun createCity(city: City): Flow<Resource<City>> = flow {
        emit(Resource.Loading())
        val dto = CityRequestDTO(
            cityName = city.name,
            countryCode = city.countryCode,
            descriptionCity = city.description,
            languageId = city.languageId,
            lat = city.lat,
            lon = city.lon,
            isPremium = city.isPremium,
            isPublished = city.isPublished,
            imageUrl = city.imageUrl,
            iconUrl = city.iconUrl,
            boundingPolygon = city.boundingPolygon,
            unlockRequirement = city.unlockRequirement
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
            isPremium = city.isPremium,
            isPublished = city.isPublished,
            imageUrl = city.imageUrl,
            iconUrl = city.iconUrl,
            boundingPolygon = city.boundingPolygon,
            unlockRequirement = city.unlockRequirement
        )
        val result = safeApiCall { cityApi.update(city.id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar cidade"))
    }

    override fun deleteCity(cityId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { cityApi.delete(cityId) }
        if (result is Resource.Success) emit(Resource.Success(Unit))
        else emit(Resource.Error(result.message ?: "Erro ao apagar cidade"))
    }

    override fun createQuestPoint(questPoint: QuestPoint): Flow<Resource<QuestPoint>> = flow {
        emit(Resource.Loading())
        val dto = QuestPointRequestDTO(
            cityId = questPoint.cityId,
            title = questPoint.title,
            descriptionQpoint = questPoint.description,
            difficulty = questPoint.difficulty.toShort(),
            lat = questPoint.lat,
            lon = questPoint.lon,
            isPremium = questPoint.isPremium,
            isPublished = questPoint.isPublished,
            imageUrl = questPoint.imageUrl,
            iconUrl = questPoint.iconUrl,
            unlockRequirement = questPoint.unlockRequirement
        )
        val result = safeApiCall { questPointApi.create(dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao criar ponto"))
    }

    override fun updateQuestPoint(questPoint: QuestPoint): Flow<Resource<QuestPoint>> = flow {
        emit(Resource.Loading())
        val dto = QuestPointRequestDTO(
            cityId = questPoint.cityId,
            title = questPoint.title,
            descriptionQpoint = questPoint.description,
            difficulty = questPoint.difficulty.toShort(),
            lat = questPoint.lat,
            lon = questPoint.lon,
            isPremium = questPoint.isPremium,
            isPublished = questPoint.isPublished,
            imageUrl = questPoint.imageUrl,
            iconUrl = questPoint.iconUrl,
            unlockRequirement = questPoint.unlockRequirement
        )
        val result = safeApiCall { questPointApi.update(questPoint.id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar ponto"))
    }

    override fun deleteQuestPoint(questPointId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { questPointApi.delete(questPointId) }
        if (result is Resource.Success) emit(Resource.Success(Unit))
        else emit(Resource.Error(result.message ?: "Erro ao apagar ponto"))
    }

    override fun createQuest(quest: Quest): Flow<Resource<Quest>> = flow {
        emit(Resource.Loading())
        val dto = QuestRequestDTO(
            questPointId = quest.questPointId,
            title = quest.title,
            descriptionQuest = quest.description,
            difficulty = quest.difficulty.toShort(),
            orderIndex = quest.orderIndex.toShort(),
            xpValue = quest.xpValue,
            isPremium = quest.isPremium,
            isPublished = quest.isPublished,
            firstDialogueId = quest.firstDialogueId,
            unlockRequirement = quest.unlockRequirement,
            learningFocus = quest.learningFocus
        )
        val result = safeApiCall { questApi.create(dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao criar missão"))
    }

    override fun updateQuest(quest: Quest): Flow<Resource<Quest>> = flow {
        emit(Resource.Loading())
        val dto = QuestRequestDTO(
            questPointId = quest.questPointId,
            title = quest.title,
            descriptionQuest = quest.description,
            difficulty = quest.difficulty.toShort(),
            orderIndex = quest.orderIndex.toShort(),
            xpValue = quest.xpValue,
            isPremium = quest.isPremium,
            isPublished = quest.isPublished,
            firstDialogueId = quest.firstDialogueId,
            unlockRequirement = quest.unlockRequirement,
            learningFocus = quest.learningFocus
        )
        val result = safeApiCall { questApi.update(quest.id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar missão"))
    }

    override fun deleteQuest(questId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { questApi.delete(questId) }
        if (result is Resource.Success) emit(Resource.Success(Unit))
        else emit(Resource.Error(result.message ?: "Erro ao apagar missão"))
    }

    override fun createCharacter(character: CharacterEntity): Flow<Resource<CharacterEntity>> = flow {
        emit(Resource.Loading())
        val dto = CharacterEntityRequestDTO(
            nameCharacter = character.name,
            avatarUrl = character.avatarUrl,
            persona = character.persona,
            spriteSheet = character.spriteSheet,
            voiceUrl = character.voiceUrl,
            isAiGenerated = character.isAiGenerated
        )
        val result = safeApiCall { characterApi.create(dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao criar personagem"))
    }

    override fun updateCharacter(character: CharacterEntity): Flow<Resource<CharacterEntity>> = flow {
        emit(Resource.Loading())
        val dto = CharacterEntityRequestDTO(
            nameCharacter = character.name,
            avatarUrl = character.avatarUrl,
            persona = character.persona,
            spriteSheet = character.spriteSheet,
            voiceUrl = character.voiceUrl,
            isAiGenerated = character.isAiGenerated
        )
        val result = safeApiCall { characterApi.update(character.id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar personagem"))
    }

    override fun deleteCharacter(characterId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { characterApi.delete(characterId) }
        if (result is Resource.Success) emit(Resource.Success(Unit))
        else emit(Resource.Error(result.message ?: "Erro ao apagar personagem"))
    }

    override fun getAllReports(page: Int, size: Int): Flow<Resource<List<Report>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { reportApi.list(page = page, size = size) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        else emit(Resource.Error(result.message ?: "Erro ao carregar relatórios"))
    }

    override fun updateReport(report: Report): Flow<Resource<Report>> = flow {
        emit(Resource.Loading())
        val dto = ReportRequestDTO(
            userId = report.userId,
            typeReport = report.type,
            descriptionReport = report.description,
            screenshotUrl = report.screenshotUrl,
            statusReport = report.status,
            appVersion = report.appVersion,
            deviceInfo = null
        )
        val result = safeApiCall { reportApi.update(report.id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar relatório"))
    }

    override fun getReportById(id: String): Flow<Resource<Report>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { reportApi.getById(id) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao carregar report"))
    }

    override fun deleteReport(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { reportApi.delete(id) }
        if (result is Resource.Success) emit(Resource.Success(Unit))
        else emit(Resource.Error(result.message ?: "Erro ao excluir report"))
    }

    override fun getAllUsers(page: Int, size: Int): Flow<Resource<List<UserAccount>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userApi.list(page = page, size = size) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        else emit(Resource.Error(result.message ?: "Erro ao carregar utilizadores"))
    }

    override fun getUserById(id: String): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userApi.getById(id) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao carregar utilizador"))
    }

    override fun createUser(
        email: String,
        displayName: String,
        password: String,
        nativeLanguageId: String,
        role: UserRole,
        avatarFile: File?
    ): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())

        var avatarUrl: String? = null

        if (avatarFile != null) {
            try {
                val requestFile = avatarFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", avatarFile.name, requestFile)
                val uploadResult = safeApiCall { uploadApi.uploadArchive(body, "avatars") }

                if (uploadResult is Resource.Success) {
                    avatarUrl = uploadResult.data?.get("url")
                } else {
                    emit(Resource.Error("Falha no upload do avatar: ${uploadResult.message}"))
                    return@flow
                }
            } catch (e: Exception) {
                emit(Resource.Error("Erro ao processar imagem: ${e.message}"))
                return@flow
            }
        }

        val dto = UserAccountRequestDTO(
            email = email,
            displayName = displayName,
            password = password,
            nativeLanguageId = nativeLanguageId,
            userRole = role,
            avatarUrl = avatarUrl
        )
        val result = safeApiCall { userApi.create(dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao criar utilizador"))
    }

    override fun updateUser(
        id: String,
        email: String,
        displayName: String,
        nativeLanguageId: String,
        role: UserRole,
        password: String?,
        avatarFile: File?
    ): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())

        var currentAvatarUrl: String? = null

        val currentUserResult = safeApiCall { userApi.getById(id) }
        if (currentUserResult is Resource.Success) {
            currentAvatarUrl = currentUserResult.data?.avatarUrl
        }

        if (avatarFile != null) {
            try {
                val requestFile = avatarFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", avatarFile.name, requestFile)
                val uploadResult = safeApiCall { uploadApi.uploadArchive(body, "avatars") }

                if (uploadResult is Resource.Success) {
                    currentAvatarUrl = uploadResult.data?.get("url")
                } else {
                    emit(Resource.Error("Falha no upload: ${uploadResult.message}"))
                    return@flow
                }
            } catch (e: Exception) {
                emit(Resource.Error("Erro imagem: ${e.message}"))
                return@flow
            }
        }

        val dto = UserAccountRequestDTO(
            email = email,
            displayName = displayName,
            password = password,
            nativeLanguageId = nativeLanguageId,
            userRole = role,
            avatarUrl = currentAvatarUrl
        )
        val result = safeApiCall { userApi.update(id, dto) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro ao atualizar utilizador"))
    }

    override fun deleteUser(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { userApi.delete(id) }
        if (result is Resource.Success) emit(Resource.Success(Unit))
        else emit(Resource.Error(result.message ?: "Erro ao excluir utilizador"))
    }

    override fun getAllTransactions(page: Int, size: Int): Flow<Resource<List<TransactionRecord>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { transactionApi.list(page = page, size = size) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        else emit(Resource.Error(result.message ?: "Erro ao carregar transações"))
    }

    override fun getProducts(page: Int, size: Int): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { productApi.list(page = page, size = size) }
        if (result is Resource.Success) {
            // Assume que o PageResponse tem um campo 'content' com a lista
            emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar produtos"))
        }
    }

    override fun createProduct(product: Product): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())
        // Mapeando do domínio para DTO
        val dto = ProductRequestDTO(
            sku = product.sku,
            title = product.title,
            descriptionProduct = product.description,
            priceCents = product.priceCents,
            currency = product.currency,
            targetType = product.targetType,
            targetId = product.targetId
        )
        val result = safeApiCall { productApi.create(dto) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao criar produto"))
        }
    }

    override fun deleteProduct(productId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { productApi.delete(productId) }
        if (result is Resource.Success) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao deletar produto"))
        }
    }
}