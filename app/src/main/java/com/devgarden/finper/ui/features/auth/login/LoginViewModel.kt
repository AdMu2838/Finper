package com.devgarden.finper.ui.features.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devgarden.finper.data.AuthRepository
import com.devgarden.finper.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val profile: UserProfile) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun signInWithGoogle(idToken: String) {
        Log.d("LoginViewModel", "start signInWithGoogle")
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            val result = repository.signInWithGoogle(idToken, null, null, null)
            if (result.isSuccess) {
                val profile = result.getOrNull()!!
                Log.d("LoginViewModel", "signInWithGoogle success: ${profile}")
                _uiState.value = LoginUiState.Success(profile)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                Log.d("LoginViewModel", "signInWithGoogle error: $err")
                _uiState.value = LoginUiState.Error(err)
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
