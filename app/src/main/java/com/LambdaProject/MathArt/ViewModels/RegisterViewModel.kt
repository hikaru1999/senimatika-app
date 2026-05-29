package com.LambdaProject.MathArt.ViewModels

import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    private val _registerState = mutableStateOf<RegisterState>(RegisterState.Idle)
    val registerState : State<RegisterState> = _registerState

    fun saveTemporaryUserData(username: String, email: String, password: String) {
        this.username = username
        this.email = email
        this.password = password
    }

    fun registerUser(fullName: String, grade: String, kelas: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("Form Tidak Lengkap")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = authRepo.registerUser(
                username = username,
                email = email,
                password = password,
                fullName = fullName,
                grade = grade,
                kelas = kelas
            )
            _registerState.value = result
        }
    }

    fun resetRegisterForm() {
        password = ""
    }

    fun checkUsernameOrEmailExists(
        username: String,
        email: String,
        onResult: (Boolean) -> Unit
    ) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get(Source.DEFAULT)
            .addOnSuccessListener { usernameResult ->
                if (!usernameResult.isEmpty) {
                    onResult(true)
                } else {
                    firestore.collection("users")
                        .whereEqualTo("email", email)
                        .limit(1)
                        .get(Source.DEFAULT)
                        .addOnSuccessListener { emailResult ->
                            onResult(!emailResult.isEmpty)
                        }
                        .addOnFailureListener { onResult(false) }
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    /* fun checkUsernameOrEmailExists(
        username: String,
        email: String,
        onResult: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        /* var usernameExists = false
        var emailExists = false

        db.collection("users")
            .whereIn("username", listOf(username))
            .get()
            .addOnSuccessListener { usernameDocs ->
                usernameExists = !usernameDocs.isEmpty

                db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { emailDocs ->
                        emailExists = !emailDocs.isEmpty

                        onResult(usernameExists, emailExists)
                    }
                    .addOnSuccessListener { e ->
                        onResult(false, false)
                    }

            }
            .addOnSuccessListener { e ->
                onResult(false, false)
            } */

        db.collection("users")
            .whereIn("username", listOf(username))
            .get()
            .addOnSuccessListener { usernameResult ->
                if (!usernameResult.isEmpty) {
                    onResult(true)
                } else {
                    db.collection("users")
                        .whereIn("email", listOf(email))
                        .get()
                        .addOnSuccessListener { emailResult ->
                            if (!emailResult.isEmpty) {
                                onResult(true)
                            } else {
                                onResult(false)
                            }
                        }
                        .addOnFailureListener { onResult(true) }
                }
            }
            .addOnFailureListener { onResult(true) }
    } */

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class Success(val userId: String) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}