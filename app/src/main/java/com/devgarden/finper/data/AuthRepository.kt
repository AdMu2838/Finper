package com.devgarden.finper.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Añadir valores por defecto para permitir deserialización con Firestore
data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val birthDate: Date? = null,
    val balance: Double = 0.0 // nuevo campo: balance total del usuario
)

// Nueva entidad para ingresos fijos
data class FixedIncome(
    val id: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val createdAt: Date = Date()
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun parseDateOrNull(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(dateStr)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        birthDate: String
    ): Result<UserProfile> {
        return try {
            val authResult: AuthResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("User is null after registration")

            val birthDateDate: Date? = parseDateOrNull(birthDate)

            val profile = UserProfile(
                uid = user.uid,
                fullName = fullName,
                email = email.trim(),
                phone = phone,
                birthDate = birthDateDate,
                balance = 0.0 // por defecto 0.0 al registrar
            )
            // Save profile to Firestore under collection "users" with document id = uid
            firestore.collection("users").document(user.uid).set(profile).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String, fullName: String?, phone: String?, birthDate: String?): Result<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw IllegalStateException("User is null after Google sign-in")

            val birthDateDate: Date? = parseDateOrNull(birthDate)

            // Construir perfil base con información disponible
            val incomingProfile = UserProfile(
                uid = user.uid,
                fullName = fullName ?: user.displayName.orEmpty(),
                email = user.email ?: "",
                phone = phone ?: (user.phoneNumber ?: ""),
                birthDate = birthDateDate,
                balance = 0.0 // valor por defecto, será reemplazado si ya existe
            )

            // Leer documento existente para no sobrescribir balance u otros campos
            val docRef = firestore.collection("users").document(user.uid)
            val existingDoc = docRef.get().await()
            val finalProfile = if (existingDoc.exists()) {
                val existingBalance = try {
                    val raw = existingDoc.get("balance")
                    if (raw is Number) raw.toDouble() else 0.0
                } catch (_: Exception) { 0.0 }
                incomingProfile.copy(balance = existingBalance)
            } else {
                incomingProfile
            }

            // Guardar/merge profile a Firestore sin eliminar campos existentes
            docRef.set(finalProfile, SetOptions.merge()).await()

            Result.success(finalProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Nueva función: login con email y password
    suspend fun signInWithEmailPassword(email: String, password: String): Result<UserProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("User is null after sign-in")

            // Intentar obtener perfil guardado en Firestore
            val doc = firestore.collection("users").document(user.uid).get().await()

            // Primero intentamos deserializar normalmente
            val profileFromDb: UserProfile? = try {
                doc.toObject(UserProfile::class.java)
            } catch (_: Exception) {
                null
            }

            // Si deserialización falla o es null, leer campos individuales como fallback
            val profile = if (profileFromDb != null) {
                profileFromDb
            } else {
                val fullName = doc.getString("fullName") ?: user.displayName ?: ""
                val emailDb = doc.getString("email") ?: user.email ?: email.trim()
                val phone = doc.getString("phone") ?: user.phoneNumber ?: ""
                val birthDateField = try { doc.getDate("birthDate") } catch (_: Exception) { null }
                val balanceField = try {
                    val raw = doc.get("balance")
                    if (raw is Number) raw.toDouble() else 0.0
                } catch (_: Exception) { 0.0 }

                UserProfile(
                    uid = user.uid,
                    fullName = fullName,
                    email = emailDb,
                    phone = phone,
                    birthDate = birthDateField,
                    balance = balanceField
                )
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Nueva función: agregar un ingreso fijo y actualizar el balance del usuario
    suspend fun addFixedIncome(uid: String, amount: Double, description: String? = null): Result<Unit> {
        if (amount <= 0.0) return Result.failure(IllegalArgumentException("El monto debe ser mayor que 0"))
        return try {
            val incomesColl = firestore.collection("users").document(uid).collection("fixedIncomes")
            val docRef = incomesColl.document()
            val income = FixedIncome(
                id = docRef.id,
                amount = amount,
                description = description ?: "",
                createdAt = Date()
            )
            // Guardar el ingreso fijo
            docRef.set(income).await()
            // Incrementar balance del usuario de forma atómica
            firestore.collection("users").document(uid).update("balance", FieldValue.increment(amount)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
