package com.devgarden.finper.ui.features.transactions

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devgarden.finper.ui.theme.PrimaryGreen
import com.devgarden.finper.utils.Constants
import com.devgarden.finper.utils.FormatUtils
import java.util.*

/**
 * Diálogo mejorado para crear nueva transacción.
 *
 * @param show Si el diálogo debe mostrarse
 * @param isExpense Si es un gasto (true) o ingreso (false)
 * @param titleValue Valor del título/descripción
 * @param onTitleChange Callback cuando cambia el título
 * @param amountValue Valor del monto
 * @param onAmountChange Callback cuando cambia el monto
 * @param categoryValue Categoría seleccionada
 * @param onCategoryChange Callback cuando cambia la categoría
 * @param dateValue Fecha seleccionada
 * @param onDateChange Callback cuando cambia la fecha
 * @param onDismiss Callback al cerrar el diálogo
 * @param onSave Callback al guardar la transacción
 */
@Composable
fun NewTransactionDialog(
    show: Boolean,
    isExpense: Boolean,
    titleValue: String,
    onTitleChange: (String) -> Unit,
    amountValue: String,
    onAmountChange: (String) -> Unit,
    categoryValue: String,
    onCategoryChange: (String) -> Unit,
    dateValue: Date?,
    onDateChange: (Date) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!show) return

    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    // Usar categorías predefinidas
    val categories = Constants.Defaults.PREDEFINED_CATEGORIES

    if (showPicker) {
        val cal = Calendar.getInstance().apply { time = dateValue ?: Date() }
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            val now = Calendar.getInstance()
            val chosen = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                set(Calendar.SECOND, now.get(Calendar.SECOND))
            }
            onDateChange(chosen.time)
            showPicker = false
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visible = show,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Encabezado con icono y título
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isExpense) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isExpense) Icons.Default.RemoveCircle else Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = if (isExpense) Color(0xFFD32F2F) else PrimaryGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isExpense) "Agregar Gasto" else "Agregar Ingreso",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Completa los datos",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Campo de Título
                    OutlinedTextField(
                        value = titleValue,
                        onValueChange = onTitleChange,
                        label = { Text("Título o Descripción") },
                        placeholder = { Text(if (isExpense) "Ej: Compras del supermercado" else "Ej: Salario mensual") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = PrimaryGreen,
                            cursorColor = PrimaryGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Monto
                    OutlinedTextField(
                        value = amountValue,
                        onValueChange = onAmountChange,
                        label = { Text("Monto") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = if (isExpense) Color(0xFFD32F2F) else PrimaryGreen
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isExpense) Color(0xFFD32F2F) else PrimaryGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = if (isExpense) Color(0xFFD32F2F) else PrimaryGreen,
                            cursorColor = if (isExpense) Color(0xFFD32F2F) else PrimaryGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Categoría (ahora sin loading ni error)
                    CategoryDropdown(
                        categories = categories,
                        selected = categoryValue,
                        onSelected = onCategoryChange
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Fecha
                    OutlinedTextField(
                        value = dateValue?.let {
                            "${FormatUtils.formatDateForDisplay(it)} ${FormatUtils.formatTimeForDisplay(it)}"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Fecha y Hora") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Cambiar fecha",
                                    tint = PrimaryGreen
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = PrimaryGreen,
                            disabledBorderColor = Color(0xFFE0E0E0),
                            disabledTextColor = Color(0xFF2D3748)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text(
                                "Cancelar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = onSave,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isExpense) Color(0xFFD32F2F) else PrimaryGreen
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Text(
                                "Guardar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dropdown mejorado para seleccionar categoría.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.ifBlank { "" },
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            label = { Text("Categoría") },
            placeholder = { Text("Selecciona una categoría") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedLabelColor = PrimaryGreen,
                cursorColor = PrimaryGreen
            ),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (categories.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(Constants.Defaults.DEFAULT_CATEGORY)
                        }
                    },
                    onClick = {
                        onSelected(Constants.Defaults.DEFAULT_CATEGORY)
                        expanded = false
                    }
                )
            } else {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(category)
                            }
                        },
                        onClick = {
                            onSelected(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
