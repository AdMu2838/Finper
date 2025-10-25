package com.devgarden.finper.navigation

import android.content.res.Resources
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devgarden.finper.R
import com.devgarden.finper.ui.features.auth.AuthScreen
import com.devgarden.finper.ui.features.auth.login.LoginScreen
import com.devgarden.finper.ui.features.launch.SplashScreen
import com.devgarden.finper.ui.features.onboarding.OnboardingScreen
import com.devgarden.finper.ui.features.auth.create_account.RegisterScreen
import com.devgarden.finper.ui.features.auth.forgot_password.ForgotPasswordScreen
import com.devgarden.finper.ui.features.auth.forgot_password.ForgotPasswordViewModel
import com.devgarden.finper.ui.features.auth.forgot_password.ForgotPasswordUiState
import com.devgarden.finper.ui.features.auth.login.LoginViewModel
import com.devgarden.finper.ui.features.auth.login.LoginUiState
import com.devgarden.finper.ui.features.home.HomeScreen
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val firebaseAuth = FirebaseAuth.getInstance()

    // ViewModel para Login (usado por el flujo de Google Sign-In)
    val loginViewModel: LoginViewModel = viewModel()
    val loginUiState by loginViewModel.uiState.collectAsState(initial = LoginUiState.Idle)

    // ViewModel para Forgot Password
    val forgotViewModel: ForgotPasswordViewModel = viewModel()
    val forgotUiState by forgotViewModel.uiState.collectAsState(initial = ForgotPasswordUiState.Idle)

    // Configuración de Google Sign-In
    val defaultWebClientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Resources.NotFoundException) {
        null
    }

    val googleSignInClient = defaultWebClientId?.let { id ->
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(id)
            .requestEmail()
            .build()
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idTokenAny = account?.idToken
                val idTokenStr = idTokenAny as? String
                if (idTokenStr != null) {
                    // Pasamos el idToken al ViewModel para completar el sign-in en Firebase
                    loginViewModel.signInWithGoogle(idTokenStr)
                } else {
                    Toast.makeText(context, "No se obtuvo idToken", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Error Google Sign-In: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Observa cambios en el estado de login y navega internamente a Home en caso de éxito
    LaunchedEffect(loginUiState) {
        when (loginUiState) {
            is LoginUiState.Success -> {
                // Navegar a Home y limpiar back stack de pantallas de autenticación
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is LoginUiState.Error -> {
                val msg = (loginUiState as LoginUiState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    // Observa estado de Forgot Password y muestra mensajes / navegación
    LaunchedEffect(forgotUiState) {
        when (forgotUiState) {
            is ForgotPasswordUiState.Success -> {
                Toast.makeText(context, "Se envió el correo de recuperación", Toast.LENGTH_LONG).show()
                // Navegar a Login y limpiar back stack de Register/Forgot
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
                forgotViewModel.resetState()
            }
            is ForgotPasswordUiState.Error -> {
                val msg = (forgotUiState as ForgotPasswordUiState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                // Reseteamos a Idle para permitir reintento
                forgotViewModel.resetState()
            }
            else -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                // Si ya hay sesión de Firebase, ir directamente a Home
                val target = when {
                    firebaseAuth.currentUser != null -> Screen.Home.route
                    OnboardingPrefs.isOnboardingSeen(context) -> Screen.Auth.route
                    else -> Screen.Onboarding.route
                }

                navController.navigate(target) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        // Pantalla 2: Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                OnboardingPrefs.setOnboardingSeen(context)
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        // Pantalla 3: Auth Screen (con botones de Ingresar/Registrar)
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginClicked = { navController.navigate(Screen.Login.route) },
                onRegisterClicked = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClicked = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        // Pantalla 4: Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password -> /* Lógica de login */ },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                onGoogleLoginClick = {
                    if (googleSignInClient != null) {
                        val signInIntent = try {
                            googleSignInClient::class.java.getMethod("getSignInIntent").invoke(googleSignInClient) as? android.content.Intent
                        } catch (ex: Exception) {
                            null
                        }
                        if (signInIntent != null) {
                            googleSignInLauncher.launch(signInIntent)
                        } else {
                            try {
                                val directIntent = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signInIntent
                                googleSignInLauncher.launch(directIntent)
                            } catch (ex: Exception) {
                                Toast.makeText(context, "No se pudo iniciar Google Sign-In. Revisa configuración y sincroniza Gradle.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Configuración de Google Sign-In faltante. Revisa strings.xml y google-services.json", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        // Pantalla 5: Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 6: Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNextClick = { email -> forgotViewModel.sendPasswordResetEmail(email) },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onGoogleLoginClick = {
                    if (googleSignInClient != null) {
                        val signInIntent = try {
                            googleSignInClient::class.java.getMethod("getSignInIntent").invoke(googleSignInClient) as? android.content.Intent
                        } catch (ex: Exception) {
                            null
                        }
                        if (signInIntent != null) {
                            googleSignInLauncher.launch(signInIntent)
                        } else {
                            Toast.makeText(context, "No se pudo iniciar Google Sign-In. Revisa configuración y sincroniza Gradle.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Configuración de Google Sign-In faltante. Revisa strings.xml y google-services.json", Toast.LENGTH_LONG).show()
                    }
                },
                isLoading = forgotUiState is ForgotPasswordUiState.Loading
            )
        }

        // Pantalla Home tras login
        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}
