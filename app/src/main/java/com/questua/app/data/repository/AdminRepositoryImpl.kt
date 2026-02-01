package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.*
import com.questua.app.data.remote.dto.*
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.enums.TargetType
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
    private val uploadApi: UploadApi,
    private val achievementApi: AchievementApi,
    private val sceneDialogueApi: SceneDialogueApi,
    private val aiGenerationApi: AiGenerationApi
) : AdminRepository, SafeApiCall() {

    override fun generateQuestPoint(cityId: String, theme: String): Flow<Resource<QuestPoint>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { aiApi.generateQuestPoint(GenerateQuestPointRequestDTO(cityId, theme)) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun generateQuest(questPointId: String, context: String, difficulty: Int): Flow<Resource<Quest>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { aiApi.generateQuest(GenerateQuestRequestDTO(questPointId, context, difficulty)) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun generateCharacter(archetype: String): Flow<Resource<CharacterEntity>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { aiApi.generateCharacter(GenerateCharacterRequestDTO(archetype)) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun generateDialogue(speakerId: String, context: String, questId: String?, inputMode: String): Flow<Resource<SceneDialogue>> = flow {
        emit(Resource.Loading())
        val request = GenerateDialogueRequestDTO(questId = questId, speakerCharacterId = speakerId, context = context, inputMode = inputMode)
        val result = safeApiCall { aiApi.generateDialogue(request) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun generateAchievement(trigger: String, difficulty: String): Flow<Resource<Achievement>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { aiApi.generateAchievement(GenerateAchievementRequestDTO(trigger, difficulty)) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.toDomain()))
        else emit(Resource.Error(result.message ?: "Erro na geração"))
    }

    override fun getAiLogs(page: Int, size: Int): Flow<Resource<List<AiGenerationLog>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { aiLogApi.list(page = page, size = size) }
        if (result is Resource.Success) emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        else emit(Resource.Error(result.message ?: "Erro ao carregar logs"))
    }

    override fun getCities(query: String?): Flow<Resource<List<City>>> = flow {
        emit(Resource.Loading())
        val response = safeApiCall { cityApi.list(size = 100) }
        if (response is Resource.Success) {
            val domainList = response.data?.content?.map { it.toDomain() } ?: emptyList()
            emit(Resource.Success(domainList))
        } else if (response is Resource.Error) {
            emit(Resource.Error(response.message ?: "Erro ao listar cidades"))
        }
    }

    override fun deleteCity(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        emit(safeApiCall { cityApi.delete(id) })


    }

    override fun uploadFile(file: java.io.File, folder: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = safeApiCall { uploadApi.uploadArchive(body, folder) }

        if (response is Resource.Success) {
            val url = response.data?.get("url")
            if (url != null) emit(Resource.Success(url))
            else emit(Resource.Error("URL não retornada pelo servidor"))
        } else {
            emit(Resource.Error(response.message ?: "Falha no upload"))
        }
    }

    override fun saveCity(
        id: String?,
        cityName: String,
        countryCode: String,
        descriptionCity: String,
        languageId: String,
        boundingPolygon: BoundingPolygon?,
        lat: Double,
        lon: Double,
        imageUrl: String?,
        iconUrl: String?,
        isPremium: Boolean,
        unlockRequirement: UnlockRequirement?,
        isAiGenerated: Boolean,
        isPublished: Boolean
    ): Flow<Resource<City>> = flow {
        emit(Resource.Loading())

        val dto = CityRequestDTO(
            cityName = cityName,
            countryCode = countryCode,
            descriptionCity = descriptionCity,
            languageId = languageId,
            boundingPolygon = boundingPolygon,
            lat = lat,
            lon = lon,
            imageUrl = imageUrl,
            iconUrl = iconUrl,
            isPremium = isPremium,
            unlockRequirement = unlockRequirement,
            isAiGenerated = isAiGenerated,
            isPublished = isPublished
        )

        val apiResult = if (id == null) {
            safeApiCall { cityApi.create(dto) }
        } else {
            safeApiCall { cityApi.update(id, dto) }
        }

        when (apiResult) {
            is Resource.Success -> {
                apiResult.data?.toDomain()?.let { emit(Resource.Success(it)) }
            }
            is Resource.Error -> {
                emit(Resource.Error(apiResult.message ?: "Erro ao salvar cidade"))
            }
            else -> Unit
        }
    }
    override fun getQuestPoints(query: String?): Flow<Resource<List<QuestPoint>>> = flow {
        emit(Resource.Loading())
        val response = safeApiCall { questPointApi.list(size = 100) }
        if (response is Resource.Success) {
            val domainList = response.data?.content?.map { it.toDomain() } ?: emptyList()
            emit(Resource.Success(domainList))
        } else if (response is Resource.Error) {
            emit(Resource.Error(response.message ?: "Erro ao listar"))
        }
    }

    override fun deleteQuestPoint(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        emit(safeApiCall { questPointApi.delete(id) })
    }

    override fun saveQuestPoint(
        id: String?,
        cityId: String,
        title: String,
        description: String,
        lat: Double,
        lon: Double
    ): Flow<Resource<QuestPoint>> = flow {
        emit(Resource.Loading())

        val dto = QuestPointRequestDTO(
            cityId = cityId,
            title = title,
            descriptionQpoint = description, // Mapeamento correto para o DTO
            lat = lat,
            lon = lon
        )

        val apiResult = if (id == null) safeApiCall { questPointApi.create(dto) }
        else safeApiCall { questPointApi.update(id, dto) }

        if (apiResult is Resource.Success) {
            apiResult.data?.toDomain()?.let { emit(Resource.Success(it)) }
        } else if (apiResult is Resource.Error) {
            emit(Resource.Error(apiResult.message ?: "Erro ao salvar"))
        }
    }

    override fun getQuests(query: String?): Flow<Resource<List<Quest>>> = flow {
        emit(Resource.Loading())
        val response = safeApiCall<PageResponse<QuestResponseDTO>> {
            questApi.getAll(page = 0, size = 100)
        }
        if (response is Resource.Success) {
            val domainList = response.data?.content?.map { it.toDomain() } ?: emptyList()
            emit(Resource.Success(domainList))
        } else if (response is Resource.Error) {
            emit(Resource.Error(response.message ?: "Erro ao listar quests"))
        }
    }

    override fun deleteQuest(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        emit(safeApiCall { questApi.delete(id) })
    }

    override fun saveQuest(
        id: String?,
        questPointId: String,
        title: String,
        description: String,
        difficulty: Int,
        orderIndex: Int,
        xpValue: Int,
        isPremium: Boolean,
        isPublished: Boolean
    ): Flow<Resource<Quest>> = flow {
        emit(Resource.Loading())
        val dto = QuestRequestDTO(
            questPointId = questPointId,
            title = title,
            descriptionQuest = description,
            difficulty = difficulty.toShort(),
            orderIndex = orderIndex.toShort(),
            xpValue = xpValue,
            isPremium = isPremium,
            isPublished = isPublished
        )
        val result = if (id == null) safeApiCall<QuestResponseDTO> { questApi.create(dto) }
        else safeApiCall<QuestResponseDTO> { questApi.update(id, dto) }

        if (result is Resource.Success) {
            result.data?.toDomain()?.let { emit(Resource.Success(it)) }
        } else if (result is Resource.Error) {
            emit(Resource.Error(result.message ?: "Erro ao salvar"))
        }
    }
    override fun getCharacters(query: String?): Flow<Resource<List<CharacterEntity>>> = flow {
        emit(Resource.Loading())
        val response = safeApiCall { characterApi.list(size = 100) }
        when (response) {
            is Resource.Success -> {
                val domainList = response.data?.content?.map { it.toDomain() } ?: emptyList()
                emit(Resource.Success(domainList))
            }
            is Resource.Error -> emit(Resource.Error(response.message ?: "Erro ao listar"))
            else -> Unit
        }
    }

    override fun deleteCharacter(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        emit(safeApiCall { characterApi.delete(id) })
    }

    override fun saveCharacter(
        id: String?,
        name: String,
        avatarUrl: String,
        isAi: Boolean,
        voiceUrl: String?,
        persona: Persona?
    ): Flow<Resource<CharacterEntity>> = flow {
        emit(Resource.Loading())

        val dto = CharacterEntityRequestDTO(
            nameCharacter = name,
            avatarUrl = avatarUrl,
            isAiGenerated = isAi,
            voiceUrl = voiceUrl,
            persona = persona
        )

        val apiResult = if (id == null) {
            safeApiCall { characterApi.create(dto) }
        } else {
            safeApiCall { characterApi.update(id, dto) }
        }

        when (apiResult) {
            is Resource.Success -> {
                val data = apiResult.data?.toDomain()
                if (data != null) {
                    emit(Resource.Success(data))
                } else {
                    emit(Resource.Error("Erro ao processar dados do servidor"))
                }
            }
            is Resource.Error -> emit(Resource.Error(apiResult.message ?: "Erro ao salvar"))
            else -> Unit
        }
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

    override fun getProducts(
        page: Int,
        size: Int,
        query: String?,
        type: TargetType?
    ): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())

        val typeString = type?.name

        val result = safeApiCall {
            productApi.list(
                page = page,
                size = size,
                title = query,
                targetType = typeString
            )
        }

        when (result) {
            is Resource.Success -> {
                val domainList = result.data?.content?.map { it.toDomain() } ?: emptyList()
                emit(Resource.Success(domainList))
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro desconhecido"))
            is Resource.Loading -> emit(Resource.Loading())
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

    override fun getAllCities(page: Int, size: Int): Flow<Resource<List<City>>> = flow {
        emit(Resource.Loading())
        // CORREÇÃO: Usando argumentos nomeados para pular o parâmetro 'filter'
        val result = safeApiCall { cityApi.list(page = page, size = size) }
        when (result) {
            is Resource.Success -> {
                val list = result.data?.content?.map { it.toDomain() } ?: emptyList()
                emit(Resource.Success(list))
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro ao buscar cidades"))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }

    override fun getAllQuests(page: Int, size: Int): Flow<Resource<List<Quest>>> = flow {
        emit(Resource.Loading())
        // QuestApi.getAll não tem filter, mas usar nomeado é mais seguro
        val result = safeApiCall { questApi.getAll(page = page, size = size) }
        when (result) {
            is Resource.Success -> {
                val list = result.data?.content?.map { it.toDomain() } ?: emptyList()
                emit(Resource.Success(list))
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro ao buscar missões"))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }

    override fun getAllQuestPoints(page: Int, size: Int): Flow<Resource<List<QuestPoint>>> = flow {
        emit(Resource.Loading())
        // CORREÇÃO: Mesmo caso da CityApi, o list espera um map primeiro
        val result = safeApiCall { questPointApi.list(page = page, size = size) }
        when (result) {
            is Resource.Success -> {
                val list = result.data?.content?.map { it.toDomain() } ?: emptyList()
                emit(Resource.Success(list))
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro ao buscar pontos"))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }

    override fun updateProduct(product: Product): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())
        val request = ProductRequestDTO(
            sku = product.sku,
            title = product.title,
            descriptionProduct = product.description,
            priceCents = product.priceCents,
            currency = product.currency,
            targetType = product.targetType,
            targetId = product.targetId
        )
        // Assumindo que sua API tem um método update(id, dto)
        val result = safeApiCall { productApi.update(product.id, request) }
        when (result) {
            is Resource.Success -> emit(Resource.Success(result.data!!.toDomain()))
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro ao atualizar"))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }

    override fun getAchievements(query: String?): Flow<Resource<List<Achievement>>> = flow {
        emit(Resource.Loading())
        val response = safeApiCall { achievementApi.list(size = 100) }
        if (response is Resource.Success) {
            val domainList = response.data?.content?.map { it.toDomain() } ?: emptyList()
            emit(Resource.Success(domainList))
        } else if (response is Resource.Error) {
            emit(Resource.Error(response.message ?: "Erro ao listar"))
        }
    }

    override fun deleteAchievement(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        emit(safeApiCall { achievementApi.delete(id) })
    }

    override fun saveAchievement(
        id: String?,
        name: String,
        description: String,
        iconUrl: String,
        xpReward: Int,
        keyName: String,
        rarity: RarityType
    ): Flow<Resource<Achievement>> = flow {
        emit(Resource.Loading())

        val dto = AchievementRequestDTO(
            nameAchievement = name,
            descriptionAchievement = description,
            iconUrl = iconUrl,
            xpReward = xpReward,
            keyName = keyName,
            rarity = rarity // Agora o tipo coincide com o esperado pelo DTO
        )

        val apiResult = if (id == null) safeApiCall { achievementApi.create(dto) }
        else safeApiCall { achievementApi.update(id!!, dto) }

        when (apiResult) {
            is Resource.Success -> {
                apiResult.data?.toDomain()?.let { emit(Resource.Success(it)) }
            }
            is Resource.Error -> emit(Resource.Error(apiResult.message ?: "Erro ao salvar"))
            else -> Unit
        }
    }
    override fun getDialogues(query: String?): Flow<Resource<List<SceneDialogue>>> = flow {
        emit(Resource.Loading())
        // CORREÇÃO: Usando 'list' com argumentos nomeados para pular o Map de filtros
        val response = safeApiCall<PageResponse<SceneDialogueResponseDTO>> {
            sceneDialogueApi.list(page = 0, size = 100)
        }
        if (response is Resource.Success) {
            val domainList = response.data?.content?.map { it.toDomain() } ?: emptyList()
            emit(Resource.Success(domainList))
        } else if (response is Resource.Error) {
            emit(Resource.Error(response.message ?: "Erro ao listar diálogos"))
        }
    }

    override fun saveDialogue(
        id: String?,
        textContent: String,
        description: String,
        backgroundUrl: String,
        speakerCharacterId: String?,
        expectsUserResponse: Boolean,
        inputMode: com.questua.app.domain.enums.InputMode,
        nextDialogueId: String?
    ): Flow<Resource<SceneDialogue>> = flow {
        emit(Resource.Loading())

        val dto = SceneDialogueRequestDTO(
            textContent = textContent,
            descriptionDialogue = description, // Garanta que no seu DTO o nome seja este
            backgroundUrl = backgroundUrl,
            speakerCharacterId = speakerCharacterId,
            expectsUserResponse = expectsUserResponse,
            inputMode = inputMode,
            nextDialogueId = nextDialogueId
        )

        val apiResult = if (id == null) {
            safeApiCall<SceneDialogueResponseDTO> { sceneDialogueApi.create(dto) }
        } else {
            safeApiCall<SceneDialogueResponseDTO> { sceneDialogueApi.update(id, dto) }
        }

        if (apiResult is Resource.Success) {
            apiResult.data?.toDomain()?.let { emit(Resource.Success(it)) }
        } else if (apiResult is Resource.Error) {
            emit(Resource.Error(apiResult.message ?: "Erro ao salvar diálogo"))
        }
    }

    override fun deleteDialogue(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        emit(safeApiCall { sceneDialogueApi.delete(id) })
    }
}