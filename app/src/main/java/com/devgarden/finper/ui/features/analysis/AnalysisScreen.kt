package com.devgarden.finper.ui.features.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devgarden.finper.ui.components.BottomBar
import com.devgarden.finper.ui.components.SummaryCard
import com.devgarden.finper.ui.viewmodel.TransactionsViewModel
import com.devgarden.finper.ui.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.max

/**
 * Pantalla para el segundo botón del BottomBar (índice 1) — "Análisis".
 * Implementa debounce en el selector de periodos, usa monthlyExpenses del ViewModel cuando corresponde
 * y muestra un gráfico de barras simple en Compose.
 */

@Composable
fun AnalysisScreen(
    onBack: () -> Unit = {},
    selectedIndex: Int = 1,
    onBottomItemSelected: (Int) -> Unit = {}
) {
    // ViewModels
    val userViewModel: UserViewModel = viewModel()
    val transactionsViewModel: TransactionsViewModel = viewModel()

    // Period selector: 0=Diario,1=Semanal,2=Mensual,3=Anual
    var selectedPeriod by remember { mutableStateOf(2) } // por defecto Mensual

    // DebouncedSelected usado para aplicar la lógica (evita cambiar listeners muy rápido)
    var debouncedSelected by remember { mutableStateOf(selectedPeriod) }

    // aplicar debounce cuando selectedPeriod cambie
    LaunchedEffect(selectedPeriod) {
        val current = selectedPeriod
        delay(300) // debounce 300ms
        debouncedSelected = current
    }

    // Cuando cambie debouncedSelected, actualizar la consulta de rango solo para períodos no mensuales
    LaunchedEffect(debouncedSelected) {
        if (debouncedSelected == 2) {
            // Mensual: evitamos crear un listener de rango adicional; TransactionsViewModel ya mantiene monthlyExpenses
        } else {
            val now = Date()
            val range = when (debouncedSelected) {
                0 -> periodDayRange(now)
                1 -> periodMonthRange(now) // <-- cargar TODO el mes para agrupar por semanas
                3 -> periodYearRange(now)
                else -> periodMonthRange(now)
            }
            transactionsViewModel.listenTransactionsRange(range.first, range.second)
        }
    }

    // Observables
    val transactions = transactionsViewModel.transactions
    val monthlyExpenses = transactionsViewModel.monthlyExpenses
    val monthlyLoading = transactionsViewModel.monthlyLoading

    // Calcular sums según rango para mostrar en el chart y en resumen — filtramos localmente según debouncedSelected
    val groupedData by remember(transactions, debouncedSelected) {
        mutableStateOf(groupTransactionsForPeriod(transactions, debouncedSelected))
    }

    // Sumas para el rango actual (desde groupedData)
    val expenseSum = groupedData.sumOf { it.second.takeIf { v -> v < 0 }?.let { -it } ?: 0.0 }
    val incomeSum = groupedData.sumOf { it.second.takeIf { v -> v > 0 } ?: 0.0 }

    val balance = userViewModel.balance

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF0F4F7)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header verde
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00B984))
                    .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Análisis",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { /* acciones adicionales */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tarjeta resumen: si es mensual usamos monthlyExpenses (optimización), sino usamos expenseSum
                val displayedExpense = if (debouncedSelected == 2) {
                    if (monthlyLoading) Double.NaN else monthlyExpenses
                } else {
                    expenseSum
                }

                SummaryCard(
                    modifier = Modifier.fillMaxWidth(),
                    balanceLabel = "Saldo total",
                    balanceValue = "S/. ${formatCurrency(balance)}",
                    expenseLabel = "Gastos",
                    expenseValue = if (debouncedSelected == 2 && monthlyLoading) "-S/.--" else "-S/. ${formatCurrency(displayedExpense.takeIf { !it.isNaN() } ?: 0.0)}",
                    progressLabel = ""
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Area blanca redondeada con contenido
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Selector de periodos
                    PeriodToggle(selected = selectedPeriod, onSelected = { selectedPeriod = it })

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gráfico de barras con los datos agrupados
                    BarChart(data = groupedData)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // BottomBar
            BottomBar(
                modifier = Modifier.fillMaxWidth(),
                items = listOf(
                    Icons.Default.Home,
                    Icons.Default.BarChart,
                    Icons.Default.SwapHoriz,
                    Icons.Default.Layers,
                    Icons.Default.Person
                ),
                selectedIndex = selectedIndex,
                onItemSelected = onBottomItemSelected
            )
        }
    }
}

