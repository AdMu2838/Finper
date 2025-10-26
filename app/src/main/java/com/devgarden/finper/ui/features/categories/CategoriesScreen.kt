package com.devgarden.finper.ui.features.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.components.TopRoundedHeader
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.ui.viewmodel.UserViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

data class Category(val icon: ImageVector, val label: String)

@Composable
fun CategoriesScreen(onBack: () -> Unit = {}, onBottomItemSelected: (Int) -> Unit = {}) {
    var bottomSelected by remember { mutableStateOf(3) }

    val categories = listOf(
        Category(Icons.Default.Restaurant, "Comida"),
        Category(Icons.Default.DirectionsBus, "Transporte"),
        Category(Icons.Default.LocalHospital, "Medicina"),
        Category(Icons.Default.ShoppingBasket, "Comestibles"),
        Category(Icons.Default.Home, "Alquiler"),
        Category(Icons.Default.CardGiftcard, "Regalos"),
        Category(Icons.Default.Savings, "Ahorros"),
        Category(Icons.Default.Movie, "Entretenimiento"),
        Category(Icons.Default.Add, "Otros")
    )

    // Obtener balance desde ViewModel
    val userViewModel: UserViewModel = viewModel()
    val balanceStr = formatCurrency(userViewModel.balance)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF0F4F7))) {

        Column(modifier = Modifier.fillMaxSize()) {
            // Header (reusable)
            TopRoundedHeader(title = "Categorias", showBack = true, onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Summary card (reusable) - ahora usando balance desde Firestore
            SummaryCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                balanceLabel = "Total Balance",
                balanceValue = balanceStr,
                expenseLabel = "Total Expense",
                expenseValue = "-S/.1,187.40",
                progress = 0.3f,
                progressLabel = "30% of Your Expenses, Looks Good."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of categories (3 columns)
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {
                val rows = categories.chunked(3)
                rows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        row.forEach { cat ->
                            CategoryItem(cat)
                        }
                        // If row has less than 3 items, add spacers
                        if (row.size < 3) {
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
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

@Composable
fun CategoryItem(category: Category) {
    Column(modifier = Modifier

        .padding(6.dp)
        .clickable { /* navegar a categoria */ }
        , horizontalAlignment = Alignment.CenterHorizontally) {

        Card(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F6F2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(category.icon, contentDescription = category.label, tint = Color(0xFF007A4F), modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = category.label, fontSize = 12.sp)
    }
}

@Composable
fun CategoriesScreenPreview() {
    FinperTheme {
        CategoriesScreen()
    }
}

// Helper local para formatear moneda con separador de miles
private fun formatCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    val df = DecimalFormat("#,##0.00", symbols)
    return "S/.${df.format(amount)}"
}
