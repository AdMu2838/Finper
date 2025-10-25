package com.devgarden.finper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import com.devgarden.finper.ui.theme.FinperTheme

class DynamicListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DynamicListScreen()
                }
            }
        }
    }
}

private data class Transaction(
    val id: Int,
    val date: String,
    val description: String,
    val category: String,
    val amount: Double,
    val isExpense: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicListScreen() {
    // Generar 20 registros simulados relevantes para finanzas personales
    val items = remember {
        (1..20).map {
            Transaction(
                id = it,
                date = "2025-10-${(it % 28) + 1}",
                description = when (it % 6) {
                    0 -> "Pago de servicios"
                    1 -> "Compra supermercado"
                    2 -> "Transferencia recibida"
                    3 -> "Cena / Restaurante"
                    4 -> "Sueldo"
                    else -> "Transporte"
                },
                category = when (it % 6) {
                    0 -> "Servicios"
                    1 -> "Alimentos"
                    2 -> "Ingresos"
                    3 -> "Ocio"
                    4 -> "Ingresos"
                    else -> "Transporte"
                },
                amount = when (it % 6) {
                    2,4 -> 1500.0 + it * 10
                    else -> -(20.0 + it * 5)
                },
                isExpense = when (it % 6) { 2,4 -> false else -> true }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(text = "Movimientos", style = MaterialTheme.typography.titleLarge)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        )

        // Lista dinámica
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { tx ->
                TransactionItem(tx)
            }
        }
    }
}

@Composable
private fun TransactionItem(tx: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circular con inicial de categoría
            val initial = tx.category.firstOrNull()?.uppercaseChar() ?: '—'
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (tx.isExpense) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initial.toString(), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = tx.description, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${tx.date} • ${tx.category}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Monto
            Text(
                text = (if (tx.amount >= 0) "+" else "-") + "$${kotlin.math.abs(tx.amount)}",
                color = if (tx.isExpense) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
