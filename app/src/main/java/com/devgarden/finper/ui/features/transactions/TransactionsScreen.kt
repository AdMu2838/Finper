package com.devgarden.finper.ui.features.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.theme.FinperTheme

// Model usado en esta pantalla (nombre específico para evitar colisiones con otros paquetes)
data class TransactionModel(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val time: String,
    val date: String,
    val category: String,
    val amount: String,
    val isExpense: Boolean
)

// Sample data
private val sampleTransactions = listOf(
    TransactionModel("1", Icons.Default.Payments, "Salario", "18:27", "Abril 30", "Mensual", "S/.4,000.00", false),
    TransactionModel("2", Icons.Default.ShoppingBasket, "Verduras", "17:00", "Abril 24", "Despensa", "-S/.100.00", true),
    TransactionModel("3", Icons.Default.RealEstateAgent, "Renta", "8:30", "Abril 15", "Renta", "-S/.674.40", true),
    TransactionModel("4", Icons.Default.DirectionsCar, "Taxi", "7:30", "Abril 08", "Transporte", "-S/.4.13", true),
    TransactionModel("5", Icons.Default.Restaurant, "Comida", "19:30", "Mar 31", "Cena", "-S/.70.40", true),
    TransactionModel("6", Icons.Default.AccountBalance, "Ahorros", "09:00", "Mar 15", "Ahorro", "S/.120.00", false)
)

// Filtro reutilizable
enum class TransactionFilter { ALL, INCOME, EXPENSE }

@Composable
fun TransactionFilterToggle(
    modifier: Modifier = Modifier,
    selected: TransactionFilter,
    onSelected: (TransactionFilter) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == TransactionFilter.ALL, onClick = { onSelected(TransactionFilter.ALL) }, label = { Text("Todas") })
        FilterChip(selected = selected == TransactionFilter.INCOME, onClick = { onSelected(TransactionFilter.INCOME) }, label = { Text("Ingresos") })
        FilterChip(selected = selected == TransactionFilter.EXPENSE, onClick = { onSelected(TransactionFilter.EXPENSE) }, label = { Text("Gastos") })
    }
}

@Composable
fun TransactionItem(item: TransactionModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEBF4FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.icon, contentDescription = item.title, tint = Color(0xFF4285F4))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${item.time} • ${item.date}", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.amount, fontWeight = FontWeight.Bold, color = if (item.isExpense) Color(0xFF2D3748) else Color(0xFF00B974))
                Text(item.category, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TransactionList(
    modifier: Modifier = Modifier,
    transactions: List<TransactionModel>
) {
    LazyColumn(modifier = modifier) {
        items(transactions) { t ->
            TransactionItem(t, modifier = Modifier.padding(vertical = 6.dp))
        }
    }
}

@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    initialFilter: TransactionFilter = TransactionFilter.ALL
) {
    var selectedFilter by remember { mutableStateOf(initialFilter) }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF0F4F7))) {
        // header
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00B984))
            .padding(20.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Transacciones", color = Color.White, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier= Modifier
                        .padding(top = 40.dp),)
                Spacer(modifier = Modifier.height(12.dp))
                SummaryCard(
                    balanceLabel = "Balance Total",
                    balanceValue = "S/.7,783.00",
                    expenseLabel = "Gastos",
                    expenseValue = "-S/.1,187.40",
                    progress = 0.3f,
                    progressLabel = ""
                )
            }
        }

        // controls area
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            // small cards could be composed separately if needed
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Ingreso", fontWeight = FontWeight.SemiBold)
                        Text("S/.4,120.00", fontWeight = FontWeight.Bold)
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Gastos", fontWeight = FontWeight.SemiBold)
                        Text("S/.1,187.40", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // filtro reutilizable
            TransactionFilterToggle(selected = selectedFilter, onSelected = { selectedFilter = it })
        }

        // list
        val filtered = when (selectedFilter) {
            TransactionFilter.ALL -> sampleTransactions
            TransactionFilter.INCOME -> sampleTransactions.filter { !it.isExpense }
            TransactionFilter.EXPENSE -> sampleTransactions.filter { it.isExpense }
        }

        TransactionList(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp), transactions = filtered)
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionsPreview() {
    FinperTheme {
        TransactionsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun IncomesPreview() {
    FinperTheme {
        TransactionsScreen(initialFilter = TransactionFilter.INCOME)
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesPreview() {
    FinperTheme {
        TransactionsScreen(initialFilter = TransactionFilter.EXPENSE)
    }
}

