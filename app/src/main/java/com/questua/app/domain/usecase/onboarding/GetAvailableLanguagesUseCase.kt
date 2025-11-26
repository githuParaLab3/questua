package com.questua.app.domain.usecase.onboarding

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Language
import com.questua.app.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableLanguagesUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    operator fun invoke(query: String? = null): Flow<Resource<List<Language>>> {
        return repository.getAvailableLanguages(query)
    }
}