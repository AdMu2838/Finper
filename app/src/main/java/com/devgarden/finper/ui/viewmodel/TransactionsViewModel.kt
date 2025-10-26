package com.devgarden.finper.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.devgarden.finper.utils.Constants
import com.devgarden.finper.utils.DateUtils
import com.devgarden.finper.utils.FirebaseUtils
import java.util.*

/**
 * ViewModel que gestiona las transacciones del usuario.
 * Proporciona acceso a la lista de transacciones, gastos mensuales y consultas por categoría.
 */
class TransactionsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Data Transfer Object para transacciones.
     *
     * @property id ID único de la transacción
     * @property amount Monto de la transacción
     * @property category Categoría de la transacción
     * @property date Fecha de la transacción
     * @property description Descripción o título
     * @property isExpense Si es gasto (true) o ingreso (false)
     */
    data class TransactionDto(
        val id: String,
        val amount: Double,
        val category: String,
        val date: Date?,
        val description: String,
        val isExpense: Boolean
    )

    // --- Estados principales ---
    var transactions by mutableStateOf<List<TransactionDto>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private var listener: ListenerRegistration? = null

    // --- Estados para gastos mensuales ---
    var monthlyExpenses by mutableStateOf(0.0)
        private set

    var monthlyLoading by mutableStateOf(false)
        private set

    var monthlyError by mutableStateOf<String?>(null)
        private set

    private var monthlyListener: ListenerRegistration? = null

    // --- Estados para ingresos mensuales ---
    var monthlyIncomes by mutableStateOf(0.0)
        private set

    var monthlyIncomesLoading by mutableStateOf(false)
        private set

    var monthlyIncomesError by mutableStateOf<String?>(null)
        private set

    private var monthlyIncomesListener: ListenerRegistration? = null

    // --- Estados para consultas por categoría ---
    var categoryTransactions by mutableStateOf<List<TransactionDto>>(emptyList())
        private set

    var categoryLoading by mutableStateOf(false)
        private set

    var categoryError by mutableStateOf<String?>(null)
        private set

    var categoryIndexUrl by mutableStateOf<String?>(null)
        private set

    init {
        listenTransactionsRange(null, null)
        listenCurrentMonthExpenses()
        listenCurrentMonthIncomes()
    }

    /**
     * Escucha transacciones en tiempo real dentro de un rango de fechas.
     *
     * @param start Fecha de inicio del rango (null para sin límite)
     * @param end Fecha de fin del rango (null para sin límite)
     */
    fun listenTransactionsRange(start: Date?, end: Date?) {
        val user = auth.currentUser
        if (user == null) {
            clearTransactions(Constants.ErrorMessages.ERROR_USER_NOT_AUTHENTICATED)
            return
        }

        var query: Query = buildBaseQuery(user.uid)

        if (start != null && end != null) {
            query = query
                .whereGreaterThanOrEqualTo(
                    Constants.Firestore.FIELD_DATE,
                    FirebaseUtils.toFirebaseTimestamp(start)
                )
                .whereLessThanOrEqualTo(
                    Constants.Firestore.FIELD_DATE,
                    FirebaseUtils.toFirebaseTimestamp(end)
                )
        }

        query = query.orderBy(Constants.Firestore.FIELD_DATE, Query.Direction.DESCENDING)

        attachQueryListener(query)
    }

    /**
     * Carga transacciones filtradas por categoría.
     * Intenta ordenar en el servidor; si falla por falta de índice, ordena localmente.
     *
     * @param category Nombre de la categoría a filtrar
     */
    fun loadTransactionsByCategory(category: String) {
        val user = auth.currentUser
        if (user == null) {
            clearCategoryQuery()
            categoryError = Constants.ErrorMessages.ERROR_USER_NOT_AUTHENTICATED
            return
        }

        categoryLoading = true
        categoryError = null
        categoryIndexUrl = null

        try {
            buildBaseQuery(user.uid)
                .whereEqualTo(Constants.Firestore.FIELD_CATEGORY, category)
                .orderBy(Constants.Firestore.FIELD_DATE, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    categoryTransactions = parseTransactionsSnapshot(snapshot.documents)
                    categoryLoading = false
                }
                .addOnFailureListener { ex ->
                    handleCategoryQueryFailure(ex, user.uid, category)
                }
        } catch (ex: Exception) {
            categoryLoading = false
            categoryError = ex.localizedMessage
            categoryIndexUrl = null
        }
    }

    /**
     * Limpia el estado de la consulta por categoría.
     */
    fun clearCategoryQuery() {
        categoryTransactions = emptyList()
        categoryLoading = false
        categoryError = null
        categoryIndexUrl = null
    }

    /**
     * Escucha en tiempo real los gastos del mes actual.
     */
    fun listenCurrentMonthExpenses() {
        listenMonthlyTransactions(
            isExpense = true,
            onUpdate = { sum -> monthlyExpenses = sum },
            onLoading = { loading -> monthlyLoading = loading },
            onError = { error -> monthlyError = error },
            listenerRef = { monthlyListener = it }
        )
    }

    /**
     * Escucha en tiempo real los ingresos del mes actual.
     */
    fun listenCurrentMonthIncomes() {
        listenMonthlyTransactions(
            isExpense = false,
            onUpdate = { sum -> monthlyIncomes = sum },
            onLoading = { loading -> monthlyIncomesLoading = loading },
            onError = { error -> monthlyIncomesError = error },
            listenerRef = { monthlyIncomesListener = it }
        )
    }

    /**
     * Añade una transacción en Firestore y actualiza el balance del usuario.
     *
     * @param amount Monto de la transacción
     * @param category Categoría
     * @param date Fecha de la transacción
     * @param description Descripción
     * @param isExpense Si es un gasto
     * @param onComplete Callback con el resultado
     */
    fun addTransaction(
        amount: Double,
        category: String,
        date: Date?,
        description: String,
        isExpense: Boolean,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, Constants.ErrorMessages.ERROR_USER_NOT_AUTHENTICATED)
            return
        }

        if (!FirebaseUtils.isValidTransactionAmount(amount)) {
            onComplete(false, Constants.ErrorMessages.ERROR_INVALID_AMOUNT)
            return
        }

        try {
            val userRef = db.collection(Constants.Firestore.COLLECTION_USERS).document(user.uid)
            val newDocRef = userRef.collection(Constants.Firestore.COLLECTION_TRANSACTIONS).document()

            val data = FirebaseUtils.buildTransactionData(amount, category, date, description, isExpense)

            val batch = db.batch()
            batch.set(newDocRef, data)

            val incrementValue = if (isExpense) -amount else amount
            batch.update(userRef, Constants.Firestore.FIELD_BALANCE, FieldValue.increment(incrementValue))

            batch.commit()
                .addOnSuccessListener { onComplete(true, null) }
                .addOnFailureListener { ex -> onComplete(false, ex.localizedMessage) }
        } catch (ex: Exception) {
            onComplete(false, ex.localizedMessage)
        }
    }

    // --- Métodos helper privados ---

    /**
     * Construye la query base para transacciones.
     */
    private fun buildBaseQuery(uid: String): Query {
        return db.collection(Constants.Firestore.COLLECTION_USERS)
            .document(uid)
            .collection(Constants.Firestore.COLLECTION_TRANSACTIONS)
    }

    /**
     * Escucha transacciones mensuales (gastos o ingresos).
     */
    private fun listenMonthlyTransactions(
        isExpense: Boolean,
        onUpdate: (Double) -> Unit,
        onLoading: (Boolean) -> Unit,
        onError: (String?) -> Unit,
        listenerRef: (ListenerRegistration?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            listenerRef(null)
            onUpdate(0.0)
            onLoading(false)
            onError(Constants.ErrorMessages.ERROR_USER_NOT_AUTHENTICATED)
            return
        }

        onLoading(true)
        onError(null)

        try {
            val range = DateUtils.getMonthlyRange()

            val query = buildBaseQuery(user.uid)
                .whereGreaterThanOrEqualTo(
                    Constants.Firestore.FIELD_DATE,
                    FirebaseUtils.toFirebaseTimestamp(range.start)
                )
                .whereLessThanOrEqualTo(
                    Constants.Firestore.FIELD_DATE,
                    FirebaseUtils.toFirebaseTimestamp(range.end)
                )
                .whereEqualTo(Constants.Firestore.FIELD_IS_EXPENSE, isExpense)
                .orderBy(Constants.Firestore.FIELD_DATE, Query.Direction.DESCENDING)

            val listener = query.addSnapshotListener { snapshot, ex ->
                if (ex != null) {
                    onError(ex.localizedMessage)
                    onLoading(false)
                    return@addSnapshotListener
                }

                val sum = snapshot?.documents?.sumOf { doc ->
                    FirebaseUtils.extractDouble(doc, Constants.Firestore.FIELD_AMOUNT, 0.0)
                } ?: 0.0

                onUpdate(sum)
                onLoading(false)
            }

            listenerRef(listener)
        } catch (ex: Exception) {
            onError(ex.localizedMessage)
            onLoading(false)
        }
    }

    /**
     * Adjunta un listener a una query de transacciones.
     */
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

            transactions = snapshot?.documents?.let { parseTransactionsSnapshot(it) } ?: emptyList()
            loading = false
        }
    }

    /**
     * Parsea documentos de Firestore a TransactionDto.
     */
    private fun parseTransactionsSnapshot(
        documents: List<com.google.firebase.firestore.DocumentSnapshot>
    ): List<TransactionDto> {
        return documents.mapNotNull { doc ->
            try {
                TransactionDto(
                    id = doc.id,
                    amount = FirebaseUtils.extractDouble(doc, Constants.Firestore.FIELD_AMOUNT),
                    category = FirebaseUtils.extractString(
                        doc,
                        Constants.Firestore.FIELD_CATEGORY,
                        Constants.Defaults.DEFAULT_CATEGORY
                    ),
                    date = FirebaseUtils.extractDate(doc, Constants.Firestore.FIELD_DATE),
                    description = FirebaseUtils.extractString(doc, Constants.Firestore.FIELD_DESCRIPTION),
                    isExpense = FirebaseUtils.extractBoolean(doc, Constants.Firestore.FIELD_IS_EXPENSE)
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Maneja errores de consultas por categoría.
     */
    private fun handleCategoryQueryFailure(ex: Exception, userId: String, category: String) {
        categoryLoading = false
        categoryError = ex.localizedMessage

        val msg = ex.localizedMessage ?: ""
        val regex = """https?://console.firebase.google.com[^"]+""".toRegex()
        categoryIndexUrl = regex.find(msg)?.value

        val requiresIndex = msg.contains("requires an index") || categoryIndexUrl != null
        if (requiresIndex) {
            performCategoryQueryFallback(userId, category)
        }
    }

    /**
     * Realiza consulta de fallback sin ordenar en servidor.
     */
    private fun performCategoryQueryFallback(userId: String, category: String) {
        buildBaseQuery(userId)
            .whereEqualTo(Constants.Firestore.FIELD_CATEGORY, category)
            .get()
            .addOnSuccessListener { snapshot ->
                val transactions = parseTransactionsSnapshot(snapshot.documents)
                categoryTransactions = transactions.sortedWith(
                    compareByDescending { it.date ?: Date(0) }
                )
            }
            .addOnFailureListener {
                // Ignorar: ya tenemos el error original
            }
    }

    /**
     * Limpia el estado de transacciones.
     */
    private fun clearTransactions(errorMessage: String) {
        listener?.remove()
        transactions = emptyList()
        loading = false
        error = errorMessage
        listener = null
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        monthlyListener?.remove()
        monthlyIncomesListener?.remove()
        listener = null
        monthlyListener = null
        monthlyIncomesListener = null
    }
}
