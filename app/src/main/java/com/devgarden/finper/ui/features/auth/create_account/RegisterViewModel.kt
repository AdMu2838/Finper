package com.devgarden.finper.ui.features.auth.create_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devgarden.finper.data.AuthRepository
import com.devgarden.finper.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val profile: UserProfile) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(
        fullName: String,
        email: String,
        phone: String,
        birthDate: String,
        password: String,
        confirmPassword: String
    ) {
        // Validaciones básicas en el ViewModel
        if (fullName.isBlank()) {
            _uiState.value = RegisterUiState.Error("El nombre es requerido")
            return
        }
        if (email.isBlank()) {
            _uiState.value = RegisterUiState.Error("El email es requerido")
            return
        }
        if (password.length < 6) {
            _uiState.value = RegisterUiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.Error("Las contraseñas no coinciden")
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            val result = repository.registerUser(fullName, email, password, phone, birthDate)
            if (result.isSuccess) {
                _uiState.value = RegisterUiState.Success(result.getOrNull()!!)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                _uiState.value = RegisterUiState.Error(err)
            }
        }
    }

    fun signInWithGoogle(idToken: String, fullName: String?, phone: String?, birthDate: String?) {
        _uiState.value = RegisterUiState.Loading
        viewModelScope.launch {
            val result = repository.signInWithGoogle(idToken, fullName, phone, birthDate)
            if (result.isSuccess) {
                _uiState.value = RegisterUiState.Success(result.getOrNull()!!)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                _uiState.value = RegisterUiState.Error(err)
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
