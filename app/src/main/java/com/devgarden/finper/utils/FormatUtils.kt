package com.devgarden.finper.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidades de formateo centralizadas para toda la aplicación.
 * Evita duplicación de código y asegura consistencia en el formato de datos.
 */
object FormatUtils {

    // --- Constantes de Formato ---
    private const val CURRENCY_SYMBOL = "S/."
    private const val CURRENCY_PATTERN = "#,##0.00"
    private const val DATE_PATTERN_DISPLAY = "d 'de' MMMM yyyy"
    private const val TIME_PATTERN_DISPLAY = "h:mm a"
    private const val DATE_SHORT_PATTERN = "dd/MM/yyyy"
    private const val LOCALE_LANGUAGE = "es"

    // --- Formateadores reutilizables (thread-safe con ThreadLocal si fuera necesario) ---
    private val currencySymbols = DecimalFormatSymbols().apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val currencyFormatter = DecimalFormat(CURRENCY_PATTERN, currencySymbols)

    private val dateDisplayFormatter = SimpleDateFormat(DATE_PATTERN_DISPLAY, Locale.forLanguageTag(LOCALE_LANGUAGE))

    private val timeDisplayFormatter = SimpleDateFormat(TIME_PATTERN_DISPLAY, Locale.forLanguageTag(LOCALE_LANGUAGE))

    private val dateShortFormatter = SimpleDateFormat(DATE_SHORT_PATTERN, Locale.forLanguageTag(LOCALE_LANGUAGE))

    /**
     * Formatea un monto como moneda con el símbolo S/. y separadores apropiados.
     * @param amount Monto a formatear
     * @param includeSymbol Si se incluye el símbolo de moneda (por defecto true)
     * @return String formateado (ej: "S/.1,234.56")
     */
    fun formatCurrency(amount: Double, includeSymbol: Boolean = true): String {
        val formatted = currencyFormatter.format(amount)
        return if (includeSymbol) "$CURRENCY_SYMBOL$formatted" else formatted
    }

    /**
     * Formatea un número con separadores de miles.
     * @param number Número a formatear
     * @return String formateado (ej: "1,234.56")
     */
    fun formatNumber(number: Double): String = currencyFormatter.format(number)

    /**
     * Formatea una fecha para mostrar en pantalla (ej: "15 de Abril 2025").
     * @param date Fecha a formatear
     * @return String formateado o vacío si la fecha es null
     */
    fun formatDateForDisplay(date: Date?): String {
        return date?.let { dateDisplayFormatter.format(it) } ?: ""
    }

    /**
     * Formatea una hora para mostrar en pantalla (ej: "6:30 PM").
     * @param date Fecha/hora a formatear
     * @return String formateado o vacío si la fecha es null
     */
    fun formatTimeForDisplay(date: Date?): String {
        return date?.let { timeDisplayFormatter.format(it) } ?: ""
    }

    /**
     * Formatea una fecha en formato corto (ej: "15/04/2025").
     * @param date Fecha a formatear
     * @return String formateado o vacío si la fecha es null
     */
    fun formatDateShort(date: Date?): String {
        return date?.let { dateShortFormatter.format(it) } ?: ""
    }

    /**
     * Formatea fecha y hora juntas (ej: "15 de Abril 2025 6:30 PM").
     * @param date Fecha/hora a formatear
     * @return String formateado o vacío si la fecha es null
     */
    fun formatDateTimeForDisplay(date: Date?): String {
        return if (date != null) {
            "${formatDateForDisplay(date)} ${formatTimeForDisplay(date)}"
        } else ""
    }

    /**
     * Formatea un monto como ingreso (positivo, sin signo negativo).
     * @param amount Monto del ingreso
     * @return String formateado (ej: "S/.4,000.00")
     */
    fun formatIncome(amount: Double): String = formatCurrency(amount)

    /**
     * Formatea un monto como gasto (negativo, con signo -).
     * @param amount Monto del gasto (debe ser positivo, se agregará el signo -)
     * @return String formateado (ej: "-S/.100.00")
     */
    fun formatExpense(amount: Double): String = "-${formatCurrency(amount)}"

    /**
     * Convierte un String a Double de forma segura.
     * @param value String a convertir
     * @param defaultValue Valor por defecto si la conversión falla
     * @return Double resultante
     */
    fun parseDouble(value: String, defaultValue: Double = 0.0): Double {
        return value.toDoubleOrNull() ?: defaultValue
    }
}

