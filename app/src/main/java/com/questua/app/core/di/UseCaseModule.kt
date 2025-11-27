package com.questua.app.core.di

import com.questua.app.domain.repository.*
import com.questua.app.domain.usecase.auth.*
import com.questua.app.domain.usecase.exploration.*
import com.questua.app.domain.usecase.gameplay.*
import com.questua.app.domain.usecase.language_learning.*
import com.questua.app.domain.usecase.monetization.*
import com.questua.app.domain.usecase.onboarding.*
import com.questua.app.domain.usecase.user.*
import com.questua.app.domain.usecase.quest.*
import com.questua.app.data.remote.api.AchievementApi
import com.questua.app.domain.usecase.feedback.SendReportUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    // --- AUTH ---
    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(repo: AuthRepository) = LoginUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideRegisterUseCase(repo: AuthRepository) = RegisterUseCase(repo)

    // --- ONBOARDING ---
    @Provides
    @ViewModelScoped
    fun provideGetAvailableLanguagesUseCase(repo: LanguageRepository) = GetAvailableLanguagesUseCase(repo)

    // --- EXPLORATION ---
    @Provides
    @ViewModelScoped
    fun provideGetWorldMapUseCase(contentRepo: ContentRepository) =
        GetWorldMapUseCase(contentRepo)

    @Provides
    @ViewModelScoped
    fun provideGetCityDetailsUseCase(repo: ContentRepository) = GetCityDetailsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetCityQuestPointsUseCase(repo: ContentRepository) = GetCityQuestPointsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetQuestPointDetailsUseCase(repo: ContentRepository) = GetQuestPointDetailsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetQuestPointQuestsUseCase(repo: ContentRepository) = GetQuestPointQuestsUseCase(repo)

    // --- GAMEPLAY ---
    @Provides
    @ViewModelScoped
    fun provideStartQuestUseCase(repo: GameRepository) = StartQuestUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideLoadSceneEngineUseCase(repo: ContentRepository) =
        LoadSceneEngineUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSubmitDialogueResponseUseCase(repo: GameRepository) = SubmitDialogueResponseUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetNextDialogueUseCase(repo: GameRepository) = GetNextDialogueUseCase(repo)

    // --- USER ---
    @Provides
    @ViewModelScoped
    fun provideGetUserProfileUseCase(repo: UserRepository) = GetUserProfileUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetUserLanguagesUseCase(repo: UserRepository) = GetUserLanguagesUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetLanguageDetailsUseCase(repo: LanguageRepository) = GetLanguageDetailsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSetLearningLanguageUseCase(repo: UserRepository) = SetLearningLanguageUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideToggleAdminModeUseCase(repo: UserRepository) = ToggleAdminModeUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideUpdateUserProfileUseCase(repo: UserRepository) = UpdateUserProfileUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetUserAchievementsUseCase(repo: UserRepository) = GetUserAchievementsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetAchievementDetailsUseCase(api: AchievementApi) = GetAchievementDetailsUseCase(api)

    // --- FEEDBACK ---
    @Provides
    @ViewModelScoped
    fun provideSendReportUseCase(repo: UserRepository) = SendReportUseCase(repo)
}