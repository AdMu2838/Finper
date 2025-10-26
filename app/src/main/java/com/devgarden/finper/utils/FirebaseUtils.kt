package com.devgarden.finper.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

/**
 * Utilidades para trabajar con Firebase Firestore.
 * Centraliza operaciones comunes y extracción segura de datos.
 */
object FirebaseUtils {

    /**
     * Extrae un valor Double de forma segura desde un DocumentSnapshot.
     *
     * @param snapshot Snapshot del documento
     * @param fieldName Nombre del campo
     * @param defaultValue Valor por defecto si falla la extracción
     * @return Valor Double extraído o el valor por defecto
     */
    fun extractDouble(
        snapshot: DocumentSnapshot,
        fieldName: String,
        defaultValue: Double = 0.0
    ): Double {
        return try {
            when (val raw = snapshot.get(fieldName)) {
                is Number -> raw.toDouble()
                is String -> raw.toDoubleOrNull() ?: defaultValue
                else -> defaultValue
            }
        } catch (_: Exception) {
            defaultValue
        }
    }

    /**
     * Extrae un valor String de forma segura desde un DocumentSnapshot.
     *
     * @param snapshot Snapshot del documento
     * @param fieldName Nombre del campo
     * @param defaultValue Valor por defecto si falla la extracción
     * @return Valor String extraído o el valor por defecto
     */
    fun extractString(
        snapshot: DocumentSnapshot,
        fieldName: String,
        defaultValue: String = ""
    ): String {
        return snapshot.getString(fieldName) ?: defaultValue
    }

    /**
     * Extrae un valor Boolean de forma segura desde un DocumentSnapshot.
     *
     * @param snapshot Snapshot del documento
     * @param fieldName Nombre del campo
     * @param defaultValue Valor por defecto si falla la extracción
     * @return Valor Boolean extraído o el valor por defecto
     */
    fun extractBoolean(
        snapshot: DocumentSnapshot,
        fieldName: String,
        defaultValue: Boolean = false
    ): Boolean {
        return snapshot.getBoolean(fieldName) ?: defaultValue
    }

    /**
     * Extrae una fecha de forma segura desde un DocumentSnapshot.
     *
     * @param snapshot Snapshot del documento
     * @param fieldName Nombre del campo
     * @return Fecha extraída o null si no existe
     */
    fun extractDate(
        snapshot: DocumentSnapshot,
        fieldName: String
    ): Date? {
        return try {
            when (val raw = snapshot.get(fieldName)) {
                is Timestamp -> raw.toDate()
                is Date -> raw
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parsea una fecha en formato dd/MM/yyyy a Date.
     * La fecha se configura a las 12:00 UTC para evitar problemas de zona horaria.
     *
     * @param dateStr String en formato dd/MM/yyyy
     * @return Date parseada o null si el formato es inválido
     */
    fun parseDateDDMMYYYY(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null

        return try {
            val parts = dateStr.trim().split('/').map { it.toInt() }
            if (parts.size != 3) return null

            val day = parts[0]
            val month = parts[1] - 1 // Calendar months are 0-based
            val year = parts[2]

            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Construye un Timestamp de Firebase desde una Date.
     *
     * @param date Fecha a convertir
     * @return Timestamp de Firebase o Timestamp actual si date es null
     */
    fun toFirebaseTimestamp(date: Date?): Timestamp {
        return date?.let { Timestamp(it) } ?: Timestamp.now()
    }

    /**
     * Valida que un monto sea válido para transacciones.
     *
     * @param amount Monto a validar
     * @return true si el monto es válido (mayor que 0 y menor que el máximo)
     */
    fun isValidTransactionAmount(amount: Double): Boolean {
        return amount > Constants.Limits.MIN_TRANSACTION_AMOUNT &&
               amount <= Constants.Limits.MAX_TRANSACTION_AMOUNT
    }

    /**
     * Construye un mapa de datos para crear un perfil de usuario.
     *
     * @param uid ID del usuario
     * @param fullName Nombre completo
     * @param email Correo electrónico
     * @param phone Teléfono
     * @param birthDate Fecha de nacimiento
     * @param balance Balance inicial
     * @return Mapa con los datos del perfil
     */
    fun buildUserProfileData(
        uid: String,
        fullName: String,
        email: String,
        phone: String = "",
        birthDate: Date? = null,
        balance: Double = Constants.Defaults.DEFAULT_BALANCE
    ): HashMap<String, Any?> {
        return hashMapOf(
            "uid" to uid,
            Constants.Firestore.FIELD_FULL_NAME to fullName,
            Constants.Firestore.FIELD_EMAIL to email,
            Constants.Firestore.FIELD_PHONE to phone,
            "birthDate" to (birthDate?.let { Timestamp(it) }),
            Constants.Firestore.FIELD_BALANCE to balance
        )
    }

    /**
     * Construye un mapa de datos para crear una transacción.
     *
     * @param amount Monto de la transacción
     * @param category Categoría
     * @param date Fecha de la transacción
     * @param description Descripción
     * @param isExpense Si es un gasto (true) o ingreso (false)
     * @return Mapa con los datos de la transacción
     */
    fun buildTransactionData(
        amount: Double,
        category: String,
        date: Date?,
        description: String,
        isExpense: Boolean
    ): HashMap<String, Any> {
        return hashMapOf(
            Constants.Firestore.FIELD_AMOUNT to amount,
            Constants.Firestore.FIELD_CATEGORY to category,
            Constants.Firestore.FIELD_DATE to toFirebaseTimestamp(date),
            Constants.Firestore.FIELD_DESCRIPTION to description,
            Constants.Firestore.FIELD_IS_EXPENSE to isExpense
        )
    }
}

