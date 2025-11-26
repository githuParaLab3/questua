package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.enums.ReportType
import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfoDTO(
    val deviceModel: String? = null,
    val androidVersion: String? = null,
    val screenWidth: Int? = null,
    val screenHeight: Int? = null
)

@Serializable
data class ReportRequestDTO(
    val userId: String,
    val typeReport: ReportType,
    val descriptionReport: String,
    val screenshotUrl: String? = null,
    val deviceInfo: DeviceInfoDTO? = null,
    val appVersion: String? = null,
    val statusReport: ReportStatus? = null
)

@Serializable
data class ReportResponseDTO(
    val id: String,
    val userId: String,
    val typeReport: ReportType,
    val descriptionReport: String,
    val screenshotUrl: String?,
    val deviceInfo: DeviceInfoDTO?,
    val appVersion: String?,
    val statusReport: ReportStatus,
    val createdAt: String,
    val updatedAt: String
)