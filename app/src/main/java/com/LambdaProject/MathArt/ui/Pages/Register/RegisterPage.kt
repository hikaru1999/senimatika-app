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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
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
    val emailInteractionSource = remember { MutableInteractionSource() }
    val passwordInteractionSource = remember { MutableInteractionSource() }

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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3490DE), Color(0xFF1A237E))
                )
            )
    ) {
        // Decorative circles for background
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 100.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        TopSnackbar(
            visible = showError,
            message = errorMessage,
            onDismiss = { showError = false}
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Daftar Akun Baru",
                fontSize = 32.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Silakan isi data berikut untuk mengakses konten pembelajaran kami",
                fontSize = 14.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp)),
                color = Color.White,
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val usernameValid = registerViewModel.username.isNotBlank() && registerViewModel.username.length <= 15
                    val emailValid = registerViewModel.email.isNotBlank() && registerViewModel.email.contains("@")
                    val passwordValid = registerViewModel.password.isNotBlank() && passwordStrength > 0 && registerViewModel.password.length <= 20
                    val allValid = usernameValid && emailValid && passwordValid

                    // Username Field
                    OutlinedTextField(
                        value = registerViewModel.username,
                        onValueChange = { newUsername ->
                            if (newUsername.length <= 15 && !newUsername.contains(" ")) {
                                registerViewModel.username = newUsername
                            }
                        },
                        label = { Text("Username", fontFamily = interFontFamily, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp)) },
                        isError = isSubmitted && (registerViewModel.username.isEmpty() || registerViewModel.username.length > 15),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFF8F9FE),
                            unfocusedContainerColor = Color(0xFFF8F9FE)
                        )
                    )

                    // Email Field
                    OutlinedTextField(
                        value = registerViewModel.email,
                        onValueChange = { registerViewModel.email = it.replace(Regex("\\s"), "") },
                        label = { Text("Email Aktif", fontFamily = interFontFamily, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp)) },
                        isError = isSubmitted && (registerViewModel.email.isEmpty() || !registerViewModel.email.contains("@")),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFF8F9FE),
                            unfocusedContainerColor = Color(0xFFF8F9FE)
                        )
                    )

                    // Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = registerViewModel.password,
                            onValueChange = {
                                registerViewModel.password = it.replace(Regex("\\s"), "").take(20)
                                passwordStrength = checkPasswordStrength(registerViewModel.password)
                            },
                            label = { Text("Password", fontFamily = interFontFamily, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp)) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_close),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            isError = isSubmitted && (registerViewModel.password.isEmpty() || passwordStrength == 0),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1976D2),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color(0xFFF8F9FE),
                                unfocusedContainerColor = Color(0xFFF8F9FE)
                            )
                        )

                        if (registerViewModel.password.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            PasswordStrengthIndicator(passwordStrength)
                        }
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
                                        CoroutineScope(Dispatchers.Main).launch {
                                            registerViewModel.saveTemporaryUserData(
                                                username = registerViewModel.username,
                                                email = registerViewModel.email,
                                                password = registerViewModel.password,
                                            )
                                            navController.navigate("ProfileForm/${registerViewModel.username}/${registerViewModel.email}/${registerViewModel.password}")
                                        }
                                    }
                                }
                            } else {
                                errorMessage = "Mohon lengkapi data dengan benar"
                                showError = true
                                delayScope(2000) { isSubmitted = false }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 2.dp,
                            disabledElevation = 0.dp
                        ),
                        enabled = registerState !is RegisterViewModel.RegisterState.Loading
                    ) {
                        if (registerState is RegisterViewModel.RegisterState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text("DAFTAR SEKARANG", fontFamily = interFontFamily, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sudah punya akun? ", fontFamily = interFontFamily, color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "Log In",
                            color = Color(0xFF1976D2),
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Helper to handle delays in Compose easily
private fun delayScope(ms: Long, block: () -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        delay(ms)
        block()
    }
}

@Composable
fun PasswordStrengthIndicator(strength: Int) {
    val strengthColor = when (strength) {
        0 -> Color(0xFFEF5350)
        1 -> Color(0xFFFFB74D)
        else -> Color(0xFF66BB6A)
    }
    
    val progress by animateFloatAsState(
        targetValue = when (strength) {
            0 -> 0.33f
            1 -> 0.66f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keamanan Password",
                fontSize = 12.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = when (strength) {
                    0 -> "Lemah"
                    1 -> "Sedang"
                    else -> "Kuat"
                },
                fontSize = 12.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                color = strengthColor
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFF5F5F5), CircleShape)
                .clip(CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(strengthColor.copy(alpha = 0.7f), strengthColor)
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun TopSnackbar(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFEF5350),
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY =  { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .zIndex(10f)
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clickable { onDismiss() },
            color = containerColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontFamily = interFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun checkPasswordStrength(password: String): Int {
    return when {
        password.length >= 10 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 2
        password.length >= 6 -> 1
        else -> 0
    }
}
