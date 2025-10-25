package com.devgarden.finper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devgarden.finper.ui.features.auth.AuthScreen
import com.devgarden.finper.ui.features.auth.login.LoginScreen
import com.devgarden.finper.ui.features.launch.SplashScreen
import com.devgarden.finper.ui.features.onboarding.OnboardingScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                // Navega al onboarding y elimina el splash del historial
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        // Pantalla 2: Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                // Navega a la pantalla de Auth y elimina el onboarding del historial
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        // Pantalla 3: Auth Screen (con botones de Ingresar/Registrar)
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginClicked = { navController.navigate(Screen.Login.route) },
                onRegisterClicked = { /* navController.navigate(Screen.Register.route) */ },
                onForgotPasswordClicked = { /* navController.navigate(Screen.ForgotPassword.route) */ }
            )
        }

        // Pantalla 4: Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password -> /* Lógica de login */ },
                onRegisterClick = { /* navController.navigate(Screen.Register.route) */ },
                onForgotPasswordClick = { /* navController.navigate(Screen.ForgotPassword.route) */ },
                onGoogleLoginClick = { /* Lógica de login con Google */ },
                //onFacebookLoginClick = { /* Lógica de login con Facebook */ }
            )
        }
    }
}