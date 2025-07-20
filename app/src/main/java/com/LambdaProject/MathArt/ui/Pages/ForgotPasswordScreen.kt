package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
            .consumeWindowInsets(WindowInsets.ime)
    ) {
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
            modifier = Modifier.fillMaxSize()
        ) {
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_forgot),
                    contentDescription = null,
                    modifier = Modifier.height(135.dp).wrapContentWidth(),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cukup isikan email/username yang kamu daftar!",
                    fontSize = 24.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                    text = "Email atau Username",
                                    fontFamily = interFontFamily,
                                    fontSize = 14.sp
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
                        )

                        Button(
                            onClick = {
                                isSubmitted = true
                                if (identifier.text.isBlank()) return@Button

                                forgotPasswordViewModel.sendResetPassword(identifier.text)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = identifier.text.isNotBlank() && state !is ForgotPasswordViewModel.ForgotPasswordState.Loading
                        ) {
                            if (state is ForgotPasswordViewModel.ForgotPasswordState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Kirim Link Reset",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            "Kembali ke Login",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E88E5),
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