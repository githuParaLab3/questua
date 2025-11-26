package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.AiGenerationStatus
import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.model.AiGenerationLog
import com.questua.app.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun generateContent(prompt: String, type: String, contextId: String?): Flow<Resource<String>>
    fun getAiGenerationLogs(): Flow<Resource<List<AiGenerationLog>>>
    fun getUserReports(): Flow<Resource<List<Report>>>
    fun resolveReport(reportId: String, status: ReportStatus, resolutionNote: String?): Flow<Resource<Boolean>>
    fun sendReport(userId: String, type: String, description: String): Flow<Resource<Boolean>> // User side
}