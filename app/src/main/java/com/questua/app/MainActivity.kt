package com.questua.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.questua.app.core.ui.components.AchievementOverlay
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.core.ui.theme.QuestuaTheme
import com.questua.app.core.ui.theme.ThemeManager
import com.questua.app.presentation.main.MainViewModel
import com.questua.app.presentation.navigation.SetupNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var achievementMonitor: AchievementMonitor

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        achievementMonitor.initialize()

        setContent {
            val isDarkThemeGlobal by themeManager.isDarkTheme.collectAsState()
            val startDestination by mainViewModel.startDestination.collectAsState()

            if (startDestination != null) {
                QuestuaTheme(darkTheme = isDarkThemeGlobal) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                        Box(modifier = Modifier.fillMaxSize()) {
                            SetupNavGraph(
                                navController = navController,
                                startDestination = startDestination!!
                            )

                            AchievementOverlay(monitor = achievementMonitor)
                        }
                    }
                }
            }
        }
    }
}