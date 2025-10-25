package com.devgarden.finper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.features.launch.SplashScreen
import com.devgarden.finper.ui.features.auth.AuthScreen
import com.devgarden.finper.ui.features.onboarding.OnboardingScreen
import com.devgarden.finper.ui.features.auth.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinperTheme {
                val context = LocalContext.current
                var showSplash by remember { mutableStateOf(true) }
                var showLoginScreen by remember { mutableStateOf(false) }
                val prefs = context.getSharedPreferences("finper_prefs", MODE_PRIVATE)
                var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_shown", false)) }

                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    if (showOnboarding) {
                        OnboardingScreen(onFinish = {
                            // Usar la extensión KTX edit para evitar advertencias
                            prefs.edit { putBoolean("onboarding_shown", true) }
                            showOnboarding = false
                        })
                    } else {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            // Usar innerPadding proporcionado por Scaffold
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                if (!showLoginScreen) {
                                    AuthScreen(
                                        onLoginClicked = {
                                            showLoginScreen = true
                                        },
                                        onRegisterClicked = {
                                            val intent = Intent(context, RegisterActivity::class.java)
                                            context.startActivity(intent)
                                        },
                                        onForgotPasswordClicked = {
                                            val intent = Intent(context, FormLoginActivity::class.java)
                                            context.startActivity(intent)
                                        }
                                    )
                                } else {
                                    // Mostrar el composable LoginScreen desde features
                                    LoginScreen(
                                        onLoginClick = { email, password ->
                                            // Simulación simple: si email y password no están vacíos, abrir HomeActivity
                                            if (email.isNotBlank() && password.isNotBlank()) {
                                                val intent = Intent(context, HomeActivity::class.java)
                                                context.startActivity(intent)
                                            } else {
                                                // Si quieres, aquí podríamos mostrar un Snackbar o Toast; por simplicidad no hacemos nada
                                            }
                                        },
                                        onRegisterClick = {
                                            // Volver a AuthScreen y abrir RegisterActivity
                                            showLoginScreen = false
                                            val intent = Intent(context, RegisterActivity::class.java)
                                            context.startActivity(intent)
                                        },
                                        onForgotPasswordClick = {
                                            // Volver a AuthScreen y abrir FormLoginActivity (simulado)
                                            showLoginScreen = false
                                            val intent = Intent(context, FormLoginActivity::class.java)
                                            context.startActivity(intent)
                                        },
                                        onGoogleLoginClick = {
                                            // Simular login con Google -> abrir Home
                                            val intent = Intent(context, HomeActivity::class.java)
                                            context.startActivity(intent)
                                        },
                                        /* onFacebookLoginClick = {
                                            // Simular login con Facebook -> abrir Home
                                            val intent = Intent(context, HomeActivity::class.java)
                                            context.startActivity(intent)
                                        }*/
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
