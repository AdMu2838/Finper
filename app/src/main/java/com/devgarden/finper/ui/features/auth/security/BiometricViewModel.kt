package com.devgarden.finper.ui.features.auth.security

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.devgarden.finper.utils.Constants

/**
 * ViewModel para gestionar la configuración de autenticación biométrica.
 */
class BiometricViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var biometricEnabled by mutableStateOf(false)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var biometricStatus by mutableStateOf(BiometricStatus.UNKNOWN_ERROR)
        private set

    /**
     * Inicializa el estado verificando si el usuario tiene habilitada la autenticación biométrica.
     */
    fun initialize(context: Context) {
        val biometricManager = BiometricAuthManager(context)
        biometricStatus = biometricManager.getBiometricStatus()
        loadBiometricPreference()
    }

    /**
     * Carga la preferencia de autenticación biométrica desde Firestore.
     */
    private fun loadBiometricPreference() {
        val user = auth.currentUser
        if (user == null) {
            biometricEnabled = false
            return
        }

        loading = true
        error = null

        db.collection(Constants.Firestore.COLLECTION_USERS)
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                biometricEnabled = document.getBoolean("biometricEnabled") ?: false
                loading = false
            }
            .addOnFailureListener { ex ->
                error = ex.localizedMessage
                loading = false
                biometricEnabled = false
            }
    }

    /**
     * Habilita o deshabilita la autenticación biométrica.
     *
     * @param enabled True para habilitar, False para deshabilitar
     * @param onComplete Callback con el resultado
     */
    fun setBiometricEnabled(enabled: Boolean, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        loading = true
        error = null

        db.collection(Constants.Firestore.COLLECTION_USERS)
            .document(user.uid)
            .update("biometricEnabled", enabled)
            .addOnSuccessListener {
                biometricEnabled = enabled
                loading = false
                onComplete(true, null)
            }
            .addOnFailureListener { ex ->
                error = ex.localizedMessage
                loading = false
                onComplete(false, ex.localizedMessage)
            }
    }

    /**
     * Verifica si la autenticación biométrica está disponible en el dispositivo.
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricAuthManager(context)
        return biometricManager.hasBiometricEnrolled()
    }

    /**
     * Obtiene un mensaje descriptivo del estado biométrico.
     */
    fun getBiometricStatusMessage(): String {
        return when (biometricStatus) {
            BiometricStatus.READY -> "Autenticación biométrica disponible"
            BiometricStatus.NOT_AVAILABLE -> "Este dispositivo no tiene sensor de huella digital"
            BiometricStatus.TEMPORARILY_UNAVAILABLE -> "Sensor de huella temporalmente no disponible"
            BiometricStatus.NOT_ENROLLED -> "No hay huellas digitales registradas en el dispositivo"
            BiometricStatus.SECURITY_UPDATE_REQUIRED -> "Actualización de seguridad requerida"
            BiometricStatus.UNKNOWN_ERROR -> "Error desconocido al verificar biometría"
        }
    }
}

