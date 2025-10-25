package com.devgarden.finper.ui.features.auth.forgot_password

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    object Success : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

class ForgotPasswordViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.value = ForgotPasswordUiState.Error("El correo es requerido")
            return
        }
        _uiState.value = ForgotPasswordUiState.Loading
        viewModelScope.launch {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ForgotPwdVM", "Reset email sent to $email")
                        _uiState.value = ForgotPasswordUiState.Success
                    } else {
                        val msg = task.exception?.localizedMessage ?: "Error al enviar el correo"
                        Log.d("ForgotPwdVM", "Error sending reset email: $msg")
                        _uiState.value = ForgotPasswordUiState.Error(msg)
                    }
                }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }
}

