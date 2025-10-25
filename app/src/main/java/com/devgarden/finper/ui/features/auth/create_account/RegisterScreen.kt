package com.devgarden.finper.ui.features.auth.create_account

import android.app.DatePickerDialog
import android.widget.DatePicker
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.ui.theme.DarkTextColor
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.LightGrayText
import com.devgarden.finper.ui.theme.PrimaryGreen

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit,
    vm: RegisterViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf(TextFieldValue("")) }
    var prevBirthDate by remember { mutableStateOf(TextFieldValue("")) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by vm.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onRegistered()
            vm.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título "Crea Tu Cuenta"
            Text(
                text = "Crea Tu Cuenta",
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 70.dp)
                    .fillMaxHeight(0.13f)
            )

            // Tarjeta blanca del formulario
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 30.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 40.dp)
                        .padding(bottom = 16.dp), // Padding extra en la parte inferior para que no se pegue
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Campo Nombre Completo
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = outlinedTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = outlinedTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Número De Teléfono
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Numero De Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = outlinedTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Fecha De Cumpleaños editable con formateo automático (dd/MM/yyyy)
                    val calendar = remember { Calendar.getInstance() }
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    val maxDateMillis = System.currentTimeMillis()
                    val minDateMillis = Calendar.getInstance().apply { add(Calendar.YEAR, -100) }.timeInMillis

                    val datePickerDialog = remember {
                        DatePickerDialog(
                            context,
                            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                                val selectedDate = Calendar.getInstance()
                                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val formatted = sdf.format(selectedDate.time)
                                birthDate = TextFieldValue(formatted, selection = TextRange(formatted.length))
                                prevBirthDate = birthDate
                            }, year, month, day
                        ).apply {
                            datePicker.maxDate = maxDateMillis
                            datePicker.minDate = minDateMillis
                        }
                    }

                    fun formatDigitsToDate(digits: String): String {
                        val d = digits.take(8)
                        return when (d.length) {
                            0,1,2 -> d
                            3,4 -> d.substring(0,2) + "/" + d.substring(2)
                            else -> d.substring(0,2) + "/" + d.substring(2,4) + "/" + d.substring(4)
                        }
                    }

                    fun countDigitsBefore(text: String, index: Int): Int {
                        val safeIndex = index.coerceIn(0, text.length)
                        return text.substring(0, safeIndex).count { it.isDigit() }
                    }

                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { newValue: TextFieldValue ->
                            val digitsOnly = newValue.text.filter { it.isDigit() }.take(8)

                            if (digitsOnly.length == 8) {
                                try {
                                    val sdfInput = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
                                    sdfInput.isLenient = false
                                    val parsed = sdfInput.parse(digitsOnly)
                                    if (parsed != null) {
                                        val time = parsed.time
                                        val clamped = when {
                                            time > maxDateMillis -> maxDateMillis
                                            time < minDateMillis -> minDateMillis
                                            else -> time
                                        }
                                        val sdfOut = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        val formatted = sdfOut.format(Date(clamped))
                                        // Colocar cursor al final
                                        birthDate = TextFieldValue(formatted, selection = TextRange(formatted.length))
                                        prevBirthDate = birthDate
                                        return@OutlinedTextField
                                    }
                                } catch (_: Exception) {
                                    // ignore and fallback to partial formatting
                                }
                            }

                            // Si no se llegó a 8 dígitos o parseo falló, aplicar formato parcial y preservar posición
                            val formattedPartial = formatDigitsToDate(digitsOnly)

                            // Estrategia mejorada para preservar el cursor:
                            // 1) contar dígitos en prevBirthDate y en newValue para saber cuántos dígitos se insertaron/borarron
                            val prevDigits = prevBirthDate.text.filter { it.isDigit() }
                            val prevDigitsCount = prevDigits.length
                            val newDigitsCount = digitsOnly.length
                            val insertedDigits = (newDigitsCount - prevDigitsCount).coerceAtLeast(0)

                            // 2) calcular la posición de dígitos donde estaba el cursor antes
                            val prevDigitCursor = countDigitsBefore(prevBirthDate.text, prevBirthDate.selection.start)

                            // 3) estimar la nueva posición en dígitos: avanzar prevDigitCursor por los dígitos insertados
                            val desiredDigitCursor = (prevDigitCursor + insertedDigits).coerceAtMost(newDigitsCount)

                            // 4) mapear la posición en dígitos a la posición en el string formateado
                            val newCursorPos = when {
                                desiredDigitCursor <= 2 -> desiredDigitCursor
                                desiredDigitCursor <= 4 -> desiredDigitCursor + 1 // una barra antes
                                else -> desiredDigitCursor + 2 // dos barras antes
                            }.coerceAtMost(formattedPartial.length)

                            birthDate = TextFieldValue(formattedPartial, selection = TextRange(newCursorPos))
                            prevBirthDate = birthDate
                        },
                        label = { Text("Fecha De Nacimiento") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "Seleccionar fecha",
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            )
                        },
                        colors = outlinedTextFieldColors(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Contraseña
                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        isPasswordVisible = isPasswordVisible,
                        onVisibilityChange = { isPasswordVisible = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Confirmar Contraseña
                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmar Contraseña",
                        isPasswordVisible = isConfirmPasswordVisible,
                        onVisibilityChange = { isConfirmPasswordVisible = it }
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Términos y condiciones
                    Text(
                        text = "Al continuar aceptas nuestros términos y condiciones",
                        color = LightGrayText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón Registrarse
                    Button(
                        onClick = {
                            vm.register(
                                fullName = fullName,
                                email = email,
                                phone = phoneNumber,
                                birthDate = birthDate.text,
                                password = password,
                                confirmPassword = confirmPassword
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        enabled = uiState !is RegisterUiState.Loading
                    ) {
                        if (uiState is RegisterUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Registrarse", fontSize = 18.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState is RegisterUiState.Error) {
                        val msg = (uiState as RegisterUiState.Error).message
                        Text(text = msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onLoginClick) {
                        Text(
                            text = "ya tienes una cuenta? Ingresa",
                            color = DarkTextColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPasswordVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
            IconButton(onClick = { onVisibilityChange(!isPasswordVisible) }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        colors = outlinedTextFieldColors()
    )
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryGreen,
    unfocusedBorderColor = Color.LightGray,
    focusedLabelColor = PrimaryGreen,
    unfocusedLabelColor = Color.Gray,
    cursorColor = PrimaryGreen
)

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun RegisterScreenPreview() {
    FinperTheme {
        RegisterScreen(onRegistered = {}, onLoginClick = {})
    }
}