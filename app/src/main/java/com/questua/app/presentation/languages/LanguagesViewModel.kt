package com.questua.app.presentation.languages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.usecase.language_learning.AbandonLanguageUseCase
import com.questua.app.domain.usecase.language_learning.GetUserLanguagesUseCase
import com.questua.app.domain.usecase.language_learning.SetLearningLanguageUseCase
import com.questua.app.domain.usecase.language_learning.StartNewLanguageUseCase
import com.questua.app.domain.usecase.onboarding.GetAvailableLanguagesUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Modelo visual completo para a Lista
data class LanguageUiModel(
    val userLanguage: UserLanguage,
    val name: String,
    val iconUrl: String?,
    val isCurrent: Boolean
)

data class LanguagesListState(
    val isLoading: Boolean = false,
    val languages: List<LanguageUiModel> = emptyList(), // Seus idiomas
    val availableLanguagesToAdd: List<Language> = emptyList(), // Idiomas que você NÃO tem (para o modal)
    val error: String? = null
)

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
            // Combina 3 fontes de dados simultaneamente para montar a tela sem "piscar"
            combine(
                getUserLanguagesUseCase(userId),        // 1. O que o usuário estuda
                getAvailableLanguagesUseCase(),         // 2. Todos os idiomas do sistema (pra pegar nome/bandeira)
                getUserStatsUseCase(userId)             // 3. Qual é o idioma ativo no momento?
            ) { userLangsRes, allLangsRes, statsRes ->

                // Extrai dados seguros
                val userLangs = userLangsRes.data ?: emptyList()
                val allLangs = allLangsRes.data ?: emptyList()
                val activeLangId = statsRes.data?.languageId

                // Mapa auxiliar: ID -> Objeto Language
                val allLangsMap = allLangs.associateBy { it.id }

                // --- LISTA PRINCIPAL (Meus Idiomas) ---
                val uiList = userLangs.mapNotNull { uLang ->
                    val details = allLangsMap[uLang.languageId]
                    if (details != null) {
                        LanguageUiModel(
                            userLanguage = uLang,
                            name = details.name,
                            iconUrl = details.iconUrl,
                            isCurrent = uLang.languageId == activeLangId
                        )
                    } else null
                }

                // --- LISTA DO MODAL (O que falta aprender) ---
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
            // Ao definir, o combine lá em cima vai detectar a mudança no Stats e atualizar o UI automaticamente
            setLearningLanguageUseCase(userId, languageId).collect()
        }
    }

    fun addNewLanguage(languageId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            // Cria o registro. O combine vai detectar o novo idioma na lista do user e atualizar a tela.
            startNewLanguageUseCase(userId, languageId).collect { result ->
                if (result is Resource.Error<*>) {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun abandonLanguage(userLanguageId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            abandonLanguageUseCase(userLanguageId).collect { result ->
                if (result is Resource.Error<*>) {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}