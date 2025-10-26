package com.devgarden.finper.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * ViewModel encargado de cargar la lista de categorías desde Firestore.
 * Sigue el estilo de `UserViewModel` (estados expuestos via mutableStateOf) y
 * mantiene un snapshot listener para recibir cambios en tiempo real.
 */
class CategoriesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var categories by mutableStateOf<List<String>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private var listener: ListenerRegistration? = null

    init {
        attachListener()
    }

    private fun attachListener() {
        loading = true
        error = null

        // Listener en tiempo real para la colección 'categories'
        listener?.remove()
        listener = db.collection("categories")
            .addSnapshotListener { snapshot, ex ->
                if (ex != null) {
                    error = ex.localizedMessage
                    loading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    categories = snapshot.documents.mapNotNull { it.getString("name") }
                } else {
                    categories = emptyList()
                }

                loading = false
            }
    }

    /**
     * Recarga manualmente las categorías (usa una petición única).
     */
    fun loadCategoriesOnce() {
        loading = true
        error = null

        db.collection("categories")
            .get()
            .addOnSuccessListener { snapshot ->
                categories = snapshot.documents.mapNotNull { it.getString("name") }
                loading = false
            }
            .addOnFailureListener { ex ->
                error = ex.localizedMessage
                loading = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        listener = null
    }
}
