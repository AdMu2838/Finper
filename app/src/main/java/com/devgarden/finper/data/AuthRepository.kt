package com.devgarden.finper.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val birthDate: String
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
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
            val profile = UserProfile(
                uid = user.uid,
                fullName = fullName,
                email = email.trim(),
                phone = phone,
                birthDate = birthDate
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

            // If new user, set profile information; otherwise update or fetch
            val profile = UserProfile(
                uid = user.uid,
                fullName = fullName ?: user.displayName.orEmpty(),
                email = user.email ?: "",
                phone = phone ?: (user.phoneNumber ?: ""),
                birthDate = birthDate ?: ""
            )

            // Save/merge profile to Firestore
            firestore.collection("users").document(user.uid).set(profile).await()

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
