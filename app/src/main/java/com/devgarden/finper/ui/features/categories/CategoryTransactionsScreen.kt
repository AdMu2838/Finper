package com.devgarden.finper.ui.features.categories

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.viewmodel.TransactionsViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun CategoryTransactionsScreen(
    category: String,
    onBack: () -> Unit,
    vm: TransactionsViewModel = viewModel()
) {
    val context = LocalContext.current

    // Llamar al ViewModel para cargar (server-side ordering). Se hace cuando cambia la categoría.
    LaunchedEffect(category) {
        vm.loadTransactionsByCategory(category)
    }

    // Estados expuestos por el ViewModel
    val loading by remember { derivedStateOf { vm.categoryLoading } }
    val error by remember { derivedStateOf { vm.categoryError } }
    val transactions by remember { derivedStateOf { vm.categoryTransactions } }
    val indexUrl by remember { derivedStateOf { vm.categoryIndexUrl } }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(text = "Transacciones: $category") },
                navigationIcon = {
                    IconButton(onClick = {
                        vm.clearCategoryQuery()
                        onBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4F7))
                .padding(innerPadding)) {

                Column(modifier = Modifier.fillMaxSize()) {
                    // Si Firestore devolvió una URL para crear el índice, mostrar botón para abrirla
                    if (indexUrl != null) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(indexUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            }) {
                                Text("Crear índice en Firebase Console", color = Color.Red)
                            }
                        }
                    }

                    when {
                        loading -> {
                            Row(modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cargando transacciones...")
                            }
                        }
                        error != null -> {
                            Text(text = "Error: ${error}", color = Color.Red, modifier = Modifier.padding(16.dp))
                        }
                        transactions.isEmpty() -> {
                            Text(text = "No se encontraron transacciones en la categoría.", modifier = Modifier.padding(16.dp))
                        }
                        else -> {
                            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                items(transactions) { t ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(text = t.description.ifBlank { if (t.isExpense) "Gasto" else "Ingreso" }, fontWeight = FontWeight.SemiBold)
                                            Text(text = t.date?.let { formatDate(it) } ?: "", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text(text = (if (t.isExpense) "-" else "") + formatCurrency(t.amount), fontWeight = FontWeight.Bold)
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }

            }
        }
    )
}

// Helper local y data class (puede reutilizarse o moverse si se prefiere)
private fun formatDate(d: Date): String {
    val fmt = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.forLanguageTag("es"))
    return fmt.format(d)
}

private fun formatCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    val df = DecimalFormat("#,##0.00", symbols)
    return "S/.${df.format(amount)}"
}

private data class TransactionRow(
    val id: String,
    val amount: Double,
    val description: String,
    val date: Date?,
    val isExpense: Boolean
)
