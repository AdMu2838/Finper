package com.devgarden.finper.ui.features.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var selectedPeriod by remember { mutableStateOf(0) } // por defecto Diario

    // Para el período mensual, controlar qué semestre mostrar (0=Ene-Jun, 1=Jul-Dic)
    // Inicializar según el mes actual: 0-5 = primer semestre, 6-11 = segundo semestre
    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) }
    var monthSemester by remember { mutableStateOf(if (currentMonth < 6) 0 else 1) }

    // DebouncedSelected usado para aplicar la lógica (evita cambiar listeners muy rápido)
    var debouncedSelected by remember { mutableStateOf(selectedPeriod) }

    // aplicar debounce cuando selectedPeriod cambie
    LaunchedEffect(selectedPeriod) {
        val current = selectedPeriod
        delay(300) // debounce 300ms
        debouncedSelected = current
    }

    // Cuando cambie debouncedSelected, actualizar la consulta de rango
    LaunchedEffect(debouncedSelected) {
        val now = Date()
        val range = when (debouncedSelected) {
            0 -> periodDayRange(now)
            1 -> periodMonthRange(now)
            2 -> periodYearRange(now) // Mensual carga todo el año
            3 -> periodMultiYearRange(now)
            else -> periodDayRange(now)
        }
        transactionsViewModel.listenTransactionsRange(range.first, range.second)
    }

    // Observables
    val transactions = transactionsViewModel.transactions

    // Gastos del mes actual (siempre)
    val monthlyExpenses = transactionsViewModel.monthlyExpenses

    // Calcular datos agrupados para el gráfico (ahora con ingresos y gastos separados)
    val groupedData by remember(transactions, debouncedSelected, monthSemester) {
        derivedStateOf { groupTransactionsForPeriodSeparated(transactions, debouncedSelected, monthSemester) }
    }

    val balance = userViewModel.balance

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header verde
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Análisis Financiero",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { /* acciones adicionales */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tarjeta resumen con gastos del mes actual
                SummaryCard(
                    modifier = Modifier.fillMaxWidth(),
                    balanceLabel = "Saldo total",
                    balanceValue = "S/. ${formatCurrency(balance)}",
                    expenseLabel = "Gastos del mes",
                    expenseValue = "-S/. ${formatCurrency(monthlyExpenses)}",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Selector de periodos
                    PeriodToggle(selected = selectedPeriod, onSelected = { selectedPeriod = it })

                    Spacer(modifier = Modifier.height(16.dp))

                    // Si es mensual, mostrar selector de semestre
                    if (selectedPeriod == 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { monthSemester = 0 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (monthSemester == 0) Color(0xFF00D1A1) else Color(0xFFE0E0E0),
                                    contentColor = if (monthSemester == 0) Color.White else Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ene - Jun")
                            }
                            Button(
                                onClick = { monthSemester = 1 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (monthSemester == 1) Color(0xFF00D1A1) else Color(0xFFE0E0E0),
                                    contentColor = if (monthSemester == 1) Color.White else Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Jul - Dic")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Leyenda del gráfico
                    ChartLegend()

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gráfico de barras mejorado
                    ImprovedBarChart(data = groupedData)

                    Spacer(modifier = Modifier.height(80.dp)) // Espacio para el BottomBar
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

// Modelo de datos para el gráfico mejorado
data class ChartDataPoint(
    val label: String,
    val income: Double,
    val expense: Double
)

// Leyenda del gráfico
@Composable
private fun ChartLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ingresos
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF00C896), RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Ingresos",
                fontSize = 13.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Gastos
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFFF6B6B), RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Gastos",
                fontSize = 13.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Gráfico de barras mejorado con ingresos y gastos separados (horizontal)
@Composable
private fun ImprovedBarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { max(it.income, it.expense) } ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFCFF)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Comparativa de Ingresos y Gastos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Área del gráfico horizontal
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                data.forEachIndexed { index, dataPoint ->
                    HorizontalBarGroup(
                        label = dataPoint.label,
                        income = dataPoint.income,
                        expense = dataPoint.expense,
                        maxValue = maxValue,
                        isLast = index == data.size - 1
                    )
                }
            }
        }
    }
}

