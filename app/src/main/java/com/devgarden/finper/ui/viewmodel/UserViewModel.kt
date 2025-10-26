package com.devgarden.finper.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.devgarden.finper.data.UsuarioActual
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var usuario by mutableStateOf<UsuarioActual?>(null)
        private set

    init {
        cargarUsuarioActual()
    }

    fun cargarUsuarioActual() {
        val user = auth.currentUser
        if (user != null) {
            // Leer la colección 'users' (la que usa AuthRepository) y mapear campos
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("fullName") ?: user.displayName ?: "Usuario"
                        val correo = document.getString("email") ?: user.email ?: ""
                        val telefono = document.getString("phone") ?: ""
                        usuario = UsuarioActual(
                            uid = user.uid,
                            nombre = nombre,
                            correo = correo,
                            telefono = telefono
                        )
                    } else {
                        // Si no está en Firestore, usar lo de FirebaseAuth
                        usuario = UsuarioActual(
                            uid = user.uid,
                            nombre = user.displayName ?: "Usuario",
                            correo = user.email ?: "",
                            telefono = ""
                        )
                    }
                }
                .addOnFailureListener {
                    // En caso de error al leer Firestore, fallback a datos de auth
                    usuario = UsuarioActual(
                        uid = user.uid,
                        nombre = user.displayName ?: "Usuario",
                        correo = user.email ?: "",
                        telefono = ""
                    )
                }
        } else {
            usuario = null
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
    }
}
