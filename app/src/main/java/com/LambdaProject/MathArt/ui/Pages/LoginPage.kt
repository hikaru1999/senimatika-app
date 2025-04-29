package com.LambdaProject.MathArt.ui.Pages

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.*
import com.LambdaProject.MathArt.ViewModels.*
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

    /* AnimatedVisibility(
        visible = showErrorBanner,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Popup(
            alignment = Alignment.TopCenter,
            offset = IntOffset(0, 80)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color(0xFFD32F2F), shape = RoundedCornerShape(4.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = interFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.ime)
    ) {
        TopSnackbar(
            visible = showErrorBanner,
            message = errorMessage,
            onDismiss = { showErrorBanner = false}
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF3490DE))
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
            Image(
                painter = painterResource(id = R.drawable.img_logo_white),
                contentDescription = "Logo App Putih",
                modifier = Modifier.height(70.dp).wrapContentWidth(),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Log in untuk Belajar!",
                fontSize = 35.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 45.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
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
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            if (!it.text.contains(" ") && !it.text.contains("\n")) {
                                identifier = it
                            }
                        },
                        label = {
                            Text(
                                text = "Username atau Email",
                                fontFamily = interFontFamily,
                                fontSize = 14.sp
                            )
                        },
                        isError = isSubmitted && identifierError,
                        supportingText = {
                            if (isSubmitted && identifierError) {
                                Text(
                                    text = "Username atau Email tidak boleh kosong",
                                    fontFamily = interFontFamily,
                                    color = Color.Red,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        isError = isSubmitted && passwordError,
                        supportingText = {
                            if (isSubmitted && passwordError) {
                                Text(
                                    text = "Password tidak boleh kosong",
                                    fontFamily = interFontFamily,
                                    color = Color.Red,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Password",
                                fontFamily = interFontFamily,
                                fontSize = 14.sp
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) painterResource(id = R.drawable.ic_eye_open)
                                else painterResource(id = R.drawable.ic_eye_close)

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = image,
                                    contentDescription = "Toggle Password Visibility",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                    )
                    Button(
                        onClick = {
                            isSubmitted = true
                            identifierError = identifier.text.isEmpty()
                            passwordError = password.text.isEmpty()

                            if (identifierError || passwordError) {
                                return@Button
                            }

                            loginViewModel.login(identifier.text, password.text)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loginState !is LoginResult.Loading
                    ) {
                        if (loginState is LoginResult.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(text = "Login", fontFamily = interFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = "Belum punya akun? ",
                            fontFamily = interFontFamily,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Daftar",
                            color = Color(0xFF1E88E5),
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                navController.navigate("register")
                            }
                        )
                    }
                }
            }
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