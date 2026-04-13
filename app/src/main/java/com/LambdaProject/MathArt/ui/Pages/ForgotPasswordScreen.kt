package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
    val state by forgotPasswordViewModel.state.collectAsState()

    var identifier by remember { mutableStateOf(TextFieldValue()) }
    var isSubmitted by remember { mutableStateOf(false) }

    var showSuccessBanner by remember { mutableStateOf(false) }
    var showErrorBanner by remember { mutableStateOf(false) }
    val errorMessage = (state as? ForgotPasswordViewModel.ForgotPasswordState.Error)?.message ?: ""

    LaunchedEffect(state) {
        when (state) {
            is ForgotPasswordViewModel.ForgotPasswordState.Success -> {
                showSuccessBanner = true
            }
            is ForgotPasswordViewModel.ForgotPasswordState.Error -> {
                showErrorBanner = true
            }
            else -> {}
        }
    }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(3500)
            showSuccessBanner = false
            forgotPasswordViewModel.resetState()
        }
    }

    LaunchedEffect(showErrorBanner) {
        if (showErrorBanner) {
            delay(3500)
            showErrorBanner = false
            forgotPasswordViewModel.resetState()
        }
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
            visible = showErrorBanner,
            message = errorMessage,
            onDismiss = { forgotPasswordViewModel.resetState() }
        )

        TopSnackbar(
            visible = showSuccessBanner,
            message = "Link reset berhasil dikirim! Silakan cek email kamu.",
            containerColor = Color(0xFF4CAF50),
            onDismiss = { forgotPasswordViewModel.resetState() }
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
                painter = painterResource(id = R.drawable.ic_forgot),
                contentDescription = null,
                modifier = Modifier.height(180.dp).wrapContentWidth(),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Lupa Password?",
                fontSize = 32.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Jangan khawatir! Masukkan email atau username kamu untuk mendapatkan instruksi reset password.",
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                text = "Email atau Username",
                                fontFamily = interFontFamily,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        isError = isSubmitted && identifier.text.isBlank(),
                        supportingText = {
                            if (isSubmitted && identifier.text.isBlank()) {
                                Text(
                                    text = "Field tidak boleh kosong",
                                    fontFamily = interFontFamily,
                                    color = Color.Red,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            isSubmitted = true
                            if (identifier.text.isBlank()) return@Button

                            forgotPasswordViewModel.sendResetPassword(identifier.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp,
                            disabledElevation = 0.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            /* disabledContainerColor = Color(0xFFE0E0E0),*/
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = identifier.text.isNotBlank() && state !is ForgotPasswordViewModel.ForgotPasswordState.Loading
                    ) {
                        if (state is ForgotPasswordViewModel.ForgotPasswordState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = "Reset Password",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    
                    Text(
                        text = "Kembali ke Login",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color(0xFF1976D2),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable {
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
