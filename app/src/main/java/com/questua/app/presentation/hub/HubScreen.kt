package com.questua.app.presentation.hub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.presentation.hub.components.HubDashboard

@Composable
fun HubScreen(
    onNavigateToLanguages: () -> Unit,
    viewModel: HubViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    HubDashboard(
        userName = state.user?.displayName ?: "Explorador",
        userStreak = state.activeLanguage?.streakDays ?: 0,
        userXp = state.activeLanguage?.xpTotal ?: 0,
        languageCode = state.currentLanguageDetails?.code,
        languageIconUrl = state.currentLanguageDetails?.iconUrl,
        onQuestClick = { },
        onLanguageClick = onNavigateToLanguages
    )
}