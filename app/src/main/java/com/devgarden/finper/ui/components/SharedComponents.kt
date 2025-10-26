package com.devgarden.finper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.ui.theme.PrimaryGreen

/**
 * Constantes de UI compartidas para componentes reutilizables.
 */
private object ComponentDefaults {
    val HEADER_BOTTOM_RADIUS = 40.dp
    val CARD_RADIUS = 24.dp
    val ICON_BACKGROUND_SIZE = 44.dp
    val PROGRESS_HEIGHT = 8.dp

    val CARD_BACKGROUND_COLOR = Color(0xFFF5FFF9)
    val TEXT_PRIMARY_COLOR = Color(0xFF15323B)
    val EXPENSE_COLOR = Color(0xFF00A78E)
    val PROGRESS_COLOR = Color(0xFF00B974)
    val PROGRESS_TRACK_COLOR = Color(0xFFF0F6F3)
    val EXPENSE_BACKGROUND_COLOR = Color(0xFFFFF6F6)
    val INCOME_BACKGROUND_COLOR = Color(0xFFEFFCF2)
}

/**
 * Encabezado redondeado con título centrado.
 *
 * @param title Título a mostrar en el encabezado
 * @param modifier Modificador opcional
 * @param showBack Si se muestra el botón de retroceso (no implementado actualmente)
 * @param onBack Callback al presionar el botón de retroceso
 */
@Composable
fun TopRoundedHeader(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(
                bottomStart = ComponentDefaults.HEADER_BOTTOM_RADIUS,
                bottomEnd = ComponentDefaults.HEADER_BOTTOM_RADIUS
            ))
            .background(PrimaryGreen)
            .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Tarjeta de resumen con balance y gastos.
 *
 * @param modifier Modificador opcional
 * @param balanceLabel Etiqueta del balance
 * @param balanceValue Valor del balance formateado
 * @param expenseLabel Etiqueta de gastos
 * @param expenseValue Valor de gastos formateado
 * @param progressLabel Etiqueta del progreso (actualmente no se usa la barra de progreso)
 */
@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    balanceLabel: String,
    balanceValue: String,
    expenseLabel: String,
    expenseValue: String,
    progressLabel: String
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ComponentDefaults.CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = ComponentDefaults.CARD_BACKGROUND_COLOR),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = balanceLabel,
                        color = ComponentDefaults.TEXT_PRIMARY_COLOR,
                        fontSize = 12.sp
                    )
                    Text(
                        text = balanceValue,
                        color = ComponentDefaults.TEXT_PRIMARY_COLOR,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = expenseLabel,
                        color = ComponentDefaults.TEXT_PRIMARY_COLOR,
                        fontSize = 12.sp
                    )
                    Text(
                        text = expenseValue,
                        color = ComponentDefaults.EXPENSE_COLOR,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (progressLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = progressLabel,
                    fontSize = 12.sp,
                    color = ComponentDefaults.TEXT_PRIMARY_COLOR
                )
            }
        }
    }
}

/**
 * Item de transacción reutilizable para listas.
 *
 * @param icon Icono de la transacción
 * @param title Título de la transacción
 * @param subtitle Subtítulo (fecha/hora)
 * @param category Categoría de la transacción
 * @param amount Monto formateado
 * @param isExpense Si es un gasto (true) o ingreso (false)
 * @param modifier Modificador opcional
 */
@Composable
fun TransactionListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    category: String,
    amount: String,
    isExpense: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo circular
            IconWithBackground(
                icon = icon,
                backgroundColor = if (isExpense)
                    ComponentDefaults.EXPENSE_BACKGROUND_COLOR
                else
                    ComponentDefaults.INCOME_BACKGROUND_COLOR,
                iconTint = if (isExpense) Color.Red else Color(0xFF4285F4)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la transacción
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    color = Color.Black,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Monto y categoría
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) Color.Red else Color(0xFF00B974),
                    fontSize = 14.sp
                )
                Text(
                    text = category,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Icono con fondo circular coloreado.
 *
 * @param icon ImageVector del icono
 * @param backgroundColor Color de fondo del círculo
 * @param iconTint Color del icono
 * @param size Tamaño del contenedor circular
 * @param modifier Modificador opcional
 */
@Composable
fun IconWithBackground(
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    size: Dp = ComponentDefaults.ICON_BACKGROUND_SIZE,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * Toggle de periodo (Diario, Semanal, Mensual).
 *
 * @param selectedIndex Índice del periodo seleccionado (0=Diario, 1=Semanal, 2=Mensual)
 * @param onSelected Callback cuando se selecciona un periodo
 * @param modifier Modificador opcional
 */
@Composable
fun TimePeriodToggle(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Diario", "Semanal", "Mensual")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelected(index) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Card de carga (loading state).
 *
 * @param message Mensaje a mostrar junto al indicador de carga
 * @param modifier Modificador opcional
 */
@Composable
fun LoadingCard(
    message: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ComponentDefaults.CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = message, fontSize = 14.sp)
        }
    }
}

/**
 * Card de error con mensaje.
 *
 * @param message Mensaje de error a mostrar
 * @param modifier Modificador opcional
 */
@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ComponentDefaults.CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6F6))
    ) {
        Text(
            text = message,
            color = Color(0xFFD32F2F),
            modifier = Modifier.padding(20.dp),
            fontSize = 14.sp
        )
    }
}
