package com.LambdaProject.MathArt.ViewModels

import com.LambdaProject.MathArt.Data.AuthRepo
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    sealed class ForgotPasswordState {
        object Idle : ForgotPasswordState()
        object Loading : ForgotPasswordState()
        data class Success(val message: String) : ForgotPasswordState()
        data class Error(val message: String) : ForgotPasswordState()
    }

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val state: StateFlow<ForgotPasswordState> = _state

    fun sendResetPassword(identifier: String) {
        viewModelScope.launch {
            _state.value = ForgotPasswordState.Loading
            val result = authRepo.sendResetPassword(identifier)
            _state.value = result.fold(
                onSuccess = { ForgotPasswordState.Success(it) },
                onFailure = { ForgotPasswordState.Error(it.message ?: "Terjadi kesalahan") }
            )
        }
    }

    fun resetState() {
        _state.value = ForgotPasswordState.Idle
    }
}
