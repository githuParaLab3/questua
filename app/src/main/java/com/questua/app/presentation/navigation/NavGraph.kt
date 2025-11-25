package com.questua.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.questua.app.presentation.auth.LoginScreen
import com.questua.app.presentation.auth.RegisterScreen
import com.questua.app.presentation.common.InitialScreen // Importe a nova tela
import com.questua.app.presentation.hub.HubScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Initial.route // <--- MUDANÇA AQUI: Começa na Inicial
    ) {
        // Rota da Tela Inicial
        composable(Screen.Initial.route) {
            InitialScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        // Rota de Login
        composable(Screen.Login.route) {
            LoginScreen(
                // No Login, "cadastre-se" também leva para Register
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Hub.route) {
                        // Ao logar, limpa a pilha (não volta pro login/inicial)
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                }
            )
        }

        // Rota de Registro
        composable(Screen.Register.route) {
            RegisterScreen(
                // Voltar leva para a tela anterior (Login ou Inicial, dependendo de onde veio)
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Hub.route) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                }
            )
        }

        // Rota do Hub
        composable(Screen.Hub.route) {
            HubScreen()
        }
    }
}