// Grupo de barras horizontales (una para ingresos, otra para gastos)
@Composable
private fun HorizontalBarGroup(
    label: String,
    income: Double,
    expense: Double,
    maxValue: Double,
    isLast: Boolean = false
) {
    val incomeWidth = if (maxValue > 0) (income / maxValue).toFloat() else 0f
    val expenseWidth = if (maxValue > 0) (expense / maxValue).toFloat() else 0f

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Etiqueta del período
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Barra de Ingresos
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ing:",
                fontSize = 11.sp,
                color = Color(0xFF00C896),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(32.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .background(Color(0xFFE8F5F1), RoundedCornerShape(12.dp))
            ) {
                if (income > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(incomeWidth)
                            .fillMaxHeight()
                            .background(
                                Color(0xFF00C896),
                                RoundedCornerShape(12.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatCurrencyShort(income),
                fontSize = 11.sp,
                color = Color(0xFF00C896),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Barra de Gastos
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gas:",
                fontSize = 11.sp,
                color = Color(0xFFFF6B6B),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(32.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .background(Color(0xFFFFF0F0), RoundedCornerShape(12.dp))
            ) {
                if (expense > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(expenseWidth)
                            .fillMaxHeight()
                            .background(
                                Color(0xFFFF6B6B),
                                RoundedCornerShape(12.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatCurrencyShort(expense),
                fontSize = 11.sp,
                color = Color(0xFFFF6B6B),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.End
            )
        }

        // Línea divisoria entre grupos
        if (!isLast) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
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
        Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(text = label, fontSize = 12.sp)
                }
            }
        }
    }
}

// Agrupa las transacciones según el periodo y devuelve lista de ChartDataPoint con ingresos y gastos separados
private fun groupTransactionsForPeriodSeparated(
    transactions: List<TransactionsViewModel.TransactionDto>,
    period: Int,
    monthSemester: Int = 0
): List<ChartDataPoint> {
    when (period) {
        0 -> { // Diario -> días de la semana actual (L a D)
            val cal = Calendar.getInstance()
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val result = mutableListOf<ChartDataPoint>()
            repeat(7) {
                val dayStart = cal.time
                val dayEndCal = cal.clone() as Calendar
                dayEndCal.set(Calendar.HOUR_OF_DAY, 23)
                dayEndCal.set(Calendar.MINUTE, 59)
                dayEndCal.set(Calendar.SECOND, 59)
                dayEndCal.set(Calendar.MILLISECOND, 999)
                val dayEnd = dayEndCal.time

                val dayTransactions = transactions.filter { tx ->
                    tx.date != null && tx.date >= dayStart && tx.date <= dayEnd
                }

                val income = dayTransactions.filter { !it.isExpense }.sumOf { it.amount }
                val expense = dayTransactions.filter { it.isExpense }.sumOf { it.amount }

                result.add(ChartDataPoint(
                    label = shortDayLabel(cal.get(Calendar.DAY_OF_WEEK)),
                    income = income,
                    expense = expense
                ))
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return result
        }
        1 -> { // Semanal -> semanas del mes actual
            val monthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val monthEnd = monthStart.clone() as Calendar
            monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
            monthEnd.set(Calendar.HOUR_OF_DAY, 23); monthEnd.set(Calendar.MINUTE, 59); monthEnd.set(Calendar.SECOND, 59); monthEnd.set(Calendar.MILLISECOND, 999)

            val firstWeekStart = monthStart.clone() as Calendar
            firstWeekStart.firstDayOfWeek = Calendar.MONDAY
            firstWeekStart.set(Calendar.DAY_OF_WEEK, firstWeekStart.firstDayOfWeek)
            if (firstWeekStart.time.after(monthStart.time)) {
                firstWeekStart.add(Calendar.DAY_OF_YEAR, -7)
            }

            val result = mutableListOf<ChartDataPoint>()
            val weekStart = firstWeekStart.clone() as Calendar
            var idx = 1
            while (!weekStart.time.after(monthEnd.time)) {
                val start = weekStart.time
                val weekEnd = weekStart.clone() as Calendar
                weekEnd.add(Calendar.DAY_OF_MONTH, 6)
                weekEnd.set(Calendar.HOUR_OF_DAY, 23); weekEnd.set(Calendar.MINUTE, 59); weekEnd.set(Calendar.SECOND, 59); weekEnd.set(Calendar.MILLISECOND, 999)
                val end = weekEnd.time

                val weekTransactions = transactions.filter { it.date != null && it.date in start..end }
                val income = weekTransactions.filter { !it.isExpense }.sumOf { it.amount }
                val expense = weekTransactions.filter { it.isExpense }.sumOf { it.amount }

                result.add(ChartDataPoint(
                    label = "S$idx",
                    income = income,
                    expense = expense
                ))

                weekStart.add(Calendar.WEEK_OF_YEAR, 1)
                idx++
            }
            return result
        }
        2 -> { // Mensual -> mostrar 6 meses según el semestre seleccionado
            val startMonth = if (monthSemester == 0) 0 else 6
            val endMonth = if (monthSemester == 0) 5 else 11

            val labels = (startMonth..endMonth).map { monthShortLabel(it) }
            val incomes = MutableList(6) { 0.0 }
            val expenses = MutableList(6) { 0.0 }
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)

            transactions.forEach { tx ->
                val d = tx.date ?: return@forEach
                val c = Calendar.getInstance()
                c.time = d
                if (c.get(Calendar.YEAR) == year) {
                    val m = c.get(Calendar.MONTH)
                    if (m in startMonth..endMonth) {
                        val index = m - startMonth
                        if (tx.isExpense) {
                            expenses[index] = expenses[index] + tx.amount
                        } else {
                            incomes[index] = incomes[index] + tx.amount
                        }
                    }
                }
            }
            return labels.mapIndexed { index, label ->
                ChartDataPoint(label, incomes[index], expenses[index])
            }
        }
        3 -> { // Anual -> 5 años desde 2025 en adelante
            val result = mutableListOf<ChartDataPoint>()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val startYear = maxOf(2025, currentYear) // Comenzar desde 2025 o el año actual, lo que sea mayor

            repeat(5) { index ->
                val y = startYear + index
                val startCal = Calendar.getInstance()
                startCal.set(Calendar.YEAR, y)
                startCal.set(Calendar.DAY_OF_YEAR, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
                val start = startCal.time

                val endCal = Calendar.getInstance()
                endCal.set(Calendar.YEAR, y)
                endCal.set(Calendar.MONTH, 11)
                endCal.set(Calendar.DAY_OF_MONTH, 31)
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
                val end = endCal.time

                val yearTransactions = transactions.filter { it.date != null && it.date in start..end }
                val income = yearTransactions.filter { !it.isExpense }.sumOf { it.amount }
                val expense = yearTransactions.filter { it.isExpense }.sumOf { it.amount }

                result.add(ChartDataPoint(
                    label = y.toString(),
                    income = income,
                    expense = expense
                ))
            }
            return result
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

    // Establecer al lunes de la semana actual
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.time

    // Establecer al domingo de la semana actual
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
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
    cal.set(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.time
    cal.set(Calendar.MONTH, 11)
    cal.set(Calendar.DAY_OF_MONTH, 31)
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val end = cal.time
    return Pair(start, end)
}

private fun periodMultiYearRange(now: Date): Pair<Date, Date> {
    val cal = Calendar.getInstance()
    cal.time = now

    // Comenzar desde 2025 o el año actual, lo que sea mayor
    val currentYear = cal.get(Calendar.YEAR)
    val startYear = maxOf(2025, currentYear)

    cal.set(Calendar.YEAR, startYear)
    cal.set(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.time

    // Fin: 5 años después del inicio
    cal.set(Calendar.YEAR, startYear + 4)
    cal.set(Calendar.MONTH, 11)
    cal.set(Calendar.DAY_OF_MONTH, 31)
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

private fun formatCurrencyShort(amount: Double): String {
    val locale = Locale.forLanguageTag("es-PE")
    return when {
        amount >= 1000000 -> String.format(locale, "%.1fM", amount / 1000000)
        amount >= 1000 -> String.format(locale, "%.1fK", amount / 1000)
        else -> String.format(locale, "%.0f", amount)
    }
}
