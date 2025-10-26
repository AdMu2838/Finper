package com.devgarden.finper.ui.features.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.components.SummaryCard

/**
 * Pantalla para el segundo botón del BottomBar (índice 1) — "Análisis".
 * Esta vista es autocontenida y sirve como punto de partida. Puedes enlazarla
 * con la navegación principal o adaptarla según tu ViewModel/estado.
 */

@Composable
fun AnalysisScreen(
    onBack: () -> Unit = {},
    selectedIndex: Int = 1,
    onBottomItemSelected: (Int) -> Unit = {}
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF0F4F7)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header verde
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00B984))
                    .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Análisis",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { /* acciones adicionales */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tarjeta resumen (usa el componente existente)
                SummaryCard(
                    modifier = Modifier.fillMaxWidth(),
                    balanceLabel = "Saldo total",
                    balanceValue = "S/. 7,783.00",
                    expenseLabel = "Gastos totales",
                    expenseValue = "S/. 1,187.40",
                    progressLabel = ""
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Area blanca redondeada con contenido
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Filtros tipo Daily/Weekly/Monthly/Yearly
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // pequeños chips simulados
                        Text(text = "Diario", color = Color.Gray)
                        Text(text = "Semanal", color = Color.Gray)
                        Text(text = "Mensual", color = Color(0xFF00B984))
                        Text(text = "Anual", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gráfico de ejemplo (placeholder)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFF6FFF9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "[Gráfico de barras aquí]", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // BottomBar — el segundo botón (índice 1) estará seleccionado por defecto
            BottomBar(
                modifier = Modifier.fillMaxWidth(),
                items = listOf(
                    Icons.Default.Home,
                    Icons.Default.BarChart,
                    Icons.Default.SwapHoriz,
                    Icons.Default.Layers,
                    Icons.Default.Person
                ),
                selectedIndex = selectedIndex,
                onItemSelected = onBottomItemSelected
            )
        }
    }
}
