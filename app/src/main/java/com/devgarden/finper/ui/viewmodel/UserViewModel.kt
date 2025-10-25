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
                        usuario = UsuarioActual(
                            uid = user.uid,
                            fullName = nombre,
                            email = correo
                        )
                    } else {
                        // Si no está en Firestore, usar lo de FirebaseAuth
                        usuario = UsuarioActual(
                            uid = user.uid,
                            fullName = user.displayName ?: "Usuario",
                            email = user.email ?: ""
                        )
                    }
                }
                .addOnFailureListener {
                    // En caso de error al leer Firestore, fallback a datos de auth
                    usuario = UsuarioActual(
                        uid = user.uid,
                        fullName = user.displayName ?: "Usuario",
                        email = user.email ?: ""
                    )
                }
        } else {
            usuario = null
        }
    }

    fun cerrarSesion() {
        auth.signOut()
        usuario = null
    }
}
