package com.questua.app.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.questua.app.presentation.hub.HubScreen
import com.questua.app.presentation.hub.components.BottomNavBar
import com.questua.app.presentation.hub.components.HubTab
import com.questua.app.presentation.profile.ProfileScreen

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToLanguages: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(HubTab.HOME) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    if (tab == HubTab.MAP) {
                        onNavigateToMap()
                    } else {
                        currentTab = tab
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
            when (currentTab) {
                HubTab.HOME -> {
                    HubScreen(
                        onNavigateToLanguages = onNavigateToLanguages
                    )
                }
                HubTab.PROFILE -> {
                    ProfileScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToHelp = { },
                        onNavigateToAdmin = onNavigateToAdmin
                    )
                }
                HubTab.PROGRESS -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tela de Progresso")
                    }
                }
                else -> {}
            }
        }
    }
}