// Composable simple de barras: recibe lista de pares (label, value). Value positivo = ingreso, negativo = gasto
@Composable
private fun BarChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val maxPositive = data.maxOfOrNull { if (it.second > 0) it.second else 0.0 } ?: 0.0
    val maxNegative = data.maxOfOrNull { if (it.second < 0) -it.second else 0.0 } ?: 0.0
    val overallMax = max(maxPositive, maxNegative)

    Column(modifier = modifier.fillMaxWidth()) {
        // Area de gráfico
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFFF6FFF9))) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                data.forEach { (label, value) ->
                    val heightFraction = if (overallMax <= 0.0) 0.0 else (kotlin.math.abs(value) / overallMax)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                        // Bar: azul para ingresos (positivo), rojo para gastos (negativo)
                        Box(modifier = Modifier
                            .height((heightFraction * 160).dp) // escalar a 160dp máximo
                            .width(18.dp)
                            .background(if (value >= 0) Color(0xFF0B6EFF) else Color(0xFFD32F2F)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = label, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodToggle(selected: Int, onSelected: (Int) -> Unit) {
    val periods = listOf("Diario", "Semanal", "Mensual", "Anual")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FFF9)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            periods.forEachIndexed { index, label ->
                val isSelected = selected == index
                Button(
                    onClick = { onSelected(index) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF00D1A1) else Color.White,
                        contentColor = if (isSelected) Color.White else Color.Gray
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(text = label)
                }
            }
        }
    }
}

// Agrupa las transacciones según el periodo y devuelve lista de pares (label, value).
// value será negativo para gastos, positivo para ingresos.
private fun groupTransactionsForPeriod(transactions: List<TransactionsViewModel.TransactionDto>, period: Int): List<Pair<String, Double>> {
    when (period) {
        0 -> { // Diario -> últimos 7 días (L..D)
            val labels = mutableListOf<String>()
            val sums = mutableListOf<Double>()
            val cal = Calendar.getInstance()
            // empezar 6 días atrás hasta hoy
            cal.add(Calendar.DAY_OF_YEAR, -6)
            repeat(7) {
                val dayStart = cal.time
                val dayEndCal = cal.clone() as Calendar
                dayEndCal.set(Calendar.HOUR_OF_DAY, 23); dayEndCal.set(Calendar.MINUTE, 59); dayEndCal.set(Calendar.SECOND, 59); dayEndCal.set(Calendar.MILLISECOND, 999)
                val dayEnd = dayEndCal.time
                val sum = transactions.filter { it.date != null && it.date in dayStart..dayEnd }.sumOf { it.amount * if (it.isExpense) -1 else 1 }
                labels.add(shortDayLabel(cal.get(Calendar.DAY_OF_WEEK)))
                sums.add(sum)
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return labels.zip(sums)
        }
        1 -> { // Semanal -> semanas del mes actual (semanas que cubren el mes)
            val labels = mutableListOf<String>()
            val sums = mutableListOf<Double>()

            // Determinar primer día del mes y último día
            val monthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val monthEnd = monthStart.clone() as Calendar
            monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
            monthEnd.set(Calendar.HOUR_OF_DAY, 23); monthEnd.set(Calendar.MINUTE, 59); monthEnd.set(Calendar.SECOND, 59); monthEnd.set(Calendar.MILLISECOND, 999)

            // Encontrar inicio de la primera semana (lunes) que incluye el 1ro del mes
            val firstWeekStart = monthStart.clone() as Calendar
            firstWeekStart.firstDayOfWeek = Calendar.MONDAY
            firstWeekStart.set(Calendar.DAY_OF_WEEK, firstWeekStart.firstDayOfWeek)
            if (firstWeekStart.time.after(monthStart.time)) {
                firstWeekStart.add(Calendar.DAY_OF_YEAR, -7)
            }

            // Iterar semana por semana hasta cubrir el último día del mes
            val weekStart = firstWeekStart.clone() as Calendar
            var idx = 1
            while (!weekStart.time.after(monthEnd.time)) {
                val start = weekStart.time
                val weekEnd = weekStart.clone() as Calendar
                weekEnd.add(Calendar.DAY_OF_MONTH, 6)
                weekEnd.set(Calendar.HOUR_OF_DAY, 23); weekEnd.set(Calendar.MINUTE, 59); weekEnd.set(Calendar.SECOND, 59); weekEnd.set(Calendar.MILLISECOND, 999)
                val end = weekEnd.time

                // Sumar transacciones que caen en este rango (dentro del mes)
                val sum = transactions.filter { it.date != null && it.date in start..end }.sumOf { it.amount * if (it.isExpense) -1 else 1 }
                labels.add("S$idx")
                sums.add(sum)

                // siguiente semana
                weekStart.add(Calendar.WEEK_OF_YEAR, 1)
                idx++
            }

            return labels.zip(sums)
        }
        2 -> { // Mensual -> meses del año (Ene..Dic), sumar por mes del año actual
            val labels = (0..11).map { monthShortLabel(it) }
            val sums = MutableList(12) { 0.0 }
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            transactions.forEach { tx ->
                val d = tx.date ?: return@forEach
                val c = Calendar.getInstance()
                c.time = d
                if (c.get(Calendar.YEAR) == year) {
                    val m = c.get(Calendar.MONTH)
                    sums[m] = sums[m] + tx.amount * if (tx.isExpense) -1 else 1
                }
            }
            return labels.zip(sums)
        }
        3 -> { // Anual -> últimos 5 años
            val labels = mutableListOf<String>()
            val sums = mutableListOf<Double>()
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -4)
            repeat(5) {
                val y = cal.get(Calendar.YEAR)
                val startCal = Calendar.getInstance(); startCal.set(Calendar.YEAR, y); startCal.set(Calendar.DAY_OF_YEAR, 1); startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
                val endCal = Calendar.getInstance(); endCal.set(Calendar.YEAR, y); endCal.set(Calendar.MONTH, 11); endCal.set(Calendar.DAY_OF_MONTH, 31); endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59); endCal.set(Calendar.MILLISECOND, 999)
                val start = startCal.time
                val end = endCal.time
                val sum = transactions.filter { it.date != null && it.date in start..end }.sumOf { it.amount * if (it.isExpense) -1 else 1 }
                labels.add(y.toString())
                sums.add(sum)
                cal.add(Calendar.YEAR, 1)
            }
            return labels.zip(sums)
        }
        else -> return emptyList()
    }
}

