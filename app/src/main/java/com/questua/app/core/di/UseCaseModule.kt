package com.questua.app.core.di

import com.questua.app.domain.repository.*
import com.questua.app.domain.usecase.auth.*
import com.questua.app.domain.usecase.exploration.*
import com.questua.app.domain.usecase.gameplay.*
import com.questua.app.domain.usecase.onboarding.*
import com.questua.app.domain.usecase.user.*
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
    fun provideGetAvailableLanguagesUseCase(repo: LanguageRepository) = GetAvailableLanguagesUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetWorldMapUseCase(contentRepo: ContentRepository, userRepo: UserRepository) =
        GetWorldMapUseCase(contentRepo, userRepo)

    @Provides
    @ViewModelScoped
    fun provideGetCityDetailsUseCase(repo: ContentRepository) = GetCityDetailsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetCityQuestPointsUseCase(repo: ContentRepository) = GetCityQuestPointsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideLoadSceneEngineUseCase(repo: ContentRepository) = LoadSceneEngineUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSubmitDialogueResponseUseCase(repo: GameRepository) = SubmitDialogueResponseUseCase(repo)

    // --- USER ---
    @Provides
    @ViewModelScoped
    fun provideGetUserProfileUseCase(repo: UserRepository) = GetUserProfileUseCase(repo)

}