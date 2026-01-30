package com.questua.app.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.questua.app.presentation.admin.AdminGeneralManagementScreen
import com.questua.app.presentation.admin.content.ContentDetailScreen
import com.questua.app.presentation.admin.content.achievements.AdminAchievementScreen
import com.questua.app.presentation.admin.content.characters.AdminCharacterScreen
import com.questua.app.presentation.admin.content.languages.AdminLanguageScreen
import com.questua.app.presentation.admin.feedback.AdminFeedbackScreen
import com.questua.app.presentation.admin.feedback.AdminReportDetailScreen
import com.questua.app.presentation.admin.logs.AiLogsScreen
import com.questua.app.presentation.admin.monetization.AdminMonetizationScreen
import com.questua.app.presentation.admin.monetization.AdminProductDetailScreen
import com.questua.app.presentation.admin.users.UserDetailScreen
import com.questua.app.presentation.admin.users.UserManagementScreen

fun NavGraphBuilder.adminNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.AdminHome.route,
        route = "admin_route"
    ) {
        composable(route = Screen.AdminHome.route) {
            AdminGeneralManagementScreen(
                navController = navController,
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

        composable(
            route = Screen.AdminMonetizationDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            AdminProductDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AdminReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            AdminReportDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AdminUserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            UserDetailScreen(navController = navController)
        }

        composable(route = Screen.AdminLanguages.route) {
            AdminLanguageScreen(navController = navController)
        }

        composable(route = Screen.AdminCharacters.route) {
            AdminCharacterScreen(navController = navController)
        }

        composable(route = Screen.AdminAchievements.route) {
            AdminAchievementScreen(navController = navController)
        }

    }
}