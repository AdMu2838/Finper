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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.viewmodel.UserViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


// --- Data class to represent a transaction ---
data class Transaction(
    val icon: ImageVector,
    val title: String,
    val time: String,
    val date: String,
    val category: String,
    val amount: String,
    val isExpense: Boolean = true
)

// --- Sample data for the list ---
val sampleTransactions = listOf(
    Transaction(Icons.Default.Payments, "Salario", "18:27", "Abril 30", "Mes", "S/.4,000,00", isExpense = false),
    Transaction(Icons.Default.ShoppingBasket, "Verduras", "17:00", "Abril 24", "Despensa", "-S/.100,00"),
    Transaction(Icons.Default.RealEstateAgent, "Renta", "8:30", "Abril 15", "Renta", "-S/.674,40")
)

@Composable
fun HomeScreen(onBottomItemSelected: (Int) -> Unit = {}) {
    var bottomSelected by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F7)) // A light gray background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Top Green Header Section ---
            HeaderSection()

            // --- Transaction List Section ---
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Padding for the bottom nav bar
            ) {
                item {
                    // --- Savings Goals Card ---
                    SavingsGoalsCard(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp))
                }
                item {
                    // --- Time Period Toggle ---
                    TimePeriodToggle(modifier = Modifier.padding(bottom = 16.dp))
                }
                // --- Transaction Items ---
                items(sampleTransactions) { transaction ->
                    TransactionItem(transaction)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // --- Bottom Navigation Bar (reutilizable) ---
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

@Composable
fun HeaderSection() {
    // Obtener balance desde UserViewModel
    val userViewModel: UserViewModel = viewModel()
    val balance = userViewModel.balance
    val balanceStr = formatCurrency(balance)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(PrimaryGreen)
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 32.dp),
    ) {
        // --- Greeting and Notification Bell ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Hola, Bienvenido de vuelta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Buenos días", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Reusable summary card reemplazando InfoCard + progress section
        SummaryCard(
            modifier = Modifier.padding(bottom = 16.dp),
            balanceLabel = "Presupuesto Total",
            balanceValue = balanceStr,
            expenseLabel = "Gasto Del Mes",
            expenseValue = "-S/.1,187.40",
            progress = 0.3f,
            progressLabel = "30% De Tus Gastos, Se Ve Bien."
        )
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

@Composable
fun InfoCard(title: String, amount: String, icon: ImageVector, isExpense: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Text(
                text = amount,
                color = if (isExpense) Color(0xFF2D3748) else Color.White, // Darker text for expense
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SavingsGoalsCard(modifier: Modifier = Modifier) {
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
            // Columna de la izquierda (el auto)
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
            //divider vertical
            Spacer(
                modifier = Modifier
                    .height(120.dp)
                    .width(1.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            // Columna de la derecha (los items)
            Column(
                modifier = Modifier
            ) {
                GoalItem(icon = Icons.Default.AccountBalance, text = "Renueva el siguiente mes", amount = "S/.4,000.00")
                Spacer(modifier = Modifier.height(8.dp))
                GoalItem(icon = Icons.Default.Restaurant, text = "Comida semana pasada", amount = "-S/.100.00", isExpense = true)
            }
        }
    }
}

@Composable
fun GoalItem(icon: ImageVector, text: String, amount: String, isExpense: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // icono dentro de un círculo
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

        // columna principal con título, subtítulo y progress
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

        // columna de monto con ancho mínimo para evitar wrap vertical
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

@Composable
fun TimePeriodToggle(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(2) }
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
                Button(
                    onClick = { selectedIndex = index },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedIndex == index) Color(0xFF00D1A1) else Color.White,
                        contentColor = if (selectedIndex == index) Color.White else Color.Gray
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(text = period)
                }
            }
        }
    }
}


@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEBF4FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = transaction.title,
                    tint = Color(0xFF4285F4)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${transaction.time} - ${transaction.date}", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (transaction.isExpense) Color.Black else Color(0xFF00D1A1)
                )
                Text(text = transaction.category, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FinperTheme {
        HomeScreen()
    }
}