package com.questua.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminHomeState(
    val isLoading: Boolean = false,
    val counts: Map<String, Int> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class AdminGeneralManagementViewModel @Inject constructor(
    // Caso ainda não tenha um UseCase específico de contagem global,
    // você pode injetar os UseCases de listagem de cada categoria aqui.
) : ViewModel() {

    private val _state = MutableStateFlow(AdminHomeState())
    val state = _state.asStateFlow()

    fun refreshStats() {
        // Implemente a lógica para buscar os counts do banco/API
        // Por enquanto, simularemos o carregamento
        _state.value = _state.value.copy(isLoading = true)

        // Exemplo de atualização (aqui viria a chamada dos seus repositórios)
        _state.value = _state.value.copy(
            isLoading = false,
            counts = mapOf(
                "languages" to 0,
                "cities" to 0,
                "quests" to 0,
                "quest_points" to 0,
                "dialogues" to 0,
                "characters" to 0,
                "achievements" to 0
            )
        )
    }
}