package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.model.UserQuest
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(userId: String): Flow<Resource<UserAccount>>
    fun updateUserProfile(user: UserAccount): Flow<Resource<UserAccount>>
    fun changePassword(userId: String, currentPass: String, newPass: String): Flow<Resource<Unit>>
    fun getUserStats(userId: String): Flow<Resource<UserLanguage>>
    fun toggleAdminMode(userId: String, enabled: Boolean): Flow<Resource<Boolean>>
    fun getAllUsers(): Flow<Resource<List<UserAccount>>>
    fun getUserLanguages(userId: String): Flow<Resource<List<UserLanguage>>>
    fun setLearningLanguage(userId: String, languageId: String): Flow<Resource<UserLanguage>>
    fun startNewLanguage(userId: String, languageId: String): Flow<Resource<UserLanguage>>
    fun resumeLanguage(userLanguageId: String): Flow<Resource<Boolean>>
    fun abandonLanguage(userLanguageId: String): Flow<Resource<Boolean>>
}