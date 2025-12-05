package com.questua.app.domain.usecase.admin.feedback_management

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Report
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReportDetailsUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(id: String): Flow<Resource<Report>> {
        return repository.getReportById(id)
    }
}