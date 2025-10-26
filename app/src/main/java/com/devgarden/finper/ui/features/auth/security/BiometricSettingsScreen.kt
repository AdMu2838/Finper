package com.devgarden.finper.ui.features.auth.security

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.MainActivity

/**
 * Pantalla de configuración de seguridad biométrica.
 */
@Composable
fun BiometricSettingsScreen(
    modifier: Modifier = Modifier,
    biometricViewModel: BiometricViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    var showEnableDialog by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        biometricViewModel.initialize(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Seguridad",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Estado del sensor biométrico
        BiometricStatusCard(
            status = biometricViewModel.biometricStatus,
            statusMessage = biometricViewModel.getBiometricStatusMessage()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Switch para habilitar/deshabilitar huella digital
        BiometricToggleCard(
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
                fontSize = 14.sp
            )
        }
    }

    // Diálogo para habilitar huella digital
    if (showEnableDialog && activity != null) {
        AlertDialog(
            onDismissRequest = { showEnableDialog = false },
            icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
            title = { Text("Habilitar Huella Digital") },
            text = { Text("Verifica tu identidad con tu huella digital para habilitar esta función.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val biometricManager = BiometricAuthManager(context)
                        biometricManager.authenticate(
                            activity = activity as androidx.fragment.app.FragmentActivity,
                            title = "Habilitar Huella Digital",
                            subtitle = "Verifica tu identidad",
                            description = "Usa tu huella digital para habilitar esta función",
                            onSuccess = {
                                biometricViewModel.setBiometricEnabled(true) { success, error ->
                                    if (success) {
                                        showEnableDialog = false
                                    }
                                }
                            },
                            onError = { _, errorMessage ->
                                showEnableDialog = false
                            }
                        )
                    }
                ) {
                    Text("Continuar")
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
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Deshabilitar Huella Digital") },
            text = { Text("¿Estás seguro de que deseas deshabilitar la autenticación con huella digital?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        biometricViewModel.setBiometricEnabled(false) { success, _ ->
                            if (success) {
                                showDisableDialog = false
                            }
                        }
                    }
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
 * Card que muestra el estado del sensor biométrico.
 */
@Composable
private fun BiometricStatusCard(
    status: BiometricStatus,
    statusMessage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                BiometricStatus.READY -> Color(0xFFE8F5E9)
                else -> Color(0xFFFFF3E0)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (status == BiometricStatus.READY) {
                    Icons.Default.Fingerprint
                } else {
                    Icons.Default.Warning
                },
                contentDescription = null,
                tint = when (status) {
                    BiometricStatus.READY -> Color(0xFF4CAF50)
                    else -> Color(0xFFFF9800)
                },
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Estado del Sensor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
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
 * Card con switch para habilitar/deshabilitar la autenticación biométrica.
 */
@Composable
private fun BiometricToggleCard(
    enabled: Boolean,
    available: Boolean,
    loading: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ingreso con Huella Digital",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (enabled) "Activado" else "Desactivado",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle(it) },
                    enabled = available
                )
            }
        }
    }
}
