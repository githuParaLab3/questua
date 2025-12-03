package com.questua.app.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.questua.app.presentation.admin.AdminGeneralManagementScreen
import com.questua.app.presentation.admin.content.ContentDetailScreen
import com.questua.app.presentation.admin.feedback.AdminFeedbackScreen
import com.questua.app.presentation.admin.logs.AiLogsScreen
import com.questua.app.presentation.admin.monetization.AdminMonetizationScreen
import com.questua.app.presentation.admin.users.UserManagementScreen

fun NavGraphBuilder.adminNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.AdminHome.route,
        route = "admin_route"
    ) {
        composable(route = Screen.AdminHome.route) {
            AdminGeneralManagementScreen(
                navController = navController,
                onNavigateToDetail = { type ->
                    navController.navigate(Screen.AdminContentDetail.passType(type))
                },
                onNavigateToLogs = {
                    navController.navigate(Screen.AdminLogs.route)
                },
                onExitAdmin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AdminContentDetail.route,
            arguments = listOf(navArgument("contentType") { type = NavType.StringType })
        ) { backStackEntry ->
            val contentType = backStackEntry.arguments?.getString("contentType") ?: ""
            ContentDetailScreen(
                navController = navController,
                contentType = contentType,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.AdminLogs.route) {
            AiLogsScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.AdminUsers.route) {
            UserManagementScreen(navController = navController)
        }

        composable(route = Screen.AdminFeedbackList.route) {
            AdminFeedbackScreen(navController = navController)
        }

        composable(route = Screen.AdminMonetization.route) {
            AdminMonetizationScreen(navController = navController)
        }
    }
}