package com.devgarden.finper.ui.features.home

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

    // Calcular el rango según selectedPeriod usando DateUtils
    LaunchedEffect(selectedPeriod) {
        val range = DateUtils.getRangeForPeriodIndex(selectedPeriod)
        transactionsViewModel.listenTransactionsRange(range.start, range.end)
    }

    // Mapear DTOs a UI usando FormatUtils
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
                    items(mappedTransactions) { transaction ->
                        TransactionListItem(
                            icon = transaction.icon,
                            title = transaction.title,
                            subtitle = "${transaction.time} - ${transaction.date}",
                            category = transaction.category,
                            amount = transaction.amount,
                            isExpense = transaction.isExpense
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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

/**
 * Card de metas de ahorro (actualmente no usado).
 */
@Suppress("unused")
@Composable
private fun SavingsGoalsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD9F3EC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Car Goal",
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF2D3748)
                )
                Text(
                    text = "Guardando Para \nTus Metas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(
                modifier = Modifier
                    .height(120.dp)
                    .width(1.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            Column {
                GoalItem(
                    icon = Icons.Default.AccountBalance,
                    text = "Renueva el siguiente mes",
                    amount = "S/.4,000.00"
                )
                Spacer(modifier = Modifier.height(8.dp))
                GoalItem(
                    icon = Icons.Default.Restaurant,
                    text = "Comida semana pasada",
                    amount = "-S/.100.00",
                    isExpense = true
                )
            }
        }
    }
}

/**
 * Item individual de meta de ahorro.
 */
@Suppress("unused")
@Composable
private fun GoalItem(
    icon: ImageVector,
    text: String,
    amount: String,
    isExpense: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isExpense) Color(0xFFFFF6F6) else Color(0xFFEFFCF2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp),
                tint = if (isExpense) Color(0xFFD32F2F) else Color(0xFF007A4F)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { if (isExpense) 0.18f else 0.6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = if (isExpense) Color(0xFFD32F2F) else Color(0xFF00B974),
                trackColor = Color(0xFFF0F6F3)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isExpense) "Gasto • Última semana" else "Ahorro • Progreso mensual",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            modifier = Modifier
                .widthIn(min = 92.dp)
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = amount,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) Color(0xFFD32F2F) else Color(0xFF2D3748),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExpense) "18%" else "60%",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
