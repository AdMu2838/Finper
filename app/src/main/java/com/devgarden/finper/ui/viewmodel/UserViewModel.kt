package com.devgarden.finper.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.devgarden.finper.data.UsuarioActual
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var usuario by mutableStateOf<UsuarioActual?>(null)
        private set

    // balance observable (por defecto 0.0)
    var balance by mutableStateOf(0.0)
        private set

    private var userListener: ListenerRegistration? = null

    init {
        cargarUsuarioActual()
    }

    fun cargarUsuarioActual() {
        val user = auth.currentUser
        if (user != null) {
            // Cancel previous listener if any
            userListener?.remove()

            // Escuchar cambios en el documento del usuario para mantener balance en tiempo real
            val docRef = db.collection("users").document(user.uid)
            userListener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // En caso de error, fallback a datos de auth
                    usuario = UsuarioActual(
                        uid = user.uid,
                        nombre = user.displayName ?: "Usuario",
                        correo = user.email ?: "",
                        telefono = ""
                    )
                    balance = balance.takeIf { it >= 0.0 } ?: 0.0
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val nombre = snapshot.getString("fullName") ?: user.displayName ?: "Usuario"
                    val correo = snapshot.getString("email") ?: user.email ?: ""
                    val telefono = snapshot.getString("phone") ?: ""
                    val balanceField = try {
                        val raw = snapshot.get("balance")
                        if (raw is Number) raw.toDouble() else 0.0
                    } catch (_: Exception) { 0.0 }

                    usuario = UsuarioActual(
                        uid = user.uid,
                        nombre = nombre,
                        correo = correo,
                        telefono = telefono
                    )
                    balance = balanceField
                } else {
                    // Si no está en Firestore, usar lo de FirebaseAuth
                    usuario = UsuarioActual(
                        uid = user.uid,
                        nombre = user.displayName ?: "Usuario",
                        correo = user.email ?: "",
                        telefono = ""
                    )
                    balance = 0.0
                }
            }
        } else {
            usuario = null
            balance = 0.0
            userListener?.remove()
            userListener = null
        }
    }

    fun actualizarPerfil(nombre: String, telefono: String, correo: String, callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "Usuario no autenticado")
            return
        }

        val data = hashMapOf<String, Any>(
            "fullName" to nombre,
            "email" to correo,
            "phone" to telefono
        )

        db.collection("users").document(user.uid)
            .set(data)
            .addOnSuccessListener {
                // Actualizar estado local
                usuario = UsuarioActual(uid = user.uid, nombre = nombre, correo = correo, telefono = telefono)
                callback(true, null)
            }
            .addOnFailureListener { ex ->
                callback(false, ex.localizedMessage)
            }
    }

    fun cerrarSesion() {
        auth.signOut()
        usuario = null
        balance = 0.0
        userListener?.remove()
        userListener = null
    }
}
