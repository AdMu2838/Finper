package com.devgarden.finper.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.MainActivity
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.ui.features.auth.security.BiometricAuthManager
import com.devgarden.finper.ui.features.auth.security.BiometricViewModel
import com.devgarden.finper.ui.features.auth.security.BiometricStatus

/**
 * Pantalla de Seguridad con configuración de huella digital.
 */
@Composable
fun SecurityScreen(
    onBack: () -> Unit = {},
    onBottomItemSelected: (Int) -> Unit = {},
    biometricViewModel: BiometricViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    var bottomSelected by remember { mutableIntStateOf(4) }

    var showEnableDialog by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        biometricViewModel.initialize(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Header verde con botón de regreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(PrimaryGreen)
                    .padding(start = 18.dp, end = 16.dp, top = 24.dp, bottom = 15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Seguridad",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card del estado del sensor biométrico
            BiometricStatusCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                status = biometricViewModel.biometricStatus,
                statusMessage = biometricViewModel.getBiometricStatusMessage()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Card principal con el switch de huella digital
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Autenticación Biométrica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Usa tu huella digital para iniciar sesión de forma rápida y segura",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Switch para habilitar/deshabilitar huella digital
                    BiometricToggleRow(
                        enabled = biometricViewModel.biometricEnabled,
                        available = biometricViewModel.biometricStatus == BiometricStatus.READY,
                        loading = biometricViewModel.loading,
                        onToggle = { enabled ->
                            if (enabled) {
                                showEnableDialog = true
                            } else {
                                showDisableDialog = true
                            }
                        }
                    )

                    if (biometricViewModel.error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error: ${biometricViewModel.error}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card de información adicional
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "La huella digital solo funciona en este dispositivo. Deberás activarla nuevamente si cambias de teléfono.",
                        fontSize = 12.sp,
                        color = Color(0xFF5D4037)
                    )
                }
            }
        }

        // Bottom bar
        BottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            items = listOf(
                Icons.Default.Home,
                Icons.Default.BarChart,
                Icons.Default.SwapHoriz,
                Icons.Default.AccountBalanceWallet,
                Icons.Default.Person
            ),
            selectedIndex = bottomSelected,
            onItemSelected = {
                bottomSelected = it
                onBottomItemSelected(it)
            }
        )
    }

    // Diálogo para habilitar huella digital
    if (showEnableDialog && activity != null) {
        AlertDialog(
            onDismissRequest = { showEnableDialog = false },
            icon = {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Habilitar Huella Digital") },
            text = {
                Text("Verifica tu identidad con tu huella digital para habilitar esta función de seguridad.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val biometricManager = BiometricAuthManager(context)
                        biometricManager.authenticate(
                            activity = activity,
                            title = "Habilitar Huella Digital",
                            subtitle = "Verifica tu identidad",
                            description = "Usa tu huella digital para habilitar esta función",
                            onSuccess = {
                                biometricViewModel.setBiometricEnabled(true) { success, _ ->
                                    if (success) {
                                        showEnableDialog = false
                                    }
                                }
                            },
                            onError = { _, _ ->
                                showEnableDialog = false
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Verificar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnableDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para deshabilitar huella digital
    if (showDisableDialog) {
        AlertDialog(
            onDismissRequest = { showDisableDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Deshabilitar Huella Digital") },
            text = {
                Text("¿Estás seguro de que deseas deshabilitar la autenticación con huella digital? Deberás ingresar con tu contraseña cada vez.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        biometricViewModel.setBiometricEnabled(false) { success, _ ->
                            if (success) {
                                showDisableDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Deshabilitar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Card que muestra el estado del sensor biométrico del dispositivo.
 */
@Composable
private fun BiometricStatusCard(
    modifier: Modifier = Modifier,
    status: BiometricStatus,
    statusMessage: String
) {
    val (backgroundColor, iconColor, icon) = when (status) {
        BiometricStatus.READY -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle
        )
        BiometricStatus.NOT_AVAILABLE -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            Icons.Default.Cancel
        )
        else -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFFF9800),
            Icons.Default.Warning
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Estado del Sensor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Row con switch para habilitar/deshabilitar la autenticación biométrica.
 */
@Composable
private fun BiometricToggleRow(
    enabled: Boolean,
    available: Boolean,
    loading: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = if (enabled) PrimaryGreen else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Ingreso con Huella",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = if (enabled) "Activado" else "Desactivado",
                    fontSize = 12.sp,
                    color = if (enabled) PrimaryGreen else Color.Gray
                )
            }
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PrimaryGreen
            )
        } else {
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle(it) },
                enabled = available,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}
