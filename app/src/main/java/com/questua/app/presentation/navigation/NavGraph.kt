package com.questua.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.questua.app.presentation.auth.LoginScreen
import com.questua.app.presentation.auth.RegisterScreen
import com.questua.app.presentation.common.InitialScreen
import com.questua.app.presentation.languages.LanguagesListScreen
import com.questua.app.presentation.main.MainScreen
import com.questua.app.presentation.onboarding.LanguageSelectionScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Initial.route
    ) {
        composable(route = Screen.Initial.route) {
            InitialScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.LanguageSelection.route) }
            )
        }

        composable(route = Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onLanguageSelected = { languageId ->
                    navController.navigate(Screen.Register.passId(languageId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Register.route,
            arguments = listOf(navArgument("languageId") { type = NavType.StringType })
        ) {
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.LanguageSelection.route) }
            )
        }

        // Rota Principal (Agora contém o Mapa)
        composable(route = Screen.Home.route) {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                onNavigateToLanguages = {
                    navController.navigate(Screen.LanguagesList.route)
                },
                onNavigateToCity = { cityId ->
                    // Implementar rota de detalhes da cidade aqui
                    // navController.navigate(Screen.CityDetail.passId(cityId))
                },
                onNavigateToAdmin = {
                    // Se tiver rota de admin
                }
            )
        }

        composable(route = Screen.LanguagesList.route) {
            LanguagesListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNewLanguage = {
                    navController.navigate(Screen.LanguageSelection.route)
                }
            )
        }

        // A rota Screen.WorldMap foi removida daqui pois agora é uma aba interna da Home
    }
}