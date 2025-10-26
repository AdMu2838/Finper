package com.devgarden.finper.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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
    // Listener separado para el cálculo/escucha de gastos del mes
    private var monthlyListener: ListenerRegistration? = null

    // Estado específico para gastos del mes
    var monthlyExpenses by mutableStateOf(0.0)
        private set

    var monthlyLoading by mutableStateOf(false)
        private set

    var monthlyError by mutableStateOf<String?>(null)
        private set

    // --- Estados para ingresos del mes ---
    var monthlyIncomes by mutableStateOf(0.0)
        private set

    var monthlyIncomesLoading by mutableStateOf(false)
        private set

    var monthlyIncomesError by mutableStateOf<String?>(null)
        private set

    private var monthlyIncomesListener: ListenerRegistration? = null

    // --- Estados y métodos para consulta por categoría (server-side ordering) ---
    var categoryTransactions by mutableStateOf<List<TransactionDto>>(emptyList())
        private set

    var categoryLoading by mutableStateOf(false)
        private set

    var categoryError by mutableStateOf<String?>(null)
        private set

    // Si Firestore devuelve el error de índice, la URL suele estar en el mensaje; la guardamos para que la UI la muestre
    var categoryIndexUrl by mutableStateOf<String?>(null)
        private set

    /**
     * Carga (una vez) las transacciones que tienen exactamente la categoría dada.
     * Intenta ordenar en el servidor por `date` DESC; si Firestore exige un índice, captura la URL y la expone en `categoryIndexUrl`.
     */
    fun loadTransactionsByCategory(category: String) {
        val user = auth.currentUser
        if (user == null) {
            categoryTransactions = emptyList()
            categoryLoading = false
            categoryError = "Usuario no autenticado"
            categoryIndexUrl = null
            return
        }

        categoryLoading = true
        categoryError = null
        categoryIndexUrl = null

        try {
            db.collection("users").document(user.uid).collection("transactions")
                .whereEqualTo("category", category)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val rawAmount = doc.get("amount")
                            val amount = when (rawAmount) {
                                is Number -> rawAmount.toDouble()
                                is String -> rawAmount.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val categoryField = doc.getString("category") ?: ""
                            val timestamp = doc.get("date")
                            val date: Date? = when (timestamp) {
                                is Timestamp -> timestamp.toDate()
                                is Date -> timestamp
                                else -> null
                            }
                            val description = doc.getString("description") ?: ""
                            val isExpense = doc.getBoolean("isExpense") ?: false

                            TransactionDto(id = id, amount = amount, category = categoryField, date = date, description = description, isExpense = isExpense)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    categoryTransactions = list
                    categoryLoading = false
                }
                .addOnFailureListener { ex ->
                    categoryLoading = false
                    categoryError = ex.localizedMessage
                    // intentar extraer URL de índice si existe
                    val msg = ex.localizedMessage ?: ""
                    val regex = """https?://console.firebase.google.com[^"]+""".toRegex()
                    val match = regex.find(msg)
                    categoryIndexUrl = match?.value

                    // Si la falla parece ser por índice, hacemos un fallback: consultar sin orderBy y ordenar localmente
                    val idxRequired = msg.contains("requires an index") || categoryIndexUrl != null
                    if (idxRequired) {
                        // consultar sin orderBy
                        db.collection("users").document(user.uid).collection("transactions")
                            .whereEqualTo("category", category)
                            .get()
                            .addOnSuccessListener { snap2 ->
                                val fallback = snap2.documents.mapNotNull { doc ->
                                    try {
                                        val id = doc.id
                                        val rawAmount = doc.get("amount")
                                        val amount = when (rawAmount) {
                                            is Number -> rawAmount.toDouble()
                                            is String -> rawAmount.toDoubleOrNull() ?: 0.0
                                            else -> 0.0
                                        }
                                        val categoryField = doc.getString("category") ?: ""
                                        val timestamp = doc.get("date")
                                        val date: Date? = when (timestamp) {
                                            is Timestamp -> timestamp.toDate()
                                            is Date -> timestamp
                                            else -> null
                                        }
                                        val description = doc.getString("description") ?: ""
                                        val isExpense = doc.getBoolean("isExpense") ?: false
                                        TransactionDto(id = id, amount = amount, category = categoryField, date = date, description = description, isExpense = isExpense)
                                    } catch (_: Exception) {
                                        null
                                    }
                                }.sortedWith(compareByDescending<TransactionDto> { it.date ?: Date(0) })

                                categoryTransactions = fallback
                                // no cambiar categoryIndexUrl ni categoryError (mostramos link), categoryLoading ya false
                            }
                            .addOnFailureListener {
                                // ignorar: ya tenemos categoryError
                            }
                    }
                }
        } catch (ex: Exception) {
            categoryLoading = false
            categoryError = ex.localizedMessage
            categoryIndexUrl = null
        }
    }

    fun clearCategoryQuery() {
        categoryTransactions = emptyList()
        categoryLoading = false
        categoryError = null
        categoryIndexUrl = null
    }

    init {
        // por defecto cargar todas las transacciones (sin rango)
        listenTransactionsRange(null, null)
        // también empezar a escuchar los gastos del mes actual
        listenCurrentMonthExpenses()
        // escuchar los ingresos del mes actual
        listenCurrentMonthIncomes()
    }

    private fun attachQueryListener(query: Query) {
        listener?.remove()
        loading = true
        error = null
        listener = query.addSnapshotListener { snapshot, ex ->
            if (ex != null) {
                error = ex.localizedMessage
                loading = false
                return@addSnapshotListener
            }

            if (snapshot != null) {
                transactions = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
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

    // --- Utilidades para inicio/fin de mes ---
    private fun startOfMonth(date: Date = Date()): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun endOfMonth(date: Date = Date()): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.time
    }

    /**
     * Escucha en tiempo real la suma de gastos (isExpense == true) desde el primer día hasta el último día del mes actual.
     */
    fun listenCurrentMonthExpenses() {
        val user = auth.currentUser
        if (user == null) {
            monthlyListener?.remove()
            monthlyListener = null
            monthlyExpenses = 0.0
            monthlyLoading = false
            monthlyError = "Usuario no autenticado"
            return
        }

        monthlyListener?.remove()
        monthlyLoading = true
        monthlyError = null

        try {
            val start = startOfMonth()
            val end = endOfMonth()

            var query: Query = db.collection("users").document(user.uid).collection("transactions")
                .whereGreaterThanOrEqualTo("date", Timestamp(start))
                .whereLessThanOrEqualTo("date", Timestamp(end))
                .whereEqualTo("isExpense", true)

            // no necesitamos ordenar para sumar, pero podemos hacerlo por fecha
            query = query.orderBy("date", Query.Direction.DESCENDING)

            monthlyListener = query.addSnapshotListener { snapshot, ex ->
                if (ex != null) {
                    monthlyError = ex.localizedMessage
                    monthlyLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var sum = 0.0
                    for (doc in snapshot.documents) {
                        try {
                            val rawAmount = doc.get("amount")
                            val amount = when (rawAmount) {
                                is Number -> rawAmount.toDouble()
                                is String -> rawAmount.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            sum += amount
                        } catch (_: Exception) {
                            // ignorar doc mal formado
                        }
                    }
                    monthlyExpenses = sum
                } else {
                    monthlyExpenses = 0.0
                }

                monthlyLoading = false
            }
        } catch (ex: Exception) {
            monthlyError = ex.localizedMessage
            monthlyLoading = false
        }
    }

    /**
     * Escucha en tiempo real la suma de ingresos (isExpense == false) desde el primer día hasta el último día del mes actual.
     */
    fun listenCurrentMonthIncomes() {
        val user = auth.currentUser
        if (user == null) {
            monthlyIncomesListener?.remove()
            monthlyIncomesListener = null
            monthlyIncomes = 0.0
            monthlyIncomesLoading = false
            monthlyIncomesError = "Usuario no autenticado"
            return
        }

        monthlyIncomesListener?.remove()
        monthlyIncomesLoading = true
        monthlyIncomesError = null

        try {
            val start = startOfMonth()
            val end = endOfMonth()

            var query: Query = db.collection("users").document(user.uid).collection("transactions")
                .whereGreaterThanOrEqualTo("date", Timestamp(start))
                .whereLessThanOrEqualTo("date", Timestamp(end))
                .whereEqualTo("isExpense", false)

            query = query.orderBy("date", Query.Direction.DESCENDING)

            monthlyIncomesListener = query.addSnapshotListener { snapshot, ex ->
                if (ex != null) {
                    monthlyIncomesError = ex.localizedMessage
                    monthlyIncomesLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var sum = 0.0
                    for (doc in snapshot.documents) {
                        try {
                            val rawAmount = doc.get("amount")
                            val amount = when (rawAmount) {
                                is Number -> rawAmount.toDouble()
                                is String -> rawAmount.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            sum += amount
                        } catch (_: Exception) {
                            // ignorar doc mal formado
                        }
                    }
                    monthlyIncomes = sum
                } else {
                    monthlyIncomes = 0.0
                }

                monthlyIncomesLoading = false
            }
        } catch (ex: Exception) {
            monthlyIncomesError = ex.localizedMessage
            monthlyIncomesLoading = false
        }
    }

    /**
     * Consulta puntual que calcula la suma de gastos del mes actual una sola vez y devuelve el resultado por callback.
     */
    @Suppress("unused")
    fun calculateCurrentMonthExpensesOnce(onComplete: (Double, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(0.0, "Usuario no autenticado")
            return
        }

        try {
            val start = startOfMonth()
            val end = endOfMonth()

            val query = db.collection("users").document(user.uid).collection("transactions")
                .whereGreaterThanOrEqualTo("date", Timestamp(start))
                .whereLessThanOrEqualTo("date", Timestamp(end))
                .whereEqualTo("isExpense", true)

            query.get()
                .addOnSuccessListener { snapshot ->
                    var sum = 0.0
                    for (doc in snapshot.documents) {
                        try {
                            val rawAmount = doc.get("amount")
                            val amount = when (rawAmount) {
                                is Number -> rawAmount.toDouble()
                                is String -> rawAmount.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            sum += amount
                        } catch (_: Exception) {
                        }
                    }
                    onComplete(sum, null)
                }
                .addOnFailureListener { ex ->
                    onComplete(0.0, ex.localizedMessage)
                }
        } catch (ex: Exception) {
            onComplete(0.0, ex.localizedMessage)
        }
    }

    /**
     * Suscribe a las transacciones en el rango [start, end] (ambos inclusive). Si start y end son null, escucha todas las transacciones.
     */
    fun listenTransactionsRange(start: Date?, end: Date?) {
        val user = auth.currentUser
        if (user == null) {
            listener?.remove()
            listener = null
            transactions = emptyList()
            loading = false
            error = "Usuario no autenticado"
            return
        }

        try {
            var query: Query = db.collection("users").document(user.uid).collection("transactions")
            if (start != null) {
                query = query.whereGreaterThanOrEqualTo("date", Timestamp(start))
            }
            if (end != null) {
                query = query.whereLessThanOrEqualTo("date", Timestamp(end))
            }
            // ordenar por fecha descendente
            query = query.orderBy("date", Query.Direction.DESCENDING)

            attachQueryListener(query)
        } catch (ex: Exception) {
            error = ex.localizedMessage
            loading = false
        }
    }

    @Suppress("unused")
    fun reload() {
        listenTransactionsRange(null, null)
    }

    /**
     * Añade una transacción en Firestore bajo users/{uid}/transactions y actualiza el balance del usuario atómicamente.
     * date: si es null, se guardará el timestamp actual.
     */
    @Suppress("unused")
    fun addTransaction(amount: Double, category: String, date: Date?, description: String, isExpense: Boolean, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        try {
            val userRef = db.collection("users").document(user.uid)
            val newDocRef = userRef.collection("transactions").document() // generar id

            val data = hashMapOf<String, Any>(
                "amount" to amount,
                "category" to category,
                "date" to (date?.let { Timestamp(it) } ?: Timestamp.now()),
                "description" to description,
                "isExpense" to isExpense
            )

            val batch = db.batch()
            batch.set(newDocRef, data)

            // Incrementar balance: sumar para ingreso, restar para gasto
            val incrementValue = if (isExpense) -amount else amount
            batch.update(userRef, "balance", FieldValue.increment(incrementValue))

            batch.commit()
                .addOnSuccessListener {
                    onComplete(true, null)
                }
                .addOnFailureListener { ex ->
                    onComplete(false, ex.localizedMessage)
                }
        } catch (ex: Exception) {
            onComplete(false, ex.localizedMessage)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        listener = null
        monthlyListener?.remove()
        monthlyListener = null
        monthlyIncomesListener?.remove()
        monthlyIncomesListener = null
    }
}
