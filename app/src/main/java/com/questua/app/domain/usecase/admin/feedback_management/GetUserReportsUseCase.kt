package com.questua.app.domain.usecase.admin.feedback_management

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Report
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserReportsUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(page: Int = 0, size: Int = 20): Flow<Resource<List<Report>>> {
        return repository.getAllReports(page, size)
    }
}