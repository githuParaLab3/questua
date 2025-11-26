package com.questua.app.presentation.hub

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.presentation.hub.components.BottomNavBar
import com.questua.app.presentation.hub.components.HubDashboard
import com.questua.app.presentation.hub.components.HubTab

@Composable
fun HubScreen(
    onNavigateToMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    viewModel: HubViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var currentTab by remember { mutableStateOf(HubTab.HOME) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = currentTab,
                onTabSelected = {
                    currentTab = it
                    when(it) {
                        HubTab.MAP -> onNavigateToMap()
                        HubTab.PROFILE -> onNavigateToProfile()
                        HubTab.RANK -> onNavigateToProgress()
                        else -> {}
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentTab == HubTab.HOME) {
                HubDashboard(
                    userName = state.user?.displayName ?: "Explorador",
                    userStreak = state.activeLanguage?.streakDays ?: 0,
                    userXp = state.activeLanguage?.xpTotal ?: 0,
                    onQuestClick = { /* Ação de clique na missão */ }
                )
            } else {
                // Placeholders para outras abas se não houver navegação externa
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Em breve...")
                }
            }
        }
    }
}