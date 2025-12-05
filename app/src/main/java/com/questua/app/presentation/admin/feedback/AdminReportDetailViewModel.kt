package com.questua.app.presentation.admin.feedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.model.Report
import com.questua.app.domain.usecase.admin.feedback_management.DeleteReportUseCase
import com.questua.app.domain.usecase.admin.feedback_management.GetReportDetailsUseCase
import com.questua.app.domain.usecase.admin.feedback_management.ResolveReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportDetailState(
    val isLoading: Boolean = false,
    val report: Report? = null,
    val error: String? = null,
    val successMessage: String? = null // Apenas a mensagem, sem flag de dialog
)

@HiltViewModel
class AdminReportDetailViewModel @Inject constructor(
    private val getReportDetailsUseCase: GetReportDetailsUseCase,
    private val resolveReportUseCase: ResolveReportUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ReportDetailState())
    val state = _state.asStateFlow()

    private val reportId: String? = savedStateHandle["reportId"]

    init {
        loadReport()
    }

    fun loadReport() {
        if (reportId == null) return

        getReportDetailsUseCase(reportId).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null)
                is Resource.Success -> _state.value = _state.value.copy(isLoading = false, report = result.data)
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    fun resolveReport() {
        val currentReport = _state.value.report ?: return
        if (currentReport.status == ReportStatus.RESOLVED) return

        viewModelScope.launch {
            resolveReportUseCase(currentReport).collect { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            report = result.data,
                            successMessage = "O report foi marcado como resolvido com sucesso!"
                        )
                    }
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun deleteReport() {
        val currentReport = _state.value.report ?: return

        viewModelScope.launch {
            deleteReportUseCase(currentReport.id).collect { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            successMessage = "O report foi excluído permanentemente."
                        )
                    }
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}