private fun shortDayLabel(dayOfWeek: Int): String = when (dayOfWeek) {
    Calendar.MONDAY -> "L"
    Calendar.TUESDAY -> "M"
    Calendar.WEDNESDAY -> "M"
    Calendar.THURSDAY -> "J"
    Calendar.FRIDAY -> "V"
    Calendar.SATURDAY -> "S"
    Calendar.SUNDAY -> "D"
    else -> ""
}

private fun monthShortLabel(monthIndex: Int): String = when (monthIndex) {
    0 -> "Ene"
    1 -> "Feb"
    2 -> "Mar"
    3 -> "Abr"
    4 -> "May"
    5 -> "Jun"
    6 -> "Jul"
    7 -> "Ago"
    8 -> "Sep"
    9 -> "Oct"
    10 -> "Nov"
    else -> "Dic"
}

// Helpers para periodos (devuelven Pair<start, end>)
private fun periodDayRange(now: Date): Pair<Date, Date> {
    val cal = Calendar.getInstance()
    cal.time = now
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.time
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val end = cal.time
    return Pair(start, end)
}

private fun periodMonthRange(now: Date): Pair<Date, Date> {
    val cal = Calendar.getInstance()
    cal.time = now
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.time
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val end = cal.time
    return Pair(start, end)
}

private fun periodYearRange(now: Date): Pair<Date, Date> {
    val cal = Calendar.getInstance()
    cal.time = now
    cal.set(Calendar.DAY_OF_YEAR, cal.getActualMinimum(Calendar.DAY_OF_YEAR))
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.time
    cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val end = cal.time
    return Pair(start, end)
}

private fun formatCurrency(amount: Double): String {
    val dfs = DecimalFormatSymbols(Locale.forLanguageTag("es"))
    dfs.groupingSeparator = ','
    dfs.decimalSeparator = '.'
    val df = DecimalFormat("#,##0.00", dfs)
    return df.format(amount)
}
