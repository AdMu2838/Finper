@file:Suppress("unused")
package com.devgarden.finper.ui.features.transactions

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.viewmodel.CategoriesViewModel
import com.devgarden.finper.ui.viewmodel.TransactionsViewModel
import com.devgarden.finper.ui.viewmodel.UserViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos específico para esta pantalla
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

// Valor por defecto compartido para categoría cuando no hay selección
private const val DEFAULT_CATEGORY = "Otros"

// Datos de ejemplo usados en la UI preview y en memoria
private val sampleTransactions = listOf(
    TransactionModel("1", Icons.Default.Payments, "Salario", "18:27", "Abril 30", "Mensual", "S/.4,000.00", false),
    TransactionModel("2", Icons.Default.ShoppingBasket, "Verduras", "17:00", "Abril 24", "Despensa", "-S/.100.00", true),
    TransactionModel("3", Icons.Default.RealEstateAgent, "Renta", "8:30", "Abril 15", "Renta", "-S/.674.40", true),
    TransactionModel("4", Icons.Default.DirectionsCar, "Taxi", "7:30", "Abril 08", "Transporte", "-S/.4.13", true),
    TransactionModel("5", Icons.Default.Restaurant, "Comida", "19:30", "Mar 31", "Cena", "-S/.70.40", true),
    TransactionModel("6", Icons.Default.AccountBalance, "Ahorros", "09:00", "Mar 15", "Ahorro", "S/.120.00", false)
)

// Filtro reutilizable para la lista
enum class TransactionFilter { ALL, INCOME, EXPENSE }

// Pequeño toggle de filtros
@Composable
fun TransactionFilterToggle(
    modifier: Modifier = Modifier,
    selected: TransactionFilter,
    onSelected: (TransactionFilter) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == TransactionFilter.ALL,
            onClick = { onSelected(TransactionFilter.ALL) },
            label = { Text("Todas") }
        )
        FilterChip(
            selected = selected == TransactionFilter.INCOME,
            onClick = { onSelected(TransactionFilter.INCOME) },
            label = { Text("Ingresos") }
        )
        FilterChip(
            selected = selected == TransactionFilter.EXPENSE,
            onClick = { onSelected(TransactionFilter.EXPENSE) },
            label = { Text("Gastos") }
        )
    }
}

// Item individual de la lista
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
                // Tint basado en tipo
                val tint = if (item.isExpense) Color(0xFFD32F2F) else Color(0xFF4285F4)
                Icon(imageVector = item.icon, contentDescription = item.title, tint = tint)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${item.time} • ${item.date}", color = Color.Gray, fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    item.amount,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isExpense) Color(0xFF2D3748) else Color(0xFF00B974)
                )
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

// Helpers de formato de fecha/hora
private val dateFormatter = SimpleDateFormat("d 'de' MMMM yyyy", Locale.forLanguageTag("es"))
private val timeFormatter = SimpleDateFormat("h:mm a", Locale.forLanguageTag("es"))

private fun formatDateForDisplay(date: Date?): String {
    if (date == null) return ""
    return dateFormatter.format(date)
}

private fun formatTimeForDisplay(date: Date?): String {
    if (date == null) return ""
    return timeFormatter.format(date)
}

// --- Secciones extraídas para mejorar la legibilidad ---

@Composable
private fun TransactionsHeader(balanceText: String) {
    // Encabezado con título y tarjeta de resumen (SummaryCard)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00B984))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Transacciones",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Obtener gastos del mes desde TransactionsViewModel
            val transactionsViewModel: TransactionsViewModel = viewModel()
            val monthlyExpenses = transactionsViewModel.monthlyExpenses
            val monthlyLoading = transactionsViewModel.monthlyLoading
            val expenseStr = if (monthlyLoading) "-S/.--" else "-${formatCurrency(monthlyExpenses)}"

            SummaryCard(
                balanceLabel = "Balance Total",
                balanceValue = balanceText,
                expenseLabel = "Gastos",
                expenseValue = expenseStr,
                //progress = 0.3f,
                progressLabel = ""
            )
        }
    }
}

@Composable
private fun ControlsSection(
    modifier: Modifier = Modifier,
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Ingreso", fontWeight = FontWeight.SemiBold)
                    Text("S/.4,120.00", fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                // Usar el ViewModel de transacciones para obtener gastos del mes
                val transactionsViewModel: TransactionsViewModel = viewModel()
                val monthlyExpenses = transactionsViewModel.monthlyExpenses
                val monthlyLoading = transactionsViewModel.monthlyLoading
                val expenseStr = if (monthlyLoading) "S/.--" else formatCurrency(monthlyExpenses)

                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Gastos", fontWeight = FontWeight.SemiBold)
                    Text("${expenseStr}", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TransactionFilterToggle(selected = selectedFilter, onSelected = onFilterSelected)

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onAddIncome, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Ingreso")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar ingreso")
            }

            Button(
                onClick = onAddExpense,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF6F6))
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Agregar Gasto", tint = Color(0xFFD32F2F))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar gasto", color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
private fun CategoryDropdown(
    categories: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = { }, // lectura solamente: cambios vienen de la selección del dropdown
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text("Categoría") },
            placeholder = { if (selected.isBlank()) Text("Seleccionar categoría") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = "Abrir")
                }
            }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth()) {
            if (categories.isEmpty()) {
                DropdownMenuItem(text = { Text(DEFAULT_CATEGORY) }, onClick = {
                    onSelected(DEFAULT_CATEGORY)
                    expanded = false
                })
            } else {
                categories.forEach { c ->
                    DropdownMenuItem(text = { Text(c) }, onClick = {
                        onSelected(c)
                        expanded = false
                    })
                }
            }
        }
    }
}

@Composable
private fun NewTransactionDialog(
    show: Boolean,
    isExpense: Boolean,
    titleValue: String,
    onTitleChange: (String) -> Unit,
    amountValue: String,
    onAmountChange: (String) -> Unit,
    categoryValue: String,
    onCategoryChange: (String) -> Unit,
    dateValue: Date?,
    onDateChange: (Date) -> Unit,
    categories: List<String>,
    categoriesLoading: Boolean,
    categoriesError: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!show) return

    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        // Inicializar picker con la fecha actual o la fecha seleccionada
        val cal = Calendar.getInstance().apply { time = dateValue ?: Date() }
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            // conservar hora/minuto actual
            val now = Calendar.getInstance()
            val chosen = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                set(Calendar.SECOND, now.get(Calendar.SECOND))
            }
            onDateChange(chosen.time)
            showPicker = false
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isExpense) "Agregar Gasto" else "Agregar Ingreso") },
        text = {
            Column {
                OutlinedTextField(value = titleValue, onValueChange = onTitleChange, label = { Text("Título") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amountValue, onValueChange = onAmountChange, label = { Text("Monto (ej: 1200.50)") })
                Spacer(modifier = Modifier.height(8.dp))

                // Manejo de estado de categorías: loading, error, o dropdown
                when {
                    categoriesLoading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cargando categorías...")
                        }
                    }
                    categoriesError != null -> {
                        Text("Error cargando categorías: ${categoriesError}", color = Color.Red)
                    }
                    else -> {
                        CategoryDropdown(categories = categories, selected = categoryValue, onSelected = onCategoryChange)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de fecha (mostrar hoy por defecto), abrir datepicker al tocar
                OutlinedTextField(
                    value = dateValue?.let { "${formatDateForDisplay(it)} ${formatTimeForDisplay(it)}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fecha") },
                    trailingIcon = {
                        IconButton(onClick = { showPicker = true }) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Pantalla principal: mantiene estados relacionados con la UI y coordina subcomponentes
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    initialFilter: TransactionFilter = TransactionFilter.ALL,
    selectedTabIndex: Int = 2,
    onBottomItemSelected: (Int) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(initialFilter) }

    // Usar ViewModel para transacciones
    val transactionsViewModel: TransactionsViewModel = viewModel()
    val tvTransactions by remember { derivedStateOf { transactionsViewModel.transactions } }

    // Mapear DTOs a modelos UI
    val transactionsMapped by remember(tvTransactions) {
        derivedStateOf {
            tvTransactions.map { dto ->
                val icon = if (dto.isExpense) Icons.Default.ShoppingBasket else Icons.Default.Payments
                val amountStr = if (dto.isExpense) "-S/.${formatNumber(dto.amount)}" else "S/.${formatNumber(dto.amount)}"
                val dateStr = formatDateForDisplay(dto.date)
                val timeStr = formatTimeForDisplay(dto.date)
                TransactionModel(
                    id = dto.id,
                    icon = icon,
                    title = dto.description.ifBlank { if (dto.isExpense) "Gasto" else "Ingreso" },
                    time = timeStr,
                    date = dateStr,
                    category = dto.category.ifBlank { DEFAULT_CATEGORY },
                    amount = amountStr,
                    isExpense = dto.isExpense
                )
            }
        }
    }

    // Estados para el diálogo de creación
    var showDialog by remember { mutableStateOf(false) }
    var isExpenseDialog by remember { mutableStateOf(true) }
    var newTitle by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newDate by remember { mutableStateOf<Date?>(Date()) } // por defecto hoy

    // Estados para categorías (obtenidas desde Firestore)
    val categoriesViewModel: CategoriesViewModel = viewModel()
    val categories by remember { derivedStateOf { categoriesViewModel.categories } }
    val categoriesLoading by remember { derivedStateOf { categoriesViewModel.loading } }
    val categoriesError by remember { derivedStateOf { categoriesViewModel.error } }

    // Obtener balance desde ViewModel
    val userViewModel: UserViewModel = viewModel()
    val balance = userViewModel.balance
    val balanceStr = formatCurrency(balance)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4F7))
                .padding(bottom = 88.dp) // leave space for bottom bar
        ) {
            TransactionsHeader(balanceText = balanceStr)

            ControlsSection(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                onAddIncome = {
                    isExpenseDialog = false
                    newTitle = ""
                    newAmount = ""
                    newCategory = ""
                    newDate = Date()
                    showDialog = true
                },
                onAddExpense = {
                    isExpenseDialog = true
                    newTitle = ""
                    newAmount = ""
                    newCategory = ""
                    newDate = Date()
                    showDialog = true
                }
            )

            val filtered = when (selectedFilter) {
                TransactionFilter.ALL -> transactionsMapped
                TransactionFilter.INCOME -> transactionsMapped.filter { !it.isExpense }
                TransactionFilter.EXPENSE -> transactionsMapped.filter { it.isExpense }
            }

            TransactionList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                transactions = if (filtered.isNotEmpty()) filtered else sampleTransactions
            )
        }

        // Barra inferior fija
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

        // Dialogo para crear transacción (extraído)
        NewTransactionDialog(
            show = showDialog,
            isExpense = isExpenseDialog,
            titleValue = newTitle,
            onTitleChange = { newTitle = it },
            amountValue = newAmount,
            onAmountChange = { newAmount = it },
            categoryValue = newCategory,
            onCategoryChange = { newCategory = it },
            dateValue = newDate,
            onDateChange = { newDate = it },
            categories = categories,
            categoriesLoading = categoriesLoading,
            categoriesError = categoriesError,
            onDismiss = { showDialog = false },
            onSave = {
                // Crear la transacción en Firestore mediante ViewModel
                val amountDouble = newAmount.toDoubleOrNull() ?: 0.0
                val category = if (newCategory.isBlank()) DEFAULT_CATEGORY else newCategory
                val dateObj = newDate ?: Date()
                val description = if (newTitle.isBlank()) (if (isExpenseDialog) "Gasto" else "Ingreso") else newTitle

                transactionsViewModel.addTransaction(amountDouble, category, dateObj, description, isExpenseDialog) { success, err ->
                    // si quieres manejar feedback, aquí puedes mostrar un snackbar
                    if (success) {
                        // Limpiar campos
                        newTitle = ""
                        newAmount = ""
                        newCategory = ""
                        newDate = Date()
                        showDialog = false
                    } else {
                        // mantener diálogo abierto y quizá mostrar error (no implementado visualmente)
                        // Para simplificar dejamos el diálogo abierto
                    }
                }
            }
        )
    }
}

// Helpers para formateo (se mantienen locales)
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

// Previews
@Preview(showBackground = true)
@Composable
fun TransactionsPreview() {
    FinperTheme { TransactionsScreen() }
}

@Preview(showBackground = true)
@Composable
fun IncomesPreview() {
    FinperTheme { TransactionsScreen(initialFilter = TransactionFilter.INCOME) }
}

@Preview(showBackground = true)
@Composable
fun ExpensesPreview() {
    FinperTheme { TransactionsScreen(initialFilter = TransactionFilter.EXPENSE) }
}
