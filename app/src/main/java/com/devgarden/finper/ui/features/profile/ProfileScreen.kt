package com.devgarden.finper.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.ui.viewmodel.UserViewModel
import com.devgarden.finper.data.UsuarioActual

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onBottomItemSelected: (Int) -> Unit = {},
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    val usuario = userViewModel.usuario
    ProfileScreenContent(usuario = usuario, onBottomItemSelected = onBottomItemSelected, onLogout = {
        userViewModel.cerrarSesion()
        onLogout()
    }, onEditProfile = onEditProfile)
}

@Composable
private fun ProfileScreenContent(
    usuario: UsuarioActual?,
    onBottomItemSelected: (Int) -> Unit = {},
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    var bottomSelected by remember { mutableIntStateOf(4) } // perfil seleccionado
    val effectiveName = usuario?.nombre ?: usuario?.correo?.substringBefore("@") ?: "Usuario"

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF0F4F7))) {

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(PrimaryGreen)
        ) {
            // Top Row: back, title, bell
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { /* volver */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "Perfil",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { /* notificaciones */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
            }

            // Avatar superpuesto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape),
                    color = Color.White
                ) {
                    // Placeholder avatar: icon centrado
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
        }

        // Card principal debajo del avatar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 140.dp, start = 16.dp, end = 16.dp, bottom = 80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FFF9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = effectiveName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15323B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "ID: ${usuario?.uid ?: "-"}", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(18.dp))

                // Lista de opciones
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileMenuItem(icon = Icons.Default.Edit, label = "Editar Perfil") { onEditProfile() }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileMenuItem(icon = Icons.Default.Security, label = "Seguridad")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileMenuItem(icon = Icons.Default.Settings, label = "Ajustes")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileMenuItem(icon = Icons.Default.HelpOutline, label = "Ayuda")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileMenuItem(icon = Icons.Default.ExitToApp, label = "Cerrar Sesión", tint = Color(0xFFD32F2F)) {
                        onLogout()
                    }
                }
            }
        }

        // Bottom bar reutilizable (llama al callback externo para navegación)
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
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, label: String, tint: Color = Color(0xFF3E8BFF), onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // icono en círculo azul claro
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(text = label, fontSize = 14.sp, color = Color(0xFF15323B))
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FinperTheme {
        // Preview con datos simulados
        ProfileScreenContent(usuario = UsuarioActual(uid = "1", nombre = "Preview User", correo = "preview@example.com"))
    }
}
