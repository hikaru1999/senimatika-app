package com.LambdaProject.MathArt.ui.Pages.Register

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility

import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ViewModels.RegisterViewModel

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.*

@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = hiltViewModel(LocalContext.current as ComponentActivity))
{
    val registerState by registerViewModel.registerState
    val email by remember { mutableStateOf("") }
    val password by remember { mutableStateOf("") }
    val username by remember { mutableStateOf("") }
    val emailInteractionSource = remember { MutableInteractionSource() }
    val passwordInteractionSource = remember { MutableInteractionSource() }

    var passwordFieldFocused by remember { mutableStateOf(false) }
    var emailFieldFocused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordStrength by remember { mutableIntStateOf(0) }
    var isSubmitted by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        registerViewModel.resetRegisterForm()
    }

    LaunchedEffect(showError) {
        if (showError) delay(3500)
        showError = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.ime)
    ) {
        TopSnackbar(
            visible = showError,
            message = errorMessage,
            onDismiss = { showError = false}
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(Color(0xFF3490De))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daftar dulu yuk!",
                fontFamily = interFontFamily,
                fontSize = 35.sp,
                lineHeight = 40.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Buat username, masukkan email aktif dan password untuk mengakses konten pembelajaran kami ya...",
                fontFamily = interFontFamily,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val usernameValid = registerViewModel.username.isNotBlank() && registerViewModel.username.length <= 15 /* && !registerViewModel.username.contains(" ") */
                    val emailValid = registerViewModel.email.isNotBlank() && registerViewModel.email.contains("@") /* && !registerViewModel.email.contains(" ") */
                    val passwordValid = registerViewModel.password.isNotBlank() && passwordStrength > 0 && registerViewModel.password.length <= 20
                    val allValid = usernameValid && emailValid && passwordValid

                    OutlinedTextField(
                        value = registerViewModel.username,
                        onValueChange = { newUsername ->
                            if (newUsername.length <= 15 && !newUsername.contains(" ")) {
                                registerViewModel.username = newUsername
                            }
                        },
                        label = { Text("Masukkan username", fontFamily = interFontFamily, fontWeight = FontWeight.Normal) },
                        isError = isSubmitted && (registerViewModel.username.isEmpty() || registerViewModel.username.length > 15 || registerViewModel.username.contains(" ")),
                        modifier = Modifier.fillMaxWidth(),
                        /* supportingText = {
                            when {
                                registerViewModel.username.length > 15 ->
                                    Text("Username tidak boleh lebih dari 15 karakter", fontFamily = interFontFamily, color = Color.Red, fontSize = 12.sp)
                            }
                        } */
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = registerViewModel.email,
                        onValueChange = { registerViewModel.email = it.replace(Regex("\\s"), "") },
                        interactionSource = emailInteractionSource,
                        singleLine = true,
                        label = { Text("Email", fontFamily = interFontFamily, fontWeight = FontWeight.Normal) },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
                        isError = registerViewModel.email.isNotEmpty() && !registerViewModel.email.contains("@"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                emailFieldFocused = focusState.isFocused
                            },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = registerViewModel.password,
                        onValueChange = {
                            registerViewModel.password = it.replace(Regex("\\s"), "").take(20)
                            passwordStrength = checkPasswordStrength(registerViewModel.password)
                        },
                        interactionSource = passwordInteractionSource,
                        label = { Text("Masukkan Password", fontFamily = interFontFamily) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_close),
                                    contentDescription = "Toggle Password Visibility",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Password),
                        isError = (isSubmitted && registerViewModel.password.isEmpty()) || (registerViewModel.password.isNotEmpty() && passwordStrength == 0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                passwordFieldFocused = focusState.isFocused
                            },
                    )

                    if (registerViewModel.password.isNotEmpty() && passwordStrength != -1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PasswordStrengthIndicator(passwordStrength)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            isSubmitted = true
                            if (allValid) {
                                registerViewModel.checkUsernameOrEmailExists(
                                    username = registerViewModel.username,
                                    email = registerViewModel.email
                                ) { exists ->
                                    if (exists) {
                                        errorMessage = "Username atau Email telah terdaftar"
                                        showError = true
                                        isSubmitted = false
                                    } else {
                                        isSubmitted = true
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(1500)
                                            registerViewModel.saveTemporaryUserData(
                                                username = registerViewModel.username,
                                                email = registerViewModel.email,
                                                password = registerViewModel.password,
                                            )
                                            navController.navigate("ProfileForm/${username}/${email}/${password}")
                                        }
                                    }
                                }
                            }
                        },
                        enabled = allValid && registerState !is RegisterViewModel.RegisterState.Loading && !isSubmitted,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSubmitted) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Lanjut!",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }

                        /* if (showError) {
                            AlertDialog(
                                onDismissRequest = { showError = false },
                                title = {
                                    Text(text = "Registrasi Gagal", fontWeight = FontWeight.Bold, fontFamily = interFontFamily, fontSize = 18.sp)
                                },
                                text = {
                                    Text(text = errorMessage, fontFamily = interFontFamily)
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { showError = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                        shape = RoundedCornerShape(5.dp)
                                    ) {
                                        Text("Ubah Data", color = Color.White, fontFamily = interFontFamily)
                                    }
                                },
                                containerColor = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } */
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row {
                        Text(text = "Sudah punya akun? ",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.DarkGray,
                            fontSize = 14.sp)

                        Text(
                            text = "Log In",
                            color = Color(0xFF1E88E5),
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: Int) {
    val strengthColor by animateColorAsState(
        targetValue = when (strength) {
            0 -> Color(0xFFED0012)
            1 -> Color(0xFFF5CC1C)
            else -> Color(0xFF00B926)
        },
        label = "strengthColor"
    )
    val progress by animateFloatAsState(
        targetValue = when (strength) {
            0 -> 0.3f
            1 -> 0.6f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = strengthColor,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black, fontFamily = interFontFamily)) {
                    append("Cek password: ")
                }
                withStyle(style = SpanStyle(color = strengthColor)) {
                    append(
                        when (strength) {
                            0 -> "Gampang ditebak \uD83D\uDE33"
                            1 -> "Standar \uD83D\uDE09"
                            else -> "Sulit ditembus \uD83D\uDE0E"
                        }
                    )
                }
            },
            fontSize = 12.sp
        )
    }
}

@Composable
fun TopSnackbar(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY =  { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .zIndex(1f)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2f)),
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable { onDismiss() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontFamily = interFontFamily,
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun checkPasswordStrength(password: String): Int {
    return when {
        password.length > 20 -> -1
        password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 2
        password.length >= 6 -> 1
        else -> 0
    }
}