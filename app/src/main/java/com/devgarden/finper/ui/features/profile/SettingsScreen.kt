package com.devgarden.finper.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onBottomItemSelected: (Int) -> Unit = {}
) {
    var bottomSelected by remember { mutableIntStateOf(4) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var darkModeEnabled by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F7))
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(PrimaryGreen)
                    .padding(start = 18.dp, end = 16.dp, top = 24.dp, bottom = 15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                    Text(
                        text = "Ajustes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card con opciones
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Preferencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Notificaciones", modifier = Modifier.weight(1f))
                        Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Activar Modo Oscuro", modifier = Modifier.weight(1f))
                        Switch(checked = darkModeEnabled, onCheckedChange = { darkModeEnabled = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
}
