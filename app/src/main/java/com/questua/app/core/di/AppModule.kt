package com.questua.app.core.di

import com.questua.app.core.network.TokenManager
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.domain.usecase.user.GetAchievementDetailsUseCase
import com.questua.app.domain.usecase.user.GetUserAchievementsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideAchievementMonitor(
        getUserAchievementsUseCase: GetUserAchievementsUseCase,
        getAchievementDetailsUseCase: GetAchievementDetailsUseCase,
        tokenManager: TokenManager
    ): AchievementMonitor {
        return AchievementMonitor(
            getUserAchievementsUseCase,
            getAchievementDetailsUseCase,
            tokenManager
        )
    }
}