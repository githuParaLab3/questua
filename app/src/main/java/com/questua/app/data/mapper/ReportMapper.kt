package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.DeviceInfoDTO
import com.questua.app.data.remote.dto.ReportResponseDTO
import com.questua.app.domain.model.DeviceInfo
import com.questua.app.domain.model.Report

fun ReportResponseDTO.toDomain(): Report {
    return Report(
        id = this.id,
        userId = this.userId,
        type = this.typeReport,
        description = this.descriptionReport,
        screenshotUrl = this.screenshotUrl,
        deviceInfo = this.deviceInfo?.toDomain(),
        appVersion = this.appVersion,
        status = this.statusReport,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun DeviceInfoDTO.toDomain(): DeviceInfo {
    return DeviceInfo(
        deviceModel = this.deviceModel,
        androidVersion = this.androidVersion,
        screenWidth = this.screenWidth,
        screenHeight = this.screenHeight
    )
}