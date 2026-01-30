package com.questua.app.presentation.admin.logs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.AiGenerationStatus
import com.questua.app.domain.enums.AiTargetType
import com.questua.app.domain.model.AiGenerationLog
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminAiLogsState(
    val isLoading: Boolean = false,
    val logs: List<AiGenerationLog> = emptyList(),
    val error: String? = null,
    val selectedStatus: AiGenerationStatus? = null,
    val selectedTarget: AiTargetType? = null
)

@HiltViewModel
class AdminAiLogsViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    var state by mutableStateOf(AdminAiLogsState())
        private set

    val filteredLogs by derivedStateOf {
        state.logs.filter { log ->
            val matchStatus = state.selectedStatus == null || log.status == state.selectedStatus
            val matchTarget = state.selectedTarget == null || log.targetType == state.selectedTarget
            matchStatus && matchTarget
        }
    }

    init { fetchLogs() }

    fun onStatusFilterSelected(status: AiGenerationStatus?) {
        state = state.copy(selectedStatus = status)
    }

    fun onTargetFilterSelected(target: AiTargetType?) {
        state = state.copy(selectedTarget = target)
    }

    fun clearFilters() {
        state = state.copy(selectedStatus = null, selectedTarget = null)
    }

    fun fetchLogs() {
        repository.getAiLogs(page = 0, size = 50).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> state.copy(logs = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }
}