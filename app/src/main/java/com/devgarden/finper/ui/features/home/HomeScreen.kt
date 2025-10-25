package com.devgarden.finper.ui.features.home


import androidx.compose.foundation.Image
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
import com.devgarden.finper.ui.theme.FinperTheme


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
fun HomeScreen() {
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

        // --- Bottom Navigation Bar ---
        BottomNavBar(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Color(0xFF00D1A1))
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 32.dp),
    ) {
        // --- Greeting and Notification Bell ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Hola", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "buenos días", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- Budget and Expense Info ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InfoCard("Presupuesto Total", "S/.7,783.00", Icons.Default.AccountBalanceWallet)
            InfoCard("Gasto Del Mes", "-S/.1,187.40", Icons.Default.ReceiptLong, isExpense = true)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- Progress Bar ---
        Text(
            text = "30% De Tus Gastos, Se Ve Bien.",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { 0.3f },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
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
                    text = "Guardando Para Tus Metas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )
            }
            Divider(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                GoalItem(icon = Icons.Default.AccountBalance, text = "Renueva el siguiente mes", amount = "S/.4,000.00")
                Spacer(modifier = Modifier.height(8.dp))
                GoalItem(icon = Icons.Default.Restaurant, text = "Comida semana pasada", amount = "-S/.100.00", isExpense = true)
            }
        }
    }
}

@Composable
fun GoalItem(icon: ImageVector, text: String, amount: String, isExpense: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = amount,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if(isExpense) Color.Red.copy(alpha = 0.7f) else Color(0xFF2D3748)
        )
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

@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp), // Increased height for the curve effect
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(icon = Icons.Default.Home, isSelected = true)
            BottomNavItem(icon = Icons.Default.BarChart)
            BottomNavItem(icon = Icons.Default.SwapHoriz)
            BottomNavItem(icon = Icons.Default.AccountBalanceWallet)
            BottomNavItem(icon = Icons.Default.Person)
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, isSelected: Boolean = false) {
    val backgroundColor = if (isSelected) Color(0xFFD9F3EC) else Color.Transparent
    val iconColor = if (isSelected) Color(0xFF00D1A1) else Color.Gray

    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor)
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FinperTheme {
        HomeScreen()
    }
}