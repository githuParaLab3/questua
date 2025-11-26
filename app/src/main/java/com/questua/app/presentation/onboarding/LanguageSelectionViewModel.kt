package com.questua.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Language
import com.questua.app.domain.usecase.onboarding.GetAvailableLanguagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageSelectionState(
    val isLoading: Boolean = false,
    val languages: List<Language> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val getAvailableLanguagesUseCase: GetAvailableLanguagesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LanguageSelectionState())
    val state = _state.asStateFlow()

    init {
        loadLanguages()
    }

    fun loadLanguages() {
        viewModelScope.launch {
            getAvailableLanguagesUseCase()
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                languages = result.data ?: emptyList(),
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar idiomas"
                            )
                        }
                    }
                }.launchIn(this)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}