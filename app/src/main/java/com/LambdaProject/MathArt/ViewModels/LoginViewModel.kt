package com.LambdaProject.MathArt.ViewModels

import com.LambdaProject.MathArt.data.repository.AuthRepository
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginState = _loginState.asStateFlow()

    fun login(identifier: String, password: String) {
        if (_loginState.value is LoginResult.Loading) return

        _loginState.value = LoginResult.Loading
        authRepo.loginUser(identifier, password) { success, errorMessage, username ->
            if (success) {
                _loginState.value = LoginResult.Success(username ?: "User")
            } else {
                /* _loginState.value = LoginResult.Error(errorMessage ?: "Username atau Password Tidak Sesuai") */
                _loginState.value = LoginResult.Error(errorMessage ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginResult.Idle
    }

}

sealed class LoginResult {
    object Idle : LoginResult()
    object Loading: LoginResult()
    data class Success(val username: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}