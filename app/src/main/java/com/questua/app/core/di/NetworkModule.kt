package com.questua.app.core.di

import com.questua.app.core.common.Constants
import com.questua.app.core.network.AuthInterceptor
import com.questua.app.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideLanguageApi(retrofit: Retrofit): LanguageApi = retrofit.create(LanguageApi::class.java)

    @Provides
    @Singleton
    fun provideCityApi(retrofit: Retrofit): CityApi = retrofit.create(CityApi::class.java)

    @Provides
    @Singleton
    fun provideQuestPointApi(retrofit: Retrofit): QuestPointApi = retrofit.create(QuestPointApi::class.java)

    @Provides
    @Singleton
    fun provideQuestApi(retrofit: Retrofit): QuestApi = retrofit.create(QuestApi::class.java)

    @Provides
    @Singleton
    fun provideSceneDialogueApi(retrofit: Retrofit): SceneDialogueApi = retrofit.create(SceneDialogueApi::class.java)

    @Provides
    @Singleton
    fun provideUserAccountApi(retrofit: Retrofit): UserAccountApi = retrofit.create(UserAccountApi::class.java)

    @Provides
    @Singleton
    fun provideUserLanguageApi(retrofit: Retrofit): UserLanguageApi = retrofit.create(UserLanguageApi::class.java)

    @Provides
    @Singleton
    fun provideUserQuestApi(retrofit: Retrofit): UserQuestApi = retrofit.create(UserQuestApi::class.java)

    @Provides
    @Singleton
    fun provideUserAchievementApi(retrofit: Retrofit): UserAchievementApi = retrofit.create(UserAchievementApi::class.java)

    @Provides
    @Singleton
    fun provideAchievementApi(retrofit: Retrofit): AchievementApi = retrofit.create(AchievementApi::class.java)

    @Provides
    @Singleton
    fun provideProductApi(retrofit: Retrofit): ProductApi = retrofit.create(ProductApi::class.java)

    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi = retrofit.create(PaymentApi::class.java)

    @Provides
    @Singleton
    fun provideTransactionRecordApi(retrofit: Retrofit): TransactionRecordApi = retrofit.create(TransactionRecordApi::class.java)

    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): ReportApi = retrofit.create(ReportApi::class.java)

    @Provides
    @Singleton
    fun provideCharacterEntityApi(retrofit: Retrofit): CharacterEntityApi = retrofit.create(CharacterEntityApi::class.java)

    @Provides
    @Singleton
    fun provideAiGenerationApi(retrofit: Retrofit): AiGenerationApi = retrofit.create(AiGenerationApi::class.java)

    @Provides
    @Singleton
    fun provideAiGenerationLogApi(retrofit: Retrofit): AiGenerationLogApi = retrofit.create(AiGenerationLogApi::class.java)

    @Provides
    @Singleton
    fun provideUploadApi(retrofit: Retrofit): UploadApi = retrofit.create(UploadApi::class.java)
}