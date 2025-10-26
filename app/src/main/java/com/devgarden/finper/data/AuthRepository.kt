package com.devgarden.finper.data

import com.devgarden.finper.utils.Constants
import com.devgarden.finper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Data class para el perfil de usuario.
 * Valores por defecto permiten deserialización segura desde Firestore.
 */
data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val birthDate: Date? = null,
    val balance: Double = Constants.Defaults.DEFAULT_BALANCE
)

/**
 * Data class para ingresos fijos.
 */
data class FixedIncome(
    val id: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val createdAt: Date = Date()
)

/**
 * Repositorio centralizado para operaciones de autenticación.
 * Maneja registro, login y gestión de perfiles de usuario con Firebase.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Registra un nuevo usuario con email y contraseña.
     *
     * @param fullName Nombre completo del usuario
     * @param email Correo electrónico
     * @param password Contraseña
     * @param phone Número de teléfono
     * @param birthDate Fecha de nacimiento en formato dd/MM/yyyy
     * @return Result con el perfil del usuario o error
     */
    suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        birthDate: String
    ): Result<UserProfile> {
        return try {
            val authResult: AuthResult = auth.createUserWithEmailAndPassword(
                email.trim(),
                password
            ).await()

            val user = authResult.user
                ?: throw IllegalStateException(Constants.ErrorMessages.ERROR_NULL_USER)

            val birthDateParsed = FirebaseUtils.parseDateDDMMYYYY(birthDate)

            val profile = UserProfile(
                uid = user.uid,
                fullName = fullName,
                email = email.trim(),
                phone = phone,
                birthDate = birthDateParsed,
                balance = Constants.Defaults.DEFAULT_BALANCE
            )

            saveUserProfile(user.uid, profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia sesión con Google.
     *
     * @param idToken Token de Google
     * @param fullName Nombre completo (opcional)
     * @param phone Teléfono (opcional)
     * @param birthDate Fecha de nacimiento (opcional)
     * @return Result con el perfil del usuario o error
     */
    suspend fun signInWithGoogle(
        idToken: String,
        fullName: String?,
        phone: String?,
        birthDate: String?
    ): Result<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
                ?: throw IllegalStateException(Constants.ErrorMessages.ERROR_NULL_USER)

            val birthDateParsed = FirebaseUtils.parseDateDDMMYYYY(birthDate)

            val incomingProfile = UserProfile(
                uid = user.uid,
                fullName = fullName ?: user.displayName.orEmpty(),
                email = user.email ?: "",
                phone = phone ?: (user.phoneNumber ?: ""),
                birthDate = birthDateParsed,
                balance = Constants.Defaults.DEFAULT_BALANCE
            )

            val finalProfile = mergeWithExistingProfile(user.uid, incomingProfile)

            firestore.collection(Constants.Firestore.COLLECTION_USERS)
                .document(user.uid)
                .set(finalProfile, SetOptions.merge())
                .await()

            Result.success(finalProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia sesión con email y contraseña.
     *
     * @param email Correo electrónico
     * @param password Contraseña
     * @return Result con el perfil del usuario o error
     */
    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<UserProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(
                email.trim(),
                password
            ).await()

            val user = authResult.user
                ?: throw IllegalStateException(Constants.ErrorMessages.ERROR_NULL_USER)

            val profile = fetchUserProfile(user.uid, user)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Agrega un ingreso fijo y actualiza el balance del usuario.
     *
     * @param uid ID del usuario
     * @param amount Monto del ingreso
     * @param description Descripción del ingreso
     * @return Result exitoso o error
     */
    suspend fun addFixedIncome(
        uid: String,
        amount: Double,
        description: String? = null
    ): Result<Unit> {
        if (!FirebaseUtils.isValidTransactionAmount(amount)) {
            return Result.failure(
                IllegalArgumentException(Constants.ErrorMessages.ERROR_INVALID_AMOUNT)
            )
        }

        return try {
            val incomesColl = firestore
                .collection(Constants.Firestore.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.Firestore.COLLECTION_FIXED_INCOMES)

            val docRef = incomesColl.document()
            val income = FixedIncome(
                id = docRef.id,
                amount = amount,
                description = description ?: "",
                createdAt = Date()
            )

            docRef.set(income).await()

            firestore.collection(Constants.Firestore.COLLECTION_USERS)
                .document(uid)
                .update(Constants.Firestore.FIELD_BALANCE, FieldValue.increment(amount))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Métodos helper privados ---

    /**
     * Guarda el perfil del usuario en Firestore.
     */
    private suspend fun saveUserProfile(uid: String, profile: UserProfile) {
        firestore.collection(Constants.Firestore.COLLECTION_USERS)
            .document(uid)
            .set(profile)
            .await()
    }

    /**
     * Obtiene el perfil del usuario desde Firestore.
     */
    private suspend fun fetchUserProfile(
        uid: String,
        user: com.google.firebase.auth.FirebaseUser
    ): UserProfile {
        val doc = firestore.collection(Constants.Firestore.COLLECTION_USERS)
            .document(uid)
            .get()
            .await()

        return try {
            doc.toObject(UserProfile::class.java) ?: buildFallbackProfile(doc, user)
        } catch (_: Exception) {
            buildFallbackProfile(doc, user)
        }
    }

    /**
     * Construye un perfil de fallback cuando la deserialización falla.
     */
    private fun buildFallbackProfile(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        user: com.google.firebase.auth.FirebaseUser
    ): UserProfile {
        return UserProfile(
            uid = user.uid,
            fullName = FirebaseUtils.extractString(
                doc,
                Constants.Firestore.FIELD_FULL_NAME,
                user.displayName ?: Constants.Defaults.DEFAULT_USER_NAME
            ),
            email = FirebaseUtils.extractString(
                doc,
                Constants.Firestore.FIELD_EMAIL,
                user.email ?: ""
            ),
            phone = FirebaseUtils.extractString(
                doc,
                Constants.Firestore.FIELD_PHONE,
                user.phoneNumber ?: ""
            ),
            birthDate = FirebaseUtils.extractDate(doc, "birthDate"),
            balance = FirebaseUtils.extractDouble(
                doc,
                Constants.Firestore.FIELD_BALANCE,
                Constants.Defaults.DEFAULT_BALANCE
            )
        )
    }

    /**
     * Combina el perfil entrante con el existente en Firestore.
     */
    private suspend fun mergeWithExistingProfile(
        uid: String,
        incomingProfile: UserProfile
    ): UserProfile {
        val docRef = firestore.collection(Constants.Firestore.COLLECTION_USERS).document(uid)
        val existingDoc = docRef.get().await()

        return if (existingDoc.exists()) {
            val existingBalance = FirebaseUtils.extractDouble(
                existingDoc,
                Constants.Firestore.FIELD_BALANCE,
                Constants.Defaults.DEFAULT_BALANCE
            )
            incomingProfile.copy(balance = existingBalance)
        } else {
            incomingProfile
        }
    }
}
