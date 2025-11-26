package com.questua.app.presentation.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.usecase.onboarding.GetLanguageDetailsUseCase
import com.questua.app.domain.usecase.user.GetUserProfileUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Modelo simples para UI
data class QuestPreview(
    val title: String,
    val description: String,
    val status: String
)

data class HubState(
    val isLoading: Boolean = false,
    val user: UserAccount? = null,
    val activeLanguage: UserLanguage? = null,
    val currentLanguageDetails: Language? = null,
    // Campos restaurados:
    val lastQuest: QuestPreview? = null,
    val newContentList: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HubViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getLanguageDetailsUseCase: GetLanguageDetailsUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(HubState())
    val state = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        observeUserSession()
    }

    fun refreshData() {
        currentUserId?.let { loadHubData(it) }
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    currentUserId = userId
                    loadHubData(userId)
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun loadHubData(userId: String) {
        _state.value = _state.value.copy(isLoading = true)

        // 1. Perfil (Recarrega sempre para atualizar foto/nome)
        getUserProfileUseCase(userId).onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(user = result.data)
            }
        }.launchIn(viewModelScope)

        // 2. Estatísticas e Dados do Hub
        getUserStatsUseCase(userId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val userLang = result.data!!

                    // Mock restaurado do conteúdo (Idealmente viria de um UseCase)
                    val lastQuestMock = QuestPreview(
                        title = "Fundamentos do Idioma",
                        description = "Complete a lição 3 para avançar",
                        status = "Em andamento"
                    )
                    val newContentMock = listOf(
                        "Expressões de Viagem",
                        "Gírias Populares",
                        "Cultura Local: Comidas"
                    )

                    _state.value = _state.value.copy(
                        activeLanguage = userLang,
                        isLoading = false,
                        lastQuest = lastQuestMock,
                        newContentList = newContentMock
                    )

                    fetchLanguageDetails(userLang.languageId)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchLanguageDetails(languageId: String) {
        getLanguageDetailsUseCase(languageId).onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(currentLanguageDetails = result.data)
            }
        }.launchIn(viewModelScope)
    }
}