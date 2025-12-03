package com.questua.app.presentation.languages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.enums.StatusLanguage
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.usecase.language_learning.AbandonLanguageUseCase
import com.questua.app.domain.usecase.language_learning.GetUserLanguagesUseCase
import com.questua.app.domain.usecase.language_learning.SetLearningLanguageUseCase
import com.questua.app.domain.usecase.language_learning.StartNewLanguageUseCase
import com.questua.app.domain.usecase.onboarding.GetAvailableLanguagesUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageUiModel(
    val userLanguage: UserLanguage,
    val name: String,
    val iconUrl: String?,
    val isCurrent: Boolean
)

data class LanguagesListState(
    val isLoading: Boolean = false,
    val languages: List<LanguageUiModel> = emptyList(),
    val availableLanguagesToAdd: List<Language> = emptyList(),
    val error: String? = null
)

sealed class LanguagesUiEvent {
    object NavigateToHub : LanguagesUiEvent()
}

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val getUserLanguagesUseCase: GetUserLanguagesUseCase,
    private val getAvailableLanguagesUseCase: GetAvailableLanguagesUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val setLearningLanguageUseCase: SetLearningLanguageUseCase,
    private val abandonLanguageUseCase: AbandonLanguageUseCase,
    private val startNewLanguageUseCase: StartNewLanguageUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(LanguagesListState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<LanguagesUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var currentUserId: String? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    currentUserId = userId
                    fetchLanguages(userId)
                }
            }
        }
    }

    private fun fetchLanguages(userId: String) {
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            combine(
                getUserLanguagesUseCase(userId),
                getAvailableLanguagesUseCase(),
                getUserStatsUseCase(userId)
            ) { userLangsRes, allLangsRes, statsRes ->

                val userLangs = userLangsRes.data ?: emptyList()
                val allLangs = allLangsRes.data ?: emptyList()
                val activeLangId = statsRes.data?.languageId

                val allLangsMap = allLangs.associateBy { it.id }

                val nonAbandonedUserLangs = userLangs.filter { it.status != StatusLanguage.ABANDONED }

                val uiList = nonAbandonedUserLangs.mapNotNull { uLang ->
                    val details = allLangsMap[uLang.languageId]
                    if (details != null) {
                        LanguageUiModel(
                            userLanguage = uLang,
                            name = details.name,
                            iconUrl = details.iconUrl,
                            isCurrent = uLang.languageId == activeLangId
                        )
                    } else null
                }.sortedByDescending { it.isCurrent }

                val myLangIds = userLangs.map { it.languageId }.toSet()
                val toAddList = allLangs.filter { it.id !in myLangIds }

                Pair(uiList, toAddList)

            }.collect { (uiList, toAddList) ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    languages = uiList,
                    availableLanguagesToAdd = toAddList,
                    error = null
                )
            }
        }
    }

    fun setCurrentLanguage(languageId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            setLearningLanguageUseCase(userId, languageId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiEvent.send(LanguagesUiEvent.NavigateToHub)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun addNewLanguage(languageId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            // 1. Inicia o novo idioma
            startNewLanguageUseCase(userId, languageId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // 2. Se deu certo, define ele como ATIVO automaticamente
                        setLearningLanguageUseCase(userId, languageId).collect { activationResult ->
                            when(activationResult) {
                                is Resource.Success -> {
                                    // 3. Redireciona para o Hub
                                    _uiEvent.send(LanguagesUiEvent.NavigateToHub)
                                }
                                is Resource.Error -> {
                                    // Falhou ao ativar, mas criou. Apenas recarrega a lista.
                                    _state.value = _state.value.copy(isLoading = false, error = "Idioma adicionado, mas falha ao ativar: ${activationResult.message}")
                                    fetchLanguages(userId)
                                }
                                else -> {}
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun abandonLanguage(userLanguageId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            abandonLanguageUseCase(userLanguageId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        currentUserId?.let { fetchLanguages(it) }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}