package com.questua.app.domain.usecase.onboarding
import com.questua.app.domain.repository.LanguageRepository
import javax.inject.Inject
class GetAvailableLanguagesUseCase @Inject constructor(private val repo: LanguageRepository) {
    operator fun invoke() = repo.getAvailableLanguages()
}