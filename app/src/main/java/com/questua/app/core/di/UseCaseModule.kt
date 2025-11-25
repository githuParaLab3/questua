package com.questua.app.core.di

import com.questua.app.domain.repository.AuthRepository
import com.questua.app.domain.repository.LanguageRepository
import com.questua.app.domain.usecase.auth.LoginUseCase
import com.questua.app.domain.usecase.auth.RegisterUseCase
import com.questua.app.domain.usecase.onboarding.GetAvailableLanguagesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(repo: AuthRepository) = LoginUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideRegisterUseCase(repo: AuthRepository) = RegisterUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetLanguagesUseCase(repo: LanguageRepository) = GetAvailableLanguagesUseCase(repo)
}