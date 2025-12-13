package com.questua.app.presentation.admin.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.enums.ReportType
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
    val error: String? = null,
    val searchQuery: String = "",
    val selectedTypeFilter: ReportType? = null,
    val selectedStatusFilter: ReportStatus? = null
)

@HiltViewModel
class AdminFeedbackViewModel @Inject constructor(
    private val getUserReportsUseCase: GetUserReportsUseCase,
    private val resolveReportUseCase: ResolveReportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminFeedbackState())
    val state = _state.asStateFlow()

    private var allReportsCache: List<Report> = emptyList()

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
                    allReportsCache = result.data ?: emptyList()
                    applyFilters()
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

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onTypeFilterChange(type: ReportType?) {
        val newType = if (_state.value.selectedTypeFilter == type) null else type
        _state.value = _state.value.copy(selectedTypeFilter = newType)
        applyFilters()
    }

    fun onStatusFilterChange(status: ReportStatus?) {
        val newStatus = if (_state.value.selectedStatusFilter == status) null else status
        _state.value = _state.value.copy(selectedStatusFilter = newStatus)
        applyFilters()
    }

    private fun applyFilters() {
        val query = _state.value.searchQuery.trim().lowercase()
        val typeFilter = _state.value.selectedTypeFilter
        val statusFilter = _state.value.selectedStatusFilter

        val filteredList = allReportsCache.filter { report ->
            val matchesQuery = if (query.isEmpty()) true else {
                report.description.lowercase().contains(query) ||
                        report.userId.lowercase().contains(query) ||
                        report.id.lowercase().contains(query)
            }

            val matchesType = typeFilter == null || report.type == typeFilter
            val matchesStatus = statusFilter == null || report.status == statusFilter

            matchesQuery && matchesType && matchesStatus
        }.sortedWith(
            compareBy<Report> { it.status }
                .thenByDescending { it.createdAt }
        )

        _state.value = _state.value.copy(
            isLoading = false,
            reports = filteredList
        )
    }

    fun resolveReport(report: Report) {
        if (report.status == ReportStatus.RESOLVED) return

        viewModelScope.launch {
            resolveReportUseCase(report).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val updatedReport = result.data!!
                        allReportsCache = allReportsCache.map {
                            if (it.id == report.id) updatedReport else it
                        }
                        applyFilters()
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