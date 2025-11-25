package com.questua.app.domain.model

import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.enums.ReportType
import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val id: String,
    val userId: String,
    val type: ReportType,
    val description: String,
    val screenshotUrl: String? = null,
    val deviceInfo: DeviceInfo? = null,
    val appVersion: String? = null,
    val status: ReportStatus,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class DeviceInfo(
    val deviceModel: String? = null,
    val androidVersion: String? = null,
    val screenWidth: Int? = null,
    val screenHeight: Int? = null
)