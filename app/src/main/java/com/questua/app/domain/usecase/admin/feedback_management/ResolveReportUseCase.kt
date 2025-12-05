package com.questua.app.domain.usecase.admin.feedback_management

import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.model.Report
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResolveReportUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(report: Report): Flow<Resource<Report>> {
        val updatedReport = report.copy(status = ReportStatus.RESOLVED)
        return repository.updateReport(updatedReport)
    }
}