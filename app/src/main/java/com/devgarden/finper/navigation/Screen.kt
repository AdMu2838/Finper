package com.devgarden.finper.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object Auth : Screen("auth_screen")
    object Login : Screen("login_screen")
    // Aquí añadirás más pantallas en el futuro, como Register, Home, etc.
}