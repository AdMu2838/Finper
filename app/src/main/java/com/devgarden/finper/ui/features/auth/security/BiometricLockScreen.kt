package com.devgarden.finper.ui.features.auth.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.MainActivity
import com.devgarden.finper.ui.theme.PrimaryGreen

/**
 * Pantalla que solicita autenticación biométrica al iniciar la app.
 */
@Composable
fun BiometricLockScreen(
    onAuthenticated: () -> Unit,
    onUsernamePassword: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var attemptCount by remember { mutableStateOf(0) }

    // Mostrar automáticamente el prompt biométrico al entrar
    LaunchedEffect(Unit) {
        if (activity != null) {
            val biometricManager = BiometricAuthManager(context)
            biometricManager.authenticate(
                activity = activity,
                title = "Desbloquear Finper",
                subtitle = "Verifica tu identidad",
                description = "Usa tu huella digital para acceder",
                onSuccess = {
                    onAuthenticated()
                },
                onError = { errorCode, errString ->
                    // Si el usuario cancela (código 13), permitir usar contraseña
                    if (errorCode == 13 || errorCode == 10) {
                        // Cancelado por el usuario o error negativo
                        errorMessage = "Autenticación cancelada"
                        showError = true
                    } else {
                        errorMessage = errString
                        showError = true
                    }
                },
                onFailed = {
                    attemptCount++
                    if (attemptCount >= 3) {
                        errorMessage = "Demasiados intentos fallidos"
                        showError = true
                    } else {
                        errorMessage = "Huella no reconocida. Intento ${attemptCount}/3"
                        showError = true
                    }
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícono grande de huella
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Huella Digital",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Desbloquear Finper",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Toca el sensor para continuar",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            if (showError) {
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Botón para reintentar
            if (showError && activity != null) {
                Button(
                    onClick = {
                        showError = false
                        val biometricManager = BiometricAuthManager(context)
                        activity.let { fragmentActivity ->
                            biometricManager.authenticate(
                                activity = fragmentActivity as androidx.fragment.app.FragmentActivity,
                                title = "Desbloquear Finper",
                                subtitle = "Verifica tu identidad",
                                description = "Usa tu huella digital para acceder",
                                onSuccess = {
                                    onAuthenticated()
                                },
                                onError = { errorCode, errString ->
                                    if (errorCode == 13 || errorCode == 10) {
                                        errorMessage = "Autenticación cancelada"
                                        showError = true
                                    } else {
                                        errorMessage = errString
                                        showError = true
                                    }
                                },
                                onFailed = {
                                    attemptCount++
                                    if (attemptCount >= 3) {
                                        errorMessage = "Demasiados intentos fallidos"
                                        showError = true
                                    } else {
                                        errorMessage = "Huella no reconocida. Intento ${attemptCount}/3"
                                        showError = true
                                    }
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Intentar de nuevo",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Botón para usar contraseña
            TextButton(
                onClick = onUsernamePassword,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text(
                    text = "Usar nombre de usuario y contraseña",
                    fontSize = 14.sp
                )
            }
        }
    }
}
