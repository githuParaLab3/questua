package com.questua.app.presentation.languages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.usecase.language_learning.AbandonLanguageUseCase
import com.questua.app.domain.usecase.language_learning.GetUserLanguagesUseCase
import com.questua.app.domain.usecase.language_learning.SetLearningLanguageUseCase
import com.questua.app.domain.usecase.onboarding.GetAvailableLanguagesUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Modelo de UI que a tela está esperando
data class LanguageUiModel(
    val userLanguage: UserLanguage,
    val name: String,
    val iconUrl: String?,
    val isCurrent: Boolean
)

data class LanguagesListState(
    val isLoading: Boolean = false,
    val languages: List<LanguageUiModel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val getUserLanguagesUseCase: GetUserLanguagesUseCase,
    private val getAvailableLanguagesUseCase: GetAvailableLanguagesUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val setLearningLanguageUseCase: SetLearningLanguageUseCase,
    private val abandonLanguageUseCase: AbandonLanguageUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(LanguagesListState())
    val state = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    currentUserId = userId
                    fetchData(userId)
                }
            }
        }
    }

    private fun fetchData(userId: String) {
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            // Combina 3 fluxos de dados para montar a tela perfeita
            combine(
                getUserLanguagesUseCase(userId),      // 1. Idiomas que o usuário estuda
                getAvailableLanguagesUseCase(),       // 2. Dados visuais (nomes, bandeiras)
                getUserStatsUseCase(userId)           // 3. Qual está ativo agora?
            ) { userLangsResult, allLangsResult, statsResult ->

                val userLangs = userLangsResult.data ?: emptyList()
                val allLangs = allLangsResult.data ?: emptyList()
                val activeLangId = statsResult.data?.languageId

                // Mapa para acesso rápido aos detalhes do idioma
                val langDetailsMap = allLangs.associateBy { it.id }

                // Transforma em LanguageUiModel
                userLangs.mapNotNull { userLang ->
                    val details = langDetailsMap[userLang.languageId]
                    if (details != null) {
                        LanguageUiModel(
                            userLanguage = userLang,
                            name = details.name,
                            iconUrl = details.iconUrl,
                            isCurrent = userLang.languageId == activeLangId
                        )
                    } else {
                        null // Oculta se não achar dados do idioma
                    }
                }
            }.collect { uiList ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    languages = uiList,
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
                    is Resource.Success<*> -> {
                        // O combine lá em cima vai detectar a mudança no stats automaticamente
                        // mas podemos forçar um reload se necessário
                    }
                    is Resource.Error<*> -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    else -> {}
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}