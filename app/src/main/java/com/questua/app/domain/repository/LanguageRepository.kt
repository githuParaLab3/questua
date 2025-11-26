package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Language
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun getAvailableLanguages(): Flow<Resource<List<Language>>>

}