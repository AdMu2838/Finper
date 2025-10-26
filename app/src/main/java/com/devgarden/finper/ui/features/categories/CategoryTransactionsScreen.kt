package com.devgarden.finper.ui.features.categories

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.viewmodel.TransactionsViewModel
import com.devgarden.finper.ui.viewmodel.UserViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

// Nodo plano para la lista (encabezado por mes o transacción)
private sealed class ListNode {
    data class Header(val month: String) : ListNode()
    data class Tx(val tx: TransactionsViewModel.TransactionDto) : ListNode()
}

@Composable
fun CategoryTransactionsScreen(
    category: String,
    onBack: () -> Unit,
    selectedIndex: Int = 3,
    onBottomItemSelected: (Int) -> Unit = {},
    vm: TransactionsViewModel = viewModel()
) {
    val context = LocalContext.current

    // cargar transacciones por categoría
    LaunchedEffect(category) {
        vm.loadTransactionsByCategory(category)
    }

    // obtener balance del usuario para mostrar en la tarjeta
    val userVM: UserViewModel = viewModel()

    val loading by remember { derivedStateOf { vm.categoryLoading } }
    val transactions by remember { derivedStateOf { vm.categoryTransactions } }
    val indexUrl by remember { derivedStateOf { vm.categoryIndexUrl } }

    // Agrupar transacciones por mes-año (descendente)
    val grouped = remember(transactions) {
        transactions
            .filter { it.date != null }
            .sortedByDescending { it.date }
            .groupBy { monthLabel(it.date!!) }
    }

    // Construir lista plana (Header + Tx) para renderizar con LazyColumn.items
    val nodes = remember(grouped) {
        grouped.entries.flatMap { (month, list) ->
            listOf(ListNode.Header(month)) + list.map { ListNode.Tx(it) }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF0F4F7)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header verde con título centrado y botones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00B984))
                    .padding(top = 24.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        vm.clearCategoryQuery()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = category,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { /* TODO: notifications */ }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SummaryCard
                SummaryCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    balanceLabel = "Total Balance",
                    balanceValue = formatCurrency(userVM.balance),
                    expenseLabel = "Total Expense",
                    expenseValue = if (vm.monthlyLoading) "-S/.--" else "-${formatCurrency(vm.monthlyExpenses)}",
                    progressLabel = ""
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // White rounded content area
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // optional index link
                    if (indexUrl != null) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = {
                                try {
                                    // usar let para evitar problemas con smart-cast en properties delegadas
                                    indexUrl?.let { url ->
                                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        context.startActivity(intent)
                                    }
                                } catch (_: Exception) {
                                }
                            }) {
                                Text("Crear índice en Firebase Console", color = Color.Red)
                            }
                        }
                    }

                    // List grouped by month rendered via nodes
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (transactions.isEmpty() && !loading) {
                            item {
                                Text("No hay transacciones", modifier = Modifier.padding(16.dp))
                            }
                        }

                        // show loading indicator at top
                        if (loading) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        items(nodes) { node ->
                            when (node) {
                                is ListNode.Header -> {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = node.month, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { /* TODO: abrir calendario o filtro */ }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Filled.Event, contentDescription = "Calendario", tint = Color(0xFF007A4F))
                                        }
                                    }
                                }
                                is ListNode.Tx -> {
                                    TransactionCard(node.tx)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom navigation bar (usar BottomBar existente) — recibir navegación vía callback
            BottomBar(
                modifier = Modifier.fillMaxWidth(),
                items = listOf(Icons.Filled.Home, Icons.Filled.BarChart, Icons.Filled.SwapHoriz, Icons.Filled.Layers, Icons.Filled.Person),
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    onBottomItemSelected(index)
                }
            )
        }
    }
}

@Composable
private fun TransactionCard(tx: TransactionsViewModel.TransactionDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FFF9))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFCCE8FF)), contentAlignment = Alignment.Center) {
                // icono de comida
                Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Color(0xFF007A4F))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.description.ifBlank { if (tx.isExpense) "Gasto" else "Ingreso" }, fontWeight = FontWeight.SemiBold)
                Text(text = tx.date?.let { formatDateShort(it) } ?: "", fontSize = 12.sp, color = Color.Gray)
            }

            Text(text = (if (tx.isExpense) "-" else "") + formatCurrency(tx.amount), fontWeight = FontWeight.Bold, color = if (tx.isExpense) Color(0xFF0B6EFF) else Color(0xFF00B974))
        }
    }
}

private fun monthLabel(d: Date): String {
    val fmt = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es"))
    return fmt.format(d)
}

private fun formatDateShort(d: Date): String {
    val fmt = SimpleDateFormat("H:mm • d MMM", Locale.forLanguageTag("es"))
    return fmt.format(d)
}

private fun formatCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    val df = DecimalFormat("#,##0.00", symbols)
    return "$${df.format(amount)}"
}
