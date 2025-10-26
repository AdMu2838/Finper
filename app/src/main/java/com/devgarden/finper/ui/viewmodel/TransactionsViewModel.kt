package com.devgarden.finper.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

/**
 * ViewModel que expone la lista de transacciones del usuario (subcolección 'transactions').
 */
class TransactionsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    data class TransactionDto(
        val id: String,
        val amount: Double,
        val category: String,
        val date: Date?,
        val description: String,
        val isExpense: Boolean
    )

    var transactions by mutableStateOf<List<TransactionDto>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private var listener: ListenerRegistration? = null

    init {
        attachListenerIfUser()
    }

    private fun attachListenerIfUser() {
        val user = auth.currentUser
        if (user == null) {
            transactions = emptyList()
            loading = false
            error = "Usuario no autenticado"
            return
        }

        loading = true
        error = null

        listener?.remove()
        listener = db.collection("users")
            .document(user.uid)
            .collection("transactions")
            .addSnapshotListener { snapshot, ex ->
                if (ex != null) {
                    error = ex.localizedMessage
                    loading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    transactions = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            // amount puede venir como Number
                            val rawAmount = doc.get("amount")
                            val amount = when (rawAmount) {
                                is Number -> rawAmount.toDouble()
                                is String -> rawAmount.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val category = doc.getString("category") ?: ""
                            val timestamp = doc.get("date")
                            val date: Date? = when (timestamp) {
                                is Timestamp -> timestamp.toDate()
                                is Date -> timestamp
                                else -> null
                            }
                            val description = doc.getString("description") ?: ""
                            val isExpense = doc.getBoolean("isExpense") ?: false

                            TransactionDto(id = id, amount = amount, category = category, date = date, description = description, isExpense = isExpense)
                        } catch (_: Exception) {
                            null
                        }
                    }
                } else {
                    transactions = emptyList()
                }

                loading = false
            }
    }

    @Suppress("unused")
    fun reload() {
        attachListenerIfUser()
    }

    /**
     * Añade una transacción en Firestore bajo users/{uid}/transactions.
     * date: si es null, se guardará el timestamp actual.
     */
    @Suppress("unused")
    fun addTransaction(amount: Double, category: String, date: Date?, description: String, isExpense: Boolean, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        val data = hashMapOf<String, Any>(
            "amount" to amount,
            "category" to category,
            "date" to (date?.let { Timestamp(it) } ?: Timestamp.now()),
            "description" to description,
            "isExpense" to isExpense
        )

        db.collection("users").document(user.uid).collection("transactions")
            .add(data)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { ex ->
                onComplete(false, ex.localizedMessage)
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        listener = null
    }
}
