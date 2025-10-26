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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.viewmodel.UserViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

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
    initialFilter: TransactionFilter = TransactionFilter.ALL,
    selectedTabIndex: Int = 2,
    onBottomItemSelected: (Int) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(initialFilter) }

    // Lista mutable en memoria para que los nuevos items se muestren inmediatamente
    val transactions = remember { mutableStateListOf<TransactionModel>().apply { addAll(sampleTransactions) } }

    // Estados para el diálogo de creación
    var showDialog by remember { mutableStateOf(false) }
    var isExpenseDialog by remember { mutableStateOf(true) }
    var newTitle by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newDate by remember { mutableStateOf("") }

    // Obtener balance desde ViewModel
    val userViewModel: UserViewModel = viewModel()
    val balance = userViewModel.balance
    val balanceStr = formatCurrency(balance)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F7))
            .padding(bottom = 88.dp) // leave space for bottom bar
        ) {
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
                        balanceValue = balanceStr,
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

                Spacer(modifier = Modifier.height(12.dp))

                // Botones para agregar Ingreso / Gasto
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        isExpenseDialog = false
                        newTitle = ""
                        newAmount = ""
                        newCategory = ""
                        newDate = ""
                        showDialog = true
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Ingreso")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar ingreso")
                    }

                    Button(onClick = {
                        isExpenseDialog = true
                        newTitle = ""
                        newAmount = ""
                        newCategory = ""
                        newDate = ""
                        showDialog = true
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF6F6))) {
                        Icon(Icons.Default.Remove, contentDescription = "Agregar Gasto", tint = Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar gasto", color = Color(0xFFD32F2F))
                    }
                }
            }

            // list
            val filtered = when (selectedFilter) {
                TransactionFilter.ALL -> transactions
                TransactionFilter.INCOME -> transactions.filter { !it.isExpense }
                TransactionFilter.EXPENSE -> transactions.filter { it.isExpense }
            }

            TransactionList(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp), transactions = filtered)
        }

        // bottom bar fija
        BottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            items = listOf(
                Icons.Default.Home,
                Icons.Default.BarChart,
                Icons.Default.SwapHoriz,
                Icons.Default.AccountBalanceWallet,
                Icons.Default.Person
            ),
            selectedIndex = selectedTabIndex,
            onItemSelected = onBottomItemSelected
        )

        // Dialogo para crear transacción
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (isExpenseDialog) "Agregar Gasto" else "Agregar Ingreso") },
                text = {
                    Column {
                        OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Título") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newAmount, onValueChange = { newAmount = it }, label = { Text("Monto (ej: 1200.50)") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newCategory, onValueChange = { newCategory = it }, label = { Text("Categoría") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newDate, onValueChange = { newDate = it }, label = { Text("Fecha (ej: Abril 30)") })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Crear nuevo item y añadir a la lista
                        val amountDouble = newAmount.toDoubleOrNull() ?: 0.0
                        val formatted = if (isExpenseDialog) "-S/.${formatNumber(amountDouble)}" else "S/.${formatNumber(amountDouble)}"
                        val icon = if (isExpenseDialog) Icons.Default.ShoppingBasket else Icons.Default.Payments
                        val id = System.currentTimeMillis().toString()
                        val title = if (newTitle.isBlank()) (if (isExpenseDialog) "Gasto" else "Ingreso") else newTitle
                        val category = if (newCategory.isBlank()) "Varios" else newCategory
                        val date = if (newDate.isBlank()) "Hoy" else newDate

                        transactions.add(0, TransactionModel(id = id, icon = icon, title = title, time = "--:--", date = date, category = category, amount = formatted, isExpense = isExpenseDialog))

                        showDialog = false
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Helper local para formatear moneda con separador de miles coma y decimal punto
private fun formatCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    val df = DecimalFormat("#,##0.00", symbols)
    return "S/.${df.format(amount)}"
}

private fun formatNumber(amount: Double): String {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    val df = DecimalFormat("#,##0.00", symbols)
    return df.format(amount)
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
