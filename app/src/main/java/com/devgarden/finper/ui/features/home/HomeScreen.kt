package com.devgarden.finper.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.components.TransactionListItem
import com.devgarden.finper.ui.viewmodel.TransactionsViewModel
import com.devgarden.finper.ui.viewmodel.UserViewModel
import com.devgarden.finper.utils.DateUtils
import com.devgarden.finper.utils.FormatUtils
import com.devgarden.finper.utils.Constants

/**
 * Modelo de datos para transacciones en la UI del Home.
 */
data class Transaction(
    val icon: ImageVector,
    val title: String,
    val time: String,
    val date: String,
    val category: String,
    val amount: String,
    val isExpense: Boolean = true
)

/**
 * Pantalla principal del Home que muestra el balance, gastos y transacciones recientes.
 *
 * @param onBottomItemSelected Callback para navegación entre pantallas
 */
@Composable
fun HomeScreen(onBottomItemSelected: (Int) -> Unit = {}) {
    var bottomSelected by remember { mutableIntStateOf(Constants.Navigation.BOTTOM_NAV_HOME) }
    var selectedPeriod by remember { mutableIntStateOf(1) } // 0=Diario, 1=Semanal, 2=Mensual

    val transactionsViewModel: TransactionsViewModel = viewModel()
    val dtoTransactions by remember { derivedStateOf { transactionsViewModel.transactions } }
    val hasMore by remember { derivedStateOf { transactionsViewModel.hasMoreTransactions } }
    val loadingMore by remember { derivedStateOf { transactionsViewModel.loadingMore } }

    LaunchedEffect(selectedPeriod) {
        val range = DateUtils.getRangeForPeriodIndex(selectedPeriod)
        transactionsViewModel.listenTransactionsRange(range.start, range.end)
    }

    val mappedTransactions by remember(dtoTransactions) {
        derivedStateOf {
            dtoTransactions.map { dto ->
                val icon = if (dto.isExpense) Icons.Default.ShoppingBasket else Icons.Default.Payments
                val amountStr = if (dto.isExpense) {
                    FormatUtils.formatExpense(dto.amount)
                } else {
                    FormatUtils.formatIncome(dto.amount)
                }
                val dateStr = FormatUtils.formatDateForDisplay(dto.date)
                val timeStr = FormatUtils.formatTimeForDisplay(dto.date)
                Transaction(
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F7))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection()

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    TimePeriodToggle(
                        selectedIndex = selectedPeriod,
                        onSelected = { selectedPeriod = it },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (mappedTransactions.isEmpty()) {
                    item {
                        EmptyTransactionsMessage()
                    }
                } else {
                    itemsIndexed(mappedTransactions) { index, transaction ->
                        TransactionListItem(
                            icon = transaction.icon,
                            title = transaction.title,
                            subtitle = "${transaction.time} - ${transaction.date}",
                            category = transaction.category,
                            amount = transaction.amount,
                            isExpense = transaction.isExpense
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Detectar cuando se llega al final para cargar más
                        if (index == mappedTransactions.size - 1 && hasMore && !loadingMore) {
                            LaunchedEffect(Unit) {
                                transactionsViewModel.loadMoreTransactions()
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

/**
 * Mensaje mostrado cuando no hay transacciones en el periodo seleccionado.
 */
@Composable
private fun EmptyTransactionsMessage() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
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
                text = "Aún no tienes transacciones en este periodo",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Sección de encabezado con balance y resumen de gastos del mes.
 */
@Composable
private fun HeaderSection() {
    val userViewModel: UserViewModel = viewModel()
    val balance = userViewModel.balance
    val balanceStr = FormatUtils.formatCurrency(balance)

    val transactionsViewModel: TransactionsViewModel = viewModel()
    val monthlyExpenses = transactionsViewModel.monthlyExpenses
    val monthlyLoading = transactionsViewModel.monthlyLoading
    val expenseStr = if (monthlyLoading) {
        "-S/.--"
    } else {
        FormatUtils.formatExpense(monthlyExpenses)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(PrimaryGreen)
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Hola, Bienvenido de vuelta",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Buenos días",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = { /*TODO: Implementar notificaciones*/ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        SummaryCard(
            modifier = Modifier.padding(bottom = 16.dp),
            balanceLabel = "Presupuesto Total",
            balanceValue = balanceStr,
            expenseLabel = "Gasto Del Mes",
            expenseValue = expenseStr,
            progressLabel = ""
        )
    }
}

/**
 * Toggle de periodo con estilo personalizado para Home.
 */
@Composable
private fun TimePeriodToggle(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 1,
    onSelected: (Int) -> Unit
) {
    val periods = listOf("Diario", "Semanal", "Mensual")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            periods.forEachIndexed { index, period ->
                val isSelected = selectedIndex == index
                Button(
                    onClick = { onSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF00D1A1) else Color.White,
                        contentColor = if (isSelected) Color.White else Color.Gray
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(text = period)
                }
            }
        }
    }
}
