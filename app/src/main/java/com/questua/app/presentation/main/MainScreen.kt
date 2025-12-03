package com.questua.app.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.questua.app.core.ui.components.BottomNavBar
import com.questua.app.core.ui.components.HubTab
import com.questua.app.domain.enums.ReportType
import com.questua.app.presentation.exploration.worldmap.WorldMapScreen
import com.questua.app.presentation.hub.HubScreen
import com.questua.app.presentation.profile.ProfileScreen
import com.questua.app.presentation.progress.ProgressScreen

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToLanguages: () -> Unit,
    onNavigateToCity: (String) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToFeedback: (ReportType) -> Unit,
    navController: NavController
) {
    var currentTab by rememberSaveable { mutableStateOf(HubTab.HOME) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                HubTab.HOME -> {
                    HubScreen(
                        onNavigateToLanguages = onNavigateToLanguages
                    )
                }
                HubTab.MAP -> {
                    WorldMapScreen(
                        onNavigateBack = null,
                        onNavigateToCity = onNavigateToCity
                    )
                }
                HubTab.PROFILE -> {
                    ProfileScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToHelp = onNavigateToHelp,
                        onNavigateToAdmin = onNavigateToAdmin,
                        onNavigateToFeedback = { onNavigateToFeedback(ReportType.FEEDBACK) },
                        onNavigateBack = null,
                        navController = navController
                    )
                }
                HubTab.PROGRESS -> {
                    ProgressScreen()
                }
            }
        }
    }
}