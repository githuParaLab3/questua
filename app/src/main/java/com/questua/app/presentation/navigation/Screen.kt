package com.questua.app.presentation.navigation

sealed class Screen(val route: String) {
    object Initial : Screen("initial_screen")
    object Login : Screen("login_screen")
    object LanguageSelection : Screen("language_selection_screen")

    object Register : Screen("register_screen/{languageId}") {
        fun passId(languageId: String): String {
            return "register_screen/$languageId"
        }
    }

    object Home : Screen("home_screen")
    object LanguagesList : Screen("languages_list_screen")
    object WorldMap : Screen("world_map_screen") // <--- NOVA ROTA
}