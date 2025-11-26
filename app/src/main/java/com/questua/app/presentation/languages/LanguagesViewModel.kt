package com.questua.app.presentation.languages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.usecase.language_learning.AbandonLanguageUseCase
import com.questua.app.domain.usecase.language_learning.GetUserLanguagesUseCase
import com.questua.app.domain.usecase.onboarding.GetLanguageDetailsUseCase
import com.questua.app.domain.usecase.language_learning.SetLearningLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguagesListState(
    val isLoading: Boolean = false,
    val userLanguages: List<UserLanguageUi> = emptyList(),
    val error: String? = null
)

data class UserLanguageUi(
    val userLanguage: UserLanguage,
    val languageDetails: Language? = null
)

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val getUserLanguagesUseCase: GetUserLanguagesUseCase,
    private val getLanguageDetailsUseCase: GetLanguageDetailsUseCase,
    private val abandonLanguageUseCase: AbandonLanguageUseCase,
    private val setLearningLanguageUseCase: SetLearningLanguageUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(LanguagesListState())
    val state = _state.asStateFlow()

    init {
        loadLanguages()
    }

    fun loadLanguages() {
        viewModelScope.launch {
            val userId = tokenManager.userId.firstOrNull() ?: return@launch

            _state.value = _state.value.copy(isLoading = true)

            getUserLanguagesUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val rawList = result.data ?: emptyList()
                        // Enriquece a lista com os detalhes visuais (bandeira, nome)
                        val enrichedList = rawList.map { userLang ->
                            val details = getLanguageDetails(userLang.languageId)
                            UserLanguageUi(userLang, details)
                        }
                        _state.value = _state.value.copy(
                            userLanguages = enrichedList,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    // Função auxiliar suspensa para pegar detalhes
    private suspend fun getLanguageDetails(languageId: String): Language? {
        return getLanguageDetailsUseCase(languageId).firstOrNull()?.data
    }

    fun resumeLanguage(userLanguage: UserLanguage) {
        viewModelScope.launch {
            // Define como ativo (Last Active)
            setLearningLanguageUseCase(userLanguage.userId, userLanguage.languageId)
                .collect { result ->
                    if (result is Resource.Success) {
                        loadLanguages() // Recarrega para atualizar status visual
                    }
                }
        }
    }

    fun abandonLanguage(userLanguageId: String) {
        viewModelScope.launch {
            abandonLanguageUseCase(userLanguageId).collect { result ->
                if (result is Resource.Success) {
                    // Remove da lista localmente ou recarrega
                    loadLanguages()
                } else if (result is Resource.Error) {
                    _state.value = _state.value.copy(error = "Erro ao remover idioma")
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}