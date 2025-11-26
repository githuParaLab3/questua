package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Language
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun getAvailableLanguages(query: String? = null): Flow<Resource<List<Language>>>
    fun getLanguageById(id: String): Flow<Resource<Language>>
}