package com.devgarden.finper.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object Auth : Screen("auth_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object Home : Screen("home_screen")
    object Profile : Screen("profile_screen")
    object EditProfile : Screen("edit_profile_screen")
}