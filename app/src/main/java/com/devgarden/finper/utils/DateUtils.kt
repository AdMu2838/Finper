package com.devgarden.finper.utils

import java.util.*

/**
 * Utilidades para manejo de fechas y rangos temporales.
 * Centraliza la lógica de cálculo de periodos (diario, semanal, mensual).
 */
object DateUtils {

    /**
     * Tipos de periodo temporal.
     */
    enum class Period {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    /**
     * Resultado de un rango de fechas.
     * @property start Fecha de inicio del rango
     * @property end Fecha de fin del rango
     */
    data class DateRange(val start: Date, val end: Date)

    /**
     * Configura un Calendar al inicio del día (00:00:00.000).
     */
    fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /**
     * Configura un Calendar al final del día (23:59:59.999).
     */
    fun Calendar.setToEndOfDay() {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    /**
     * Obtiene el rango de fechas para el día actual.
     * @return DateRange desde el inicio al fin del día actual
     */
    fun getDailyRange(): DateRange {
        val start = Calendar.getInstance().apply { setToStartOfDay() }.time
        val end = Calendar.getInstance().apply { setToEndOfDay() }.time
        return DateRange(start, end)
    }

    /**
     * Obtiene el rango de fechas para la semana actual (Lunes a Domingo).
     * @return DateRange desde el inicio del lunes al fin del domingo
     */
    fun getWeeklyRange(): DateRange {
        val cal = Calendar.getInstance().apply { setToStartOfDay() }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToMonday = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek

        cal.add(Calendar.DAY_OF_MONTH, daysToMonday)
        val start = cal.time

        cal.add(Calendar.DAY_OF_MONTH, 6)
        cal.setToEndOfDay()
        val end = cal.time

        return DateRange(start, end)
    }

    /**
     * Obtiene el rango de fechas para el mes actual.
     * @return DateRange desde el primer día al último día del mes
     */
    fun getMonthlyRange(): DateRange {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.setToStartOfDay()
        val start = cal.time

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.setToEndOfDay()
        val end = cal.time

        return DateRange(start, end)
    }

    /**
     * Obtiene el rango de fechas según el periodo especificado.
     * @param period Tipo de periodo (DAILY, WEEKLY, MONTHLY)
     * @return DateRange correspondiente al periodo
     */
    fun getRangeForPeriod(period: Period): DateRange = when (period) {
        Period.DAILY -> getDailyRange()
        Period.WEEKLY -> getWeeklyRange()
        Period.MONTHLY -> getMonthlyRange()
    }

    /**
     * Obtiene el rango de fechas según un índice (0=Diario, 1=Semanal, 2=Mensual).
     * @param periodIndex Índice del periodo
     * @return DateRange correspondiente al periodo
     */
    fun getRangeForPeriodIndex(periodIndex: Int): DateRange = when (periodIndex) {
        0 -> getDailyRange()
        1 -> getWeeklyRange()
        else -> getMonthlyRange()
    }

    /**
     * Verifica si una fecha está en el rango actual del mes.
     * @param date Fecha a verificar
     * @return true si la fecha está en el mes actual
     */
    fun isInCurrentMonth(date: Date?): Boolean {
        if (date == null) return false
        val range = getMonthlyRange()
        return date.time in range.start.time..range.end.time
    }

    /**
     * Obtiene la fecha de inicio del mes para una fecha dada.
     * @param date Fecha de referencia (por defecto hoy)
     * @return Fecha del primer día del mes a las 00:00:00.000
     */
    fun getStartOfMonth(date: Date = Date()): Date {
        val cal = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        return cal.time
    }

    /**
     * Obtiene la fecha de fin del mes para una fecha dada.
     * @param date Fecha de referencia (por defecto hoy)
     * @return Fecha del último día del mes a las 23:59:59.999
     */
    fun getEndOfMonth(date: Date = Date()): Date {
        val cal = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            setToEndOfDay()
        }
        return cal.time
    }
}

