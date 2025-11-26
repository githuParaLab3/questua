package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.model.UserAchievement
import com.questua.app.domain.model.UserLanguage
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UserRepository {
    fun getUserProfile(userId: String): Flow<Resource<UserAccount>>
    fun updateUserProfile(user: UserAccount, password: String?, avatarFile: File?): Flow<Resource<UserAccount>>
    fun changePassword(userId: String, currentPass: String, newPass: String): Flow<Resource<Unit>>
    fun getUserStats(userId: String): Flow<Resource<UserLanguage>>
    fun toggleAdminMode(userId: String, enabled: Boolean): Flow<Resource<Boolean>>
    fun getAllUsers(): Flow<Resource<List<UserAccount>>>
    fun setLearningLanguage(userId: String, languageId: String): Flow<Resource<UserLanguage>>
    fun getUserLanguages(userId: String): Flow<Resource<List<UserLanguage>>>
    fun abandonLanguage(userLanguageId: String): Flow<Resource<Boolean>>
    fun startNewLanguage(userId: String, languageId: String): Flow<Resource<UserLanguage>>
    fun resumeLanguage(userLanguageId: String): Flow<Resource<Boolean>>
    fun getUserAchievements(userId: String): Flow<Resource<List<UserAchievement>>>
    fun sendReport(userId: String, type: String, description: String, screenshotUrl: String?): Flow<Resource<Boolean>>
}