package com.devgarden.finper.ui.features.auth.login

import com.devgarden.finper.ui.theme.FinperTheme
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.MainActivity
import com.devgarden.finper.ui.theme.DarkTextColor
import com.devgarden.finper.ui.theme.GrayTextColor
import com.devgarden.finper.ui.theme.LightGreenGray
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.R
import com.devgarden.finper.ui.features.auth.security.BiometricAuthManager
import com.devgarden.finper.ui.features.auth.security.BiometricViewModel
import com.devgarden.finper.ui.features.auth.security.BiometricStatus

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onBiometricLoginSuccess: () -> Unit = {},
    biometricViewModel: BiometricViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? MainActivity

    var showBiometricError by remember { mutableStateOf(false) }
    var biometricErrorMessage by remember { mutableStateOf("") }

    val uiState by loginViewModel.uiState.collectAsState()
    val isLoading = uiState is LoginUiState.Loading

    LaunchedEffect(Unit) {
        biometricViewModel.initialize(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 100.dp)
                    .fillMaxHeight(0.3f)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Campo de Nombre de Usuario o Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Nombre De Usuario O Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = PrimaryGreen,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = PrimaryGreen,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onLoginClick(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Ingresar", fontSize = 18.sp, color = Color.White)
                        }
                    }

                    // Enlace Olvidaste tu contraseña
                    TextButton(
                        onClick = onForgotPasswordClick,
                        enabled = !isLoading
                    ) {
                        Text("Olvidaste tu contraseña?", color = GrayTextColor, fontSize = 14.sp)
                    }

                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightGreenGray),
                        enabled = !isLoading
                    ) {
                        Text("Registrarse", fontSize = 18.sp, color = DarkTextColor)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón de huella digital (solo si está habilitado)
                    if (biometricViewModel.biometricEnabled &&
                        biometricViewModel.biometricStatus == BiometricStatus.READY &&
                        activity != null && !isLoading) {

                        Text(
                            text = "Usa Tu Huella Para Entrar",
                            color = DarkTextColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        IconButton(
                            onClick = {
                                val biometricManager = BiometricAuthManager(context)
                                biometricManager.authenticate(
                                    activity = activity,
                                    title = "Iniciar Sesión",
                                    subtitle = "Verifica tu identidad",
                                    description = "Usa tu huella digital para iniciar sesión",
                                    onSuccess = {
                                        onBiometricLoginSuccess()
                                    },
                                    onError = { _, errorMessage ->
                                        biometricErrorMessage = errorMessage
                                        showBiometricError = true
                                    },
                                    onFailed = {
                                        biometricErrorMessage = "Huella digital no reconocida"
                                        showBiometricError = true
                                    }
                                )
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = PrimaryGreen.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(40.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Huella Digital",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Texto "o ingresa desde"
                    Text(
                        text = "o ingresa desde",
                        color = GrayTextColor,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Iconos de Login Social
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        IconButton(
                            onClick = onGoogleLoginClick,
                            enabled = !isLoading
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Login",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Overlay de carga con animación mejorada
        if (isLoading) {
            // Animaciones
            val infiniteTransition = rememberInfiniteTransition(label = "loading")

            // Animación de escala pulsante para la tarjeta
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            // Animación de opacidad para el texto
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Círculo de progreso con animación
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = PrimaryGreen,
                                strokeWidth = 5.dp
                            )

                            // Círculo interior para efecto visual
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        PrimaryGreen.copy(alpha = 0.1f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Ingresando...",
                            color = DarkTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(alpha)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Por favor espera",
                            color = GrayTextColor,
                            fontSize = 12.sp,
                            modifier = Modifier.alpha(alpha * 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Diálogo de error biométrico
    if (showBiometricError) {
        AlertDialog(
            onDismissRequest = { showBiometricError = false },
            title = { Text("Error de Autenticación") },
            text = { Text(biometricErrorMessage) },
            confirmButton = {
                TextButton(onClick = { showBiometricError = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun LoginScreenPreview() {
    FinperTheme {
        LoginScreen(
            onLoginClick = { _, _ -> },
            onRegisterClick = { },
            onForgotPasswordClick = { },
            onGoogleLoginClick = { },
            onBiometricLoginSuccess = { }
        )
    }
}
