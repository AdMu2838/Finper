package com.devgarden.finper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.features.launch.SplashScreen
import com.devgarden.finper.ui.features.auth.AuthScreen
import com.devgarden.finper.ui.features.onboarding.OnboardingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinperTheme {
                val context = LocalContext.current

                // Estado para el splash
                var showSplash by remember { mutableStateOf(true) }

                // Leer SharedPreferences para saber si ya se mostró el onboarding
                val prefs = context.getSharedPreferences("finper_prefs", Context.MODE_PRIVATE)
                var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_shown", false)) }

                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    if (showOnboarding) {
                        OnboardingScreen(onFinish = {
                            prefs.edit().putBoolean("onboarding_shown", true).apply()
                            showOnboarding = false
                        })
                    } else {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            AuthScreen(
                                onLoginClicked = {
                                    val intent = Intent(context, HomeActivity::class.java)
                                    context.startActivity(intent)
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
                        }
                    }
                }
            }
        }
    }
}

