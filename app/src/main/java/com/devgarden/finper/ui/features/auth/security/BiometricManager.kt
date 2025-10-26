package com.devgarden.finper.ui.features.auth.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Gestor de autenticación biométrica (huella digital).
 * Maneja la verificación de disponibilidad y el proceso de autenticación.
 */
class BiometricAuthManager(private val context: Context) {

    /**
     * Verifica si el dispositivo soporta autenticación biométrica.
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }
    }

    /**
     * Verifica si hay credenciales biométricas registradas.
     */
    fun hasBiometricEnrolled(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Obtiene el estado detallado de la autenticación biométrica.
     */
    fun getBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.TEMPORARILY_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SECURITY_UPDATE_REQUIRED
            else -> BiometricStatus.UNKNOWN_ERROR
        }
    }

    /**
     * Muestra el prompt de autenticación biométrica.
     *
     * @param activity Actividad desde donde se lanza la autenticación
     * @param title Título del diálogo
     * @param subtitle Subtítulo del diálogo
     * @param description Descripción del diálogo
     * @param negativeButtonText Texto del botón negativo
     * @param onSuccess Callback cuando la autenticación es exitosa
     * @param onError Callback cuando hay un error
     * @param onFailed Callback cuando la autenticación falla
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Autenticación Biométrica",
        subtitle: String = "Verifica tu identidad",
        description: String = "Usa tu huella digital para continuar",
        negativeButtonText: String = "Cancelar",
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

/**
 * Estados posibles de la autenticación biométrica.
 */
enum class BiometricStatus {
    READY,
    NOT_AVAILABLE,
    TEMPORARILY_UNAVAILABLE,
    NOT_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNKNOWN_ERROR
}
