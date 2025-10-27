package com.devgarden.finper.ui.features.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.data.UsuarioActual
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(userViewModel: UserViewModel, onBack: () -> Unit = {}) {
    val usuario = userViewModel.usuario
    val context = LocalContext.current

    EditProfileContent(usuario = usuario, onBack = onBack) { nombre, telefono, correo ->
        // llamar al ViewModel para actualizar
        userViewModel.actualizarPerfil(nombre = nombre, telefono = telefono, correo = correo) { success, message ->
            if (success) {
                Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_LONG).show()
                onBack()
            } else {
                Toast.makeText(context, message ?: "Error al actualizar perfil", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(usuario: UsuarioActual?, onBack: () -> Unit = {}, onSave: (String, String, String) -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var telefono by remember { mutableStateOf(usuario?.telefono ?: "") }
    var correo by remember { mutableStateOf(usuario?.correo ?: "") }
    // Notificaciones y modo oscuro se han movido a SettingsScreen.kt
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)
        .padding(16.dp)) {

        TopAppBar(
            title = { Text(text = "Editar mi Perfil", fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen, titleContentColor = MaterialTheme.colorScheme.onPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card-like area
        Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Ajustes De Cuenta", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Celular") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Correo: campo no editable
                OutlinedTextField(
                    value = correo,
                    onValueChange = { /* no-op: campo no editable */ },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Los toggles de Notificaciones y Modo Oscuro han sido movidos a la pantalla de Ajustes.
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (nombre.isBlank() || correo.isBlank()) {
                            Toast.makeText(context, "Nombre y correo son obligatorios", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        onSave(nombre.trim(), telefono.trim(), correo.trim())
                        // isSaving se resetea desde el callback del ViewModel cuando termine; aquí dejamos true sá como indicador
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(text = if (isSaving) "Guardando..." else "Actualizar Perfil", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun EditProfilePreview() {
    FinperTheme {
        EditProfileContent(usuario = UsuarioActual(uid = "1", nombre = "Preview User", correo = "preview@example.com", telefono = "+1 555 555 555")) { _, _, _ -> }
    }
}
