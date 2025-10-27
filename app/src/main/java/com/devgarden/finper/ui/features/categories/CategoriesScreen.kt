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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.components.AdaptiveAdBanner
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.components.TopRoundedHeader
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.viewmodel.UserViewModel
import com.devgarden.finper.ui.viewmodel.TransactionsViewModel
import com.devgarden.finper.utils.FormatUtils
import com.devgarden.finper.utils.Constants

/**
 * Modelo de datos para categorías.
 */
data class Category(val icon: ImageVector, val label: String)

/**
 * Pantalla de categorías que muestra todas las categorías disponibles en un grid.
 *
 * @param onBack Callback para retroceder
 * @param onBottomItemSelected Callback para navegación de barra inferior
 */
@Composable
fun CategoriesScreen(onBack: () -> Unit = {}, onBottomItemSelected: (Int) -> Unit = {}) {
    var bottomSelected by remember { mutableStateOf(Constants.Navigation.BOTTOM_NAV_CATEGORIES) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Usar categorías desde Constants
    val categories = Constants.Defaults.PREDEFINED_CATEGORIES.map { categoryName ->
        Category(
            icon = Constants.Defaults.getCategoryIcon(categoryName),
            label = categoryName
        )
    }

    // Obtener balance desde UserViewModel
    val userViewModel: UserViewModel = viewModel()
    val balanceStr = FormatUtils.formatCurrency(userViewModel.balance)

    // Obtener gastos del mes actual desde TransactionsViewModel
    val transactionsViewModel: TransactionsViewModel = viewModel()
    val monthlyExpenses = transactionsViewModel.monthlyExpenses
    val monthlyLoading = transactionsViewModel.monthlyLoading
    val expenseStr = if (monthlyLoading) {
        "-S/.--"
    } else {
        FormatUtils.formatExpense(monthlyExpenses)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header (reusable)
            TopRoundedHeader(title = "Categorias", showBack = true, onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Summary card (reusable) - ahora usando balance desde Firestore
            SummaryCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                balanceLabel = "Balance Total",
                balanceValue = balanceStr,
                expenseLabel = "Gasto del mes",
                expenseValue = expenseStr,
                //progress = 0.3f,
                progressLabel = ""
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Banner de anuncios no invasivo
            AdaptiveAdBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Grid de categorías (3 columnas)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                val rows = categories.chunked(3)
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { cat ->
                            // pasar onClick para abrir la pantalla de transacciones filtradas por categoria
                            CategoryItem(cat) { selectedCategory = cat.label }
                        }
                        // Agregar espaciadores si la fila tiene menos de 3 items
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

    // Mostrar pantalla de transacciones filtradas por categoría si se selecciona una
    if (selectedCategory != null) {
        CategoryTransactionsScreen(
            category = selectedCategory!!,
            onBack = { selectedCategory = null },
            selectedIndex = bottomSelected,
            onBottomItemSelected = { idx ->
                bottomSelected = idx
                onBottomItemSelected(idx)
            }
        )
    }
}

/**
 * Item individual de categoría en el grid.
 *
 * @param category Categoría a mostrar
 * @param onClick Callback al hacer clic
 */
@Composable
fun CategoryItem(category: Category, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    category.icon,
                    contentDescription = category.label,
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = category.label, fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun CategoriesScreenPreview() {
    FinperTheme {
        CategoriesScreen()
    }
}
