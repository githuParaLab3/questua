package com.questua.app.presentation.admin.logs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.AiGenerationLog
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminLogDetailState(
    val isLoading: Boolean = false,
    val log: AiGenerationLog? = null,
    val error: String? = null
)

@HiltViewModel
class AdminLogDetailViewModel @Inject constructor(
    private val repository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminLogDetailState())
        private set

    init {
        savedStateHandle.get<String>("logId")?.let { logId ->
            fetchLogDetail(logId)
        }
    }

    private fun fetchLogDetail(logId: String) {
        // Nota: Certifique-se de que o AdminRepository possua o método getAiLogById
        repository.getAiLogs(0, 100).onEach { result ->
            when (result) {
                is Resource.Loading -> state = state.copy(isLoading = true)
                is Resource.Success -> {
                    val log = result.data?.find { it.id == logId }
                    state = state.copy(log = log, isLoading = false)
                }
                is Resource.Error -> state = state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }
}