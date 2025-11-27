package com.questua.app.domain.usecase.feedback

import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.ReportType
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class SendReportUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(
        userId: String,
        type: ReportType,
        description: String,
        screenshotFile: File? = null
    ): Flow<Resource<Boolean>> {
        return repository.sendReport(
            userId = userId,
            type = type.name,
            description = description,
            screenshotUrl = screenshotFile?.absolutePath
        )
    }
}