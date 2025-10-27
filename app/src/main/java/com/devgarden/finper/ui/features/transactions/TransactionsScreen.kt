@file:Suppress("unused")
package com.devgarden.finper.ui.features.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.ui.viewmodel.CategoriesViewModel
import com.devgarden.finper.ui.viewmodel.TransactionsViewModel
import com.devgarden.finper.ui.viewmodel.UserViewModel
import com.devgarden.finper.utils.Constants
import com.devgarden.finper.utils.FormatUtils
import java.util.*

/**
 * Modelo de datos específico para transacciones en la UI.
 */
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

/**
 * Filtro para mostrar transacciones.
 */
enum class TransactionFilter { ALL, INCOME, EXPENSE }

/**
 * Toggle de filtros de transacciones.
 */
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
            label = { Text("Todas") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        FilterChip(
            selected = selected == TransactionFilter.INCOME,
            onClick = { onSelected(TransactionFilter.INCOME) },
            label = { Text("Ingresos") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        FilterChip(
            selected = selected == TransactionFilter.EXPENSE,
            onClick = { onSelected(TransactionFilter.EXPENSE) },
            label = { Text("Gastos") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Item individual de la lista de transacciones.
 */
@Composable
fun TransactionItem(item: TransactionModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val tint = if (item.isExpense) Color(0xFFD32F2F) else Color(0xFF4285F4)
                Icon(imageVector = item.icon, contentDescription = item.title, tint = tint)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${item.time} • ${item.date}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    item.amount,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isExpense) Color.Red else PrimaryGreen
                )
                Text(item.category, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Lista de transacciones con mensaje de estado vacío.
 */
@Composable
fun TransactionList(
    modifier: Modifier = Modifier,
    transactions: List<TransactionModel>,
    viewModel: TransactionsViewModel = viewModel()
) {
    val hasMore by remember { derivedStateOf { viewModel.hasMoreTransactions } }
    val loadingMore by remember { derivedStateOf { viewModel.loadingMore } }

    if (transactions.isEmpty()) {
        EmptyTransactionsCard()
    } else {
        LazyColumn(modifier = modifier) {
            itemsIndexed(transactions) { index, t ->
                TransactionItem(t, modifier = Modifier.padding(vertical = 6.dp))

                // Detectar cuando se llega al final para cargar más
                if (index == transactions.size - 1 && hasMore && !loadingMore) {
                    LaunchedEffect(Unit) {
                        viewModel.loadMoreTransactions()
                    }
                }
            }

            // Mostrar indicador de carga al final
            if (loadingMore) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryGreen
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mensaje mostrado cuando no hay transacciones.
 */
@Composable
private fun EmptyTransactionsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Sin transacciones",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay transacciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Agrega tu primera transacción usando los botones de arriba",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Encabezado de la pantalla de transacciones.
 */
@Composable
private fun TransactionsHeader(balanceText: String) {
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

            val transactionsViewModel: TransactionsViewModel = viewModel()
            val monthlyExpenses = transactionsViewModel.monthlyExpenses
            val monthlyLoading = transactionsViewModel.monthlyLoading
            val expenseStr = if (monthlyLoading) {
                "-S/.--"
            } else {
                FormatUtils.formatExpense(monthlyExpenses)
            }

            SummaryCard(
                balanceLabel = "Balance Total",
                balanceValue = balanceText,
                expenseLabel = "Gastos",
                expenseValue = expenseStr,
                progressLabel = ""
            )
        }
    }
}

/**
 * Sección de controles y filtros.
 */
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                val transactionsViewModel: TransactionsViewModel = viewModel()
                val monthlyIncomes = transactionsViewModel.monthlyIncomes
                val monthlyIncomesLoading = transactionsViewModel.monthlyIncomesLoading
                val incomeStr = if (monthlyIncomesLoading) "S/.--" else FormatUtils.formatCurrency(monthlyIncomes)

                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Ingreso", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(incomeStr, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                val transactionsViewModel: TransactionsViewModel = viewModel()
                val monthlyExpenses = transactionsViewModel.monthlyExpenses
                val monthlyLoading = transactionsViewModel.monthlyLoading
                val expenseStr = if (monthlyLoading) "S/.--" else FormatUtils.formatCurrency(monthlyExpenses)

                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Gastos", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(expenseStr, fontWeight = FontWeight.Bold, color = Color.Red)
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

/**
 * Dropdown para seleccionar categoría.
 */
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
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text("Categoría") },
            placeholder = { if (selected.isBlank()) Text("Seleccionar categoría") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Abrir"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (categories.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(Constants.Defaults.DEFAULT_CATEGORY) },
                    onClick = {
                        onSelected(Constants.Defaults.DEFAULT_CATEGORY)
                        expanded = false
                    }
                )
            } else {
                categories.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = {
                            onSelected(c)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Pantalla principal de transacciones.
 */
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    initialFilter: TransactionFilter = TransactionFilter.ALL,
    selectedTabIndex: Int = Constants.Navigation.BOTTOM_NAV_TRANSACTIONS,
    onBottomItemSelected: (Int) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(initialFilter) }

    val transactionsViewModel: TransactionsViewModel = viewModel()
    val tvTransactions by remember { derivedStateOf { transactionsViewModel.transactions } }

    // Mapear DTOs a modelos UI usando FormatUtils
    val transactionsMapped by remember(tvTransactions) {
        derivedStateOf {
            tvTransactions.map { dto ->
                val icon = if (dto.isExpense) Icons.Default.ShoppingBasket else Icons.Default.Payments
                val amountStr = if (dto.isExpense) {
                    FormatUtils.formatExpense(dto.amount)
                } else {
                    FormatUtils.formatIncome(dto.amount)
                }
                val dateStr = FormatUtils.formatDateForDisplay(dto.date)
                val timeStr = FormatUtils.formatTimeForDisplay(dto.date)
                TransactionModel(
                    id = dto.id,
                    icon = icon,
                    title = dto.description.ifBlank { if (dto.isExpense) "Gasto" else "Ingreso" },
                    time = timeStr,
                    date = dateStr,
                    category = dto.category.ifBlank { Constants.Defaults.DEFAULT_CATEGORY },
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
    var newDate by remember { mutableStateOf<Date?>(Date()) }

    // Obtener balance
    val userViewModel: UserViewModel = viewModel()
    val balance = userViewModel.balance
    val balanceStr = FormatUtils.formatCurrency(balance)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 88.dp)
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
                transactions = filtered
            )
        }

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
            onDismiss = { showDialog = false },
            onSave = {
                val amountDouble = newAmount.toDoubleOrNull() ?: 0.0
                val category = if (newCategory.isBlank()) Constants.Defaults.DEFAULT_CATEGORY else newCategory
                val dateObj = newDate ?: Date()
                val description = if (newTitle.isBlank()) {
                    if (isExpenseDialog) "Gasto" else "Ingreso"
                } else newTitle

                transactionsViewModel.addTransaction(
                    amountDouble,
                    category,
                    dateObj,
                    description,
                    isExpenseDialog
                ) { success, _ ->
                    if (success) {
                        newTitle = ""
                        newAmount = ""
                        newCategory = ""
                        newDate = Date()
                        showDialog = false
                    }
                }
            }
        )
    }
}
