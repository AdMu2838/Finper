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
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val datos = document.toObject(UsuarioActual::class.java)
                    if (datos != null) {
                        usuario = datos
                    } else {
                        // Si no está en Firestore, usar lo de FirebaseAuth
                        usuario = UsuarioActual(
                            uid = user.uid,
                            fullName = user.displayName ?: "Usuario",
                            email = user.email ?: ""
                        )
                    }
                }
        }
    }

    fun cerrarSesion() {
        auth.signOut()
        usuario = null
    }
}

