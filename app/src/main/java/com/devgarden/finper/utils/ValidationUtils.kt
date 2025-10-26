package com.devgarden.finper.utils

import android.util.Patterns

/**
 * Utilidades de validación centralizadas para formularios y entrada de datos.
 * Proporciona métodos reutilizables para validar diferentes tipos de datos.
 */
object ValidationUtils {

    // --- Constantes de Validación ---
    private const val MIN_PASSWORD_LENGTH = 6
    private const val MIN_NAME_LENGTH = 2
    private const val MIN_PHONE_LENGTH = 9
    private const val MAX_PHONE_LENGTH = 15

    // --- Mensajes de Error Estandarizados ---
    const val ERROR_EMPTY_FIELD = "Este campo es requerido"
    const val ERROR_INVALID_EMAIL = "El formato del email no es válido"
    const val ERROR_PASSWORD_TOO_SHORT = "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
    const val ERROR_PASSWORDS_MISMATCH = "Las contraseñas no coinciden"
    const val ERROR_INVALID_PHONE = "El número de teléfono no es válido"
    const val ERROR_INVALID_AMOUNT = "El monto debe ser mayor que 0"
    const val ERROR_NAME_TOO_SHORT = "El nombre debe tener al menos $MIN_NAME_LENGTH caracteres"

    /**
     * Valida que un campo no esté vacío.
     * @param value Valor a validar
     * @return true si es válido, false si está vacío
     */
    fun isNotEmpty(value: String): Boolean = value.isNotBlank()

    /**
     * Valida el formato de un email.
     * @param email Email a validar
     * @return true si el formato es válido, false en caso contrario
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Valida la longitud mínima de una contraseña.
     * @param password Contraseña a validar
     * @param minLength Longitud mínima requerida
     * @return true si cumple con la longitud mínima
     */
    fun isValidPassword(password: String, minLength: Int = MIN_PASSWORD_LENGTH): Boolean {
        return password.length >= minLength
    }

    /**
     * Valida que dos contraseñas coincidan.
     * @param password Contraseña original
     * @param confirmation Contraseña de confirmación
     * @return true si coinciden, false en caso contrario
     */
    fun passwordsMatch(password: String, confirmation: String): Boolean {
        return password == confirmation && password.isNotBlank()
    }

    /**
     * Valida el formato de un número de teléfono.
     * @param phone Número de teléfono a validar
     * @return true si el formato es válido
     */
    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        return cleaned.length in MIN_PHONE_LENGTH..MAX_PHONE_LENGTH
    }

    /**
     * Valida que un monto sea mayor que cero.
     * @param amount Monto a validar
     * @return true si es mayor que 0
     */
    fun isValidAmount(amount: Double): Boolean = amount > 0.0

    /**
     * Valida que un monto en formato String sea válido.
     * @param amountString String que representa el monto
     * @return true si es un número válido mayor que 0
     */
    fun isValidAmountString(amountString: String): Boolean {
        val amount = amountString.toDoubleOrNull() ?: return false
        return isValidAmount(amount)
    }

    /**
     * Valida la longitud mínima de un nombre.
     * @param name Nombre a validar
     * @param minLength Longitud mínima requerida
     * @return true si cumple con la longitud mínima
     */
    fun isValidName(name: String, minLength: Int = MIN_NAME_LENGTH): Boolean {
        return name.isNotBlank() && name.length >= minLength
    }

    /**
     * Obtiene mensaje de error para un campo de email.
     * @param email Email a validar
     * @return Mensaje de error o null si es válido
     */
    fun getEmailError(email: String): String? = when {
        email.isBlank() -> ERROR_EMPTY_FIELD
        !isValidEmail(email) -> ERROR_INVALID_EMAIL
        else -> null
    }

    /**
     * Obtiene mensaje de error para un campo de contraseña.
     * @param password Contraseña a validar
     * @return Mensaje de error o null si es válida
     */
    fun getPasswordError(password: String): String? = when {
        password.isBlank() -> ERROR_EMPTY_FIELD
        !isValidPassword(password) -> ERROR_PASSWORD_TOO_SHORT
        else -> null
    }

    /**
     * Obtiene mensaje de error para confirmación de contraseña.
     * @param password Contraseña original
     * @param confirmation Contraseña de confirmación
     * @return Mensaje de error o null si coinciden
     */
    fun getPasswordConfirmationError(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> ERROR_EMPTY_FIELD
        !passwordsMatch(password, confirmation) -> ERROR_PASSWORDS_MISMATCH
        else -> null
    }
}

