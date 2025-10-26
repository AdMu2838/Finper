package com.devgarden.finper.utils

/**
 * Constantes globales de la aplicación.
 * Centraliza todos los valores constantes para facilitar el mantenimiento.
 */
object Constants {

    // --- Colecciones de Firestore ---
    object Firestore {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_TRANSACTIONS = "transactions"
        const val COLLECTION_CATEGORIES = "categories"
        const val COLLECTION_FIXED_INCOMES = "fixedIncomes"

        // Campos comunes
        const val FIELD_BALANCE = "balance"
        const val FIELD_EMAIL = "email"
        const val FIELD_FULL_NAME = "fullName"
        const val FIELD_PHONE = "phone"
        const val FIELD_AMOUNT = "amount"
        const val FIELD_CATEGORY = "category"
        const val FIELD_DATE = "date"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_IS_EXPENSE = "isExpense"
        const val FIELD_NAME = "name"
    }

    // --- Valores por defecto ---
    object Defaults {
        const val DEFAULT_USER_NAME = "Usuario"
        const val DEFAULT_BALANCE = 0.0
        const val DEFAULT_CATEGORY = "Otros"
        const val EMPTY_BALANCE_TEXT = "S/.0.00"
    }

    // --- Mensajes de Error ---
    object ErrorMessages {
        const val ERROR_NULL_USER = "El usuario es nulo después de la operación"
        const val ERROR_USER_NOT_AUTHENTICATED = "Usuario no autenticado"
        const val ERROR_INVALID_AMOUNT = "El monto debe ser mayor que 0"
        const val ERROR_INVALID_CREDENTIALS = "Contraseña incorrecta"
        const val ERROR_USER_NOT_FOUND = "No existe una cuenta con ese correo"
        const val ERROR_EMAIL_ALREADY_EXISTS = "Ya existe una cuenta con ese correo"
        const val ERROR_WEAK_PASSWORD = "La contraseña es muy débil"
        const val ERROR_UNKNOWN = "Error desconocido"
        const val ERROR_NETWORK = "Error de conexión"
        const val ERROR_LOADING_DATA = "Error al cargar los datos"
    }

    // --- Preferencias de Usuario ---
    object Preferences {
        const val PREFS_NAME = "finper_prefs"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LANGUAGE = "language"
    }

    // --- Navegación ---
    object Navigation {
        const val BOTTOM_NAV_HOME = 0
        const val BOTTOM_NAV_ANALYSIS = 1
        const val BOTTOM_NAV_TRANSACTIONS = 2
        const val BOTTOM_NAV_CATEGORIES = 3
        const val BOTTOM_NAV_PROFILE = 4
    }

    // --- Iconos por categoría (mapeo de nombres a iconos) ---
    val CATEGORY_ICONS = mapOf(
        "Comida" to "restaurant",
        "Transporte" to "directions_bus",
        "Medicina" to "local_hospital",
        "Comestibles" to "shopping_basket",
        "Alquiler" to "home",
        "Regalos" to "card_giftcard",
        "Ahorros" to "savings",
        "Entretenimiento" to "movie",
        "Otros" to "add"
    )

    // --- Límites y validaciones ---
    object Limits {
        const val MAX_TRANSACTION_AMOUNT = 1_000_000.0
        const val MIN_TRANSACTION_AMOUNT = 0.01
        const val MAX_DESCRIPTION_LENGTH = 200
        const val MAX_CATEGORY_NAME_LENGTH = 50
    }

    // --- Tags para Logging ---
    object LogTags {
        const val TAG_AUTH = "AuthRepository"
        const val TAG_USER_VM = "UserViewModel"
        const val TAG_TRANSACTIONS_VM = "TransactionsViewModel"
        const val TAG_CATEGORIES_VM = "CategoriesViewModel"
        const val TAG_LOGIN = "LoginViewModel"
        const val TAG_REGISTER = "RegisterViewModel"
    }
}

