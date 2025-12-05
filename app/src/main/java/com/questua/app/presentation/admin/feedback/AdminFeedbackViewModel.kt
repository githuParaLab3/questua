package com.questua.app.presentation.admin.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.model.Report
import com.questua.app.domain.usecase.admin.feedback_management.GetUserReportsUseCase
import com.questua.app.domain.usecase.admin.feedback_management.ResolveReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminFeedbackState(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminFeedbackViewModel @Inject constructor(
    private val getUserReportsUseCase: GetUserReportsUseCase,
    private val resolveReportUseCase: ResolveReportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminFeedbackState())
    val state = _state.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        getUserReportsUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }
                is Resource.Success -> {
                    // Ordenação composta:
                    // 1º Critério: Status (OPEN < RESOLVED, então Abertos ficam em cima)
                    // 2º Critério: Data de criação (Mais novos primeiro dentro de cada grupo)
                    val sortedReports = (result.data ?: emptyList())
                        .sortedWith(
                            compareBy<Report> { it.status }
                                .thenByDescending { it.createdAt }
                        )

                    _state.value = _state.value.copy(
                        isLoading = false,
                        reports = sortedReports
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resolveReport(report: Report) {
        if (report.status == ReportStatus.RESOLVED) return

        viewModelScope.launch {
            resolveReportUseCase(report).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val updatedReport = result.data!!

                        // Atualiza o item na lista
                        val updatedList = _state.value.reports.map {
                            if (it.id == report.id) updatedReport else it
                        }

                        // Re-ordena a lista para mover o item resolvido para baixo imediatamente
                        val resortedList = updatedList.sortedWith(
                            compareBy<Report> { it.status }
                                .thenByDescending { it.createdAt }
                        )

                        _state.value = _state.value.copy(reports = resortedList)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
}