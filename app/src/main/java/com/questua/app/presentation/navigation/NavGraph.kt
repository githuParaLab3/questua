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
import com.questua.app.presentation.hub.HubScreen
import com.questua.app.presentation.onboarding.LanguageSelectionScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Initial.route
    ) {
        // Tela Inicial
        composable(route = Screen.Initial.route) {
            InitialScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                // Fluxo de cadastro começa escolhendo o idioma
                onNavigateToRegister = { navController.navigate(Screen.LanguageSelection.route) }
            )
        }

        // Tela de Seleção de Idioma
        composable(route = Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onLanguageSelected = { languageId ->
                    // Navega para o registro passando o ID escolhido
                    navController.navigate(Screen.Register.passId(languageId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Tela de Registro (Recebe languageId)
        composable(
            route = Screen.Register.route,
            arguments = listOf(navArgument("languageId") { type = NavType.StringType })
        ) {
            // O ViewModel pega o ID automaticamente pelo SavedStateHandle,
            // então não precisamos passar explicitamente via construtor da Screen
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Tela de Login
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

        // Tela Principal (Hub)
        composable(route = Screen.Home.route) {
            HubScreen()
        }
    }
}