package com.devgarden.finper.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.devgarden.finper.data.UsuarioActual
import com.devgarden.finper.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * ViewModel que gestiona el estado del usuario actual.
 * Mantiene sincronizado el perfil del usuario y su balance en tiempo real con Firebase.
 */
class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Usuario actualmente autenticado.
     */
    var usuario by mutableStateOf<UsuarioActual?>(null)
        private set

    /**
     * Balance actual del usuario (sincronizado en tiempo real).
     */
    var balance by mutableStateOf(Constants.Defaults.DEFAULT_BALANCE)
        private set

    /**
     * Estado de carga del perfil.
     */
    var loading by mutableStateOf(false)
        private set

    /**
     * Error al cargar el perfil.
     */
    var error by mutableStateOf<String?>(null)
        private set

    private var userListener: ListenerRegistration? = null

    init {
        cargarUsuarioActual()
    }

    /**
     * Carga el usuario actual desde Firebase Auth y Firestore.
     * Establece un listener en tiempo real para mantener el balance actualizado.
     */
    fun cargarUsuarioActual() {
        val user = auth.currentUser
        if (user == null) {
            clearUserData()
            return
        }

        loading = true
        error = null
        userListener?.remove()

        val docRef = db.collection(Constants.Firestore.COLLECTION_USERS).document(user.uid)
        userListener = docRef.addSnapshotListener { snapshot, firebaseError ->
            if (firebaseError != null) {
                error = firebaseError.localizedMessage
                loadFallbackUserData(user)
                loading = false
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                loadUserDataFromSnapshot(snapshot, user)
            } else {
                loadFallbackUserData(user)
            }

            loading = false
        }
    }

    /**
     * Actualiza el perfil del usuario en Firestore.
     *
     * @param nombre Nombre completo del usuario
     * @param telefono Número de teléfono
     * @param correo Correo electrónico
     * @param callback Callback con resultado (éxito, mensaje de error)
     */
    fun actualizarPerfil(
        nombre: String,
        telefono: String,
        correo: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, Constants.ErrorMessages.ERROR_USER_NOT_AUTHENTICATED)
            return
        }

        val data = buildProfileUpdateData(nombre, correo, telefono)

        db.collection(Constants.Firestore.COLLECTION_USERS)
            .document(user.uid)
            .set(data)
            .addOnSuccessListener {
                updateLocalUserData(user.uid, nombre, correo, telefono)
                callback(true, null)
            }
            .addOnFailureListener { ex ->
                callback(false, ex.localizedMessage)
            }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun cerrarSesion() {
        auth.signOut()
        clearUserData()
    }

    // --- Métodos helper privados ---

    /**
     * Carga los datos del usuario desde un snapshot de Firestore.
     */
    private fun loadUserDataFromSnapshot(
        snapshot: com.google.firebase.firestore.DocumentSnapshot,
        user: com.google.firebase.auth.FirebaseUser
    ) {
        val nombre = snapshot.getString(Constants.Firestore.FIELD_FULL_NAME)
            ?: user.displayName
            ?: Constants.Defaults.DEFAULT_USER_NAME

        val correo = snapshot.getString(Constants.Firestore.FIELD_EMAIL)
            ?: user.email
            ?: ""

        val telefono = snapshot.getString(Constants.Firestore.FIELD_PHONE) ?: ""

        val balanceField = extractBalanceFromSnapshot(snapshot)

        usuario = UsuarioActual(
            uid = user.uid,
            nombre = nombre,
            correo = correo,
            telefono = telefono
        )
        balance = balanceField
    }

    /**
     * Carga datos de fallback desde Firebase Auth cuando Firestore falla.
     */
    private fun loadFallbackUserData(user: com.google.firebase.auth.FirebaseUser) {
        usuario = UsuarioActual(
            uid = user.uid,
            nombre = user.displayName ?: Constants.Defaults.DEFAULT_USER_NAME,
            correo = user.email ?: "",
            telefono = ""
        )
        balance = balance.takeIf { it >= 0.0 } ?: Constants.Defaults.DEFAULT_BALANCE
    }

    /**
     * Extrae el balance del snapshot de Firestore de forma segura.
     */
    private fun extractBalanceFromSnapshot(
        snapshot: com.google.firebase.firestore.DocumentSnapshot
    ): Double {
        return try {
            val raw = snapshot.get(Constants.Firestore.FIELD_BALANCE)
            when (raw) {
                is Number -> raw.toDouble()
                is String -> raw.toDoubleOrNull() ?: Constants.Defaults.DEFAULT_BALANCE
                else -> Constants.Defaults.DEFAULT_BALANCE
            }
        } catch (_: Exception) {
            Constants.Defaults.DEFAULT_BALANCE
        }
    }

    /**
     * Construye el mapa de datos para actualizar el perfil.
     */
    private fun buildProfileUpdateData(
        nombre: String,
        correo: String,
        telefono: String
    ): HashMap<String, Any> {
        return hashMapOf(
            Constants.Firestore.FIELD_FULL_NAME to nombre,
            Constants.Firestore.FIELD_EMAIL to correo,
            Constants.Firestore.FIELD_PHONE to telefono
        )
    }

    /**
     * Actualiza los datos del usuario en el estado local.
     */
    private fun updateLocalUserData(
        uid: String,
        nombre: String,
        correo: String,
        telefono: String
    ) {
        usuario = UsuarioActual(
            uid = uid,
            nombre = nombre,
            correo = correo,
            telefono = telefono
        )
    }

    /**
     * Limpia todos los datos del usuario.
     */
    private fun clearUserData() {
        usuario = null
        balance = Constants.Defaults.DEFAULT_BALANCE
        error = null
        loading = false
        userListener?.remove()
        userListener = null
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        userListener = null
    }
}
