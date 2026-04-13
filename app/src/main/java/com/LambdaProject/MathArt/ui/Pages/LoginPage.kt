package com.LambdaProject.MathArt.ui.Pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.*
import com.LambdaProject.MathArt.ViewModels.*
import com.LambdaProject.MathArt.ui.Pages.Register.TopSnackbar
import kotlinx.coroutines.*

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val loginState by loginViewModel.loginState.collectAsState()

    var identifier by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var passwordError by remember { mutableStateOf(false) }
    var showErrorBanner by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var identifierError by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

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
            visible = showErrorBanner,
            message = errorMessage,
            onDismiss = { showErrorBanner = false}
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo_white),
                contentDescription = "Logo App Putih",
                modifier = Modifier.height(100.dp).wrapContentWidth(),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Selamat Datang!",
                fontSize = 32.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Log in untuk melanjutkan belajarmu di MathArt",
                fontSize = 14.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp)),
                color = Color.White,
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Identifier Field
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            if (!it.text.contains(" ") && !it.text.contains("\n")) {
                                identifier = it
                            }
                        },
                        label = { Text("Username atau Email", fontFamily = interFontFamily, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp)) },
                        isError = isSubmitted && identifierError,
                        supportingText = {
                            if (isSubmitted && identifierError) {
                                Text("Wajib diisi", color = Color.Red, fontSize = 11.sp, fontFamily = interFontFamily)
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFF8F9FE),
                            unfocusedContainerColor = Color(0xFFF8F9FE)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", fontFamily = interFontFamily, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp)) },
                        isError = isSubmitted && passwordError,
                        supportingText = {
                            if (isSubmitted && passwordError) {
                                Text("Wajib diisi", color = Color.Red, fontSize = 11.sp, fontFamily = interFontFamily)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = if (passwordVisible) painterResource(R.drawable.ic_eye_open) else painterResource(R.drawable.ic_eye_close),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFF8F9FE),
                            unfocusedContainerColor = Color(0xFFF8F9FE)
                        )
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = "Lupa Password?",
                            color = Color(0xFF1976D2),
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { navController.navigate("ForgotPassword") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isSubmitted = true
                            identifierError = identifier.text.isEmpty()
                            passwordError = password.text.isEmpty()

                            if (!identifierError && !passwordError) {
                                loginViewModel.login(identifier.text, password.text)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = loginState !is LoginResult.Loading
                    ) {
                        if (loginState is LoginResult.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Text("LOGIN", fontFamily = interFontFamily, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Belum punya akun? ", fontFamily = interFontFamily, color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "Daftar Sekarang",
                            color = Color(0xFF1976D2),
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { navController.navigate("register") }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginResult.Success -> {
                val username = state.username
                with(sharedPreferences.edit()) {
                    putString("USERNAME_KEY", username)
                    apply()
                }
                Toast.makeText(context, "Login Berhasil", Toast.LENGTH_SHORT).show()
                navController.navigate("Dashboard/$username") {
                    popUpTo(0)
                    launchSingleTop = true
                    restoreState = true
                }
            }
            is LoginResult.Error -> {
                errorMessage = state.message
                showErrorBanner = true
                coroutineScope.launch {
                    delay(3500)
                    showErrorBanner = false
                }
            }
            else -> {}
        }
    }
}
