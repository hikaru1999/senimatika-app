package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.ForgotPasswordViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.Pages.Register.TopSnackbar
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
    val state by forgotPasswordViewModel.state.collectAsState()

    var step by remember { mutableIntStateOf(1) } // 1: Kirim OTP, 2: Input OTP & Password, 3: Sukses
    var identifier by remember { mutableStateOf("") }
    var targetEmail by remember { mutableStateOf("") }
    var targetUsername by remember { mutableStateOf("") }

    var otpInput by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var cooldownSeconds by remember { mutableIntStateOf(0) }
    var isSubmitted by remember { mutableStateOf(false) }
    var formErrorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showErrorBanner by remember { mutableStateOf(false) }
    var serverErrorMessage by remember { mutableStateOf("") }

    // Hitung mundur cooldown kirim ulang OTP
    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds -= 1
        }
    }

    // Tangani perubahan state ViewModel
    LaunchedEffect(state) {
        when (val s = state) {
            is ForgotPasswordViewModel.ForgotPasswordState.OtpSent -> {
                targetEmail = s.email
                targetUsername = s.username
                step = 2
                cooldownSeconds = 60
                successMessage = "Kode OTP 6-digit telah dikirim ke $targetEmail"
                showSuccessBanner = true
                forgotPasswordViewModel.resetState()
            }
            is ForgotPasswordViewModel.ForgotPasswordState.ResetSuccess -> {
                step = 3
                forgotPasswordViewModel.resetState()
            }
            is ForgotPasswordViewModel.ForgotPasswordState.Error -> {
                serverErrorMessage = s.message
                showErrorBanner = true
            }
            is ForgotPasswordViewModel.ForgotPasswordState.Success -> {
                successMessage = s.message
                showSuccessBanner = true
            }
            else -> {}
        }
    }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(3500)
            showSuccessBanner = false
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
        // Lingkaran ornamen background
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
            message = serverErrorMessage,
            onDismiss = {
                showErrorBanner = false
                forgotPasswordViewModel.resetState()
            }
        )

        TopSnackbar(
            visible = showSuccessBanner,
            message = successMessage,
            containerColor = Color(0xFF4CAF50),
            onDismiss = { showSuccessBanner = false }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (step != 3) {
                Image(
                    painter = painterResource(id = R.drawable.ic_forgot),
                    contentDescription = null,
                    modifier = Modifier
                        .height(140.dp)
                        .wrapContentWidth(),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "ForgotPassStepAnim"
            ) { targetStep ->
                when (targetStep) {
                    1 -> {
                        // =====================================================
                        // STEP 1: INPUT IDENTIFIER (EMAIL / USERNAME)
                        // =====================================================
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Lupa Password?",
                                fontSize = 30.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Masukkan email atau username terdaftarmu. Kami akan mengirimkan kode OTP 6-digit untuk pemulihan akun.",
                                fontSize = 13.5.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp)),
                                color = Color.White,
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(26.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = identifier,
                                        onValueChange = {
                                            if (!it.contains(" ") && !it.contains("\n")) {
                                                identifier = it
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = "Email atau Username",
                                                fontFamily = interFontFamily,
                                                fontSize = 13.5.sp
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
                                        isError = isSubmitted && identifier.isBlank(),
                                        supportingText = {
                                            if (isSubmitted && identifier.isBlank()) {
                                                Text(
                                                    text = "Field tidak boleh kosong",
                                                    fontFamily = interFontFamily,
                                                    color = Color.Red,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        },
                                        singleLine = true,
                                        maxLines = 1,
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Done
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF1976D2),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color(0xFFF8F9FE),
                                            unfocusedContainerColor = Color(0xFFF8F9FE)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = {
                                            isSubmitted = true
                                            if (identifier.isBlank()) return@Button

                                            forgotPasswordViewModel.sendResetOtp(identifier.trim())
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1976D2),
                                            disabledContainerColor = Color(0xFF1976D2).copy(alpha = 0.85f),
                                            disabledContentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 0.dp,
                                            pressedElevation = 0.dp,
                                            disabledElevation = 0.dp
                                        ),
                                        enabled = identifier.isNotBlank() && state !is ForgotPasswordViewModel.ForgotPasswordState.Loading
                                    ) {
                                        if (state is ForgotPasswordViewModel.ForgotPasswordState.Loading) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                trackColor = Color.Transparent,
                                                modifier = Modifier.size(22.dp),
                                                strokeWidth = 2.5.dp
                                            )
                                        } else {
                                            Text(
                                                text = "KIRIM KODE OTP",
                                                fontFamily = interFontFamily,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Kembali ke Login",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = Color(0xFF1976D2),
                                        modifier = Modifier
                                            .padding(top = 4.dp)
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

                    2 -> {
                        // =====================================================
                        // STEP 2: INPUT KODE OTP & PASSWORD BARU
                        // =====================================================
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Reset Kata Sandi",
                                fontSize = 28.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Kode 6-digit telah dikirimkan ke email:",
                                fontSize = 13.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Email Badge dengan tombol Ubah
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = targetEmail,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Text(
                                        text = "Ubah",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFFFD54F),
                                        modifier = Modifier.clickable {
                                            step = 1
                                            otpInput = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            formErrorMessage = null
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp)),
                                color = Color.White,
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(26.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Label & Input OTP
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Kode OTP 6-Digit",
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = otpInput,
                                            onValueChange = { input ->
                                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                                    otpInput = input
                                                }
                                            },
                                            placeholder = {
                                                Text(
                                                    text = "• • • • • •",
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 20.sp,
                                                    letterSpacing = 8.sp
                                                )
                                            },
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(
                                                fontFamily = interFontFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 22.sp,
                                                textAlign = TextAlign.Center,
                                                letterSpacing = 8.sp,
                                                color = Color(0xFF0F172A)
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF1976D2),
                                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                                focusedContainerColor = Color(0xFFF8FAFC),
                                                unfocusedContainerColor = Color(0xFFF8FAFC)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(54.dp)
                                        )
                                    }

                                    // Input Password Baru
                                    OutlinedTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it },
                                        label = { Text("Password Baru", fontFamily = interFontFamily, fontSize = 13.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Lock, null, tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        maxLines = 1,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF1976D2),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color(0xFFF8F9FE),
                                            unfocusedContainerColor = Color(0xFFF8F9FE)
                                        )
                                    )

                                    // Input Konfirmasi Password
                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it },
                                        label = { Text("Konfirmasi Password", fontFamily = interFontFamily, fontSize = 13.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Lock, null, tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        maxLines = 1,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF1976D2),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color(0xFFF8F9FE),
                                            unfocusedContainerColor = Color(0xFFF8F9FE)
                                        )
                                    )

                                    if (formErrorMessage != null) {
                                        Text(
                                            text = formErrorMessage ?: "",
                                            fontFamily = interFontFamily,
                                            color = Color(0xFFDC2626),
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // Tombol Simpan Password Baru
                                    Button(
                                        onClick = {
                                            formErrorMessage = null
                                            if (otpInput.length != 6) {
                                                formErrorMessage = "Kode OTP harus 6 digit."
                                                return@Button
                                            }
                                            if (newPassword.length < 6) {
                                                formErrorMessage = "Kata sandi minimal 6 karakter."
                                                return@Button
                                            }
                                            if (newPassword != confirmPassword) {
                                                formErrorMessage = "Konfirmasi kata sandi tidak cocok."
                                                return@Button
                                            }

                                            forgotPasswordViewModel.verifyAndResetPassword(
                                                email = targetEmail,
                                                otpCode = otpInput.trim(),
                                                newPassword = newPassword
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1976D2),
                                            disabledContainerColor = Color(0xFF1976D2).copy(alpha = 0.85f),
                                            disabledContentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 0.dp,
                                            pressedElevation = 0.dp,
                                            disabledElevation = 0.dp
                                        ),
                                        enabled = otpInput.length == 6 &&
                                                newPassword.isNotBlank() &&
                                                confirmPassword.isNotBlank() &&
                                                state !is ForgotPasswordViewModel.ForgotPasswordState.Loading
                                    ) {
                                        if (state is ForgotPasswordViewModel.ForgotPasswordState.Loading) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                trackColor = Color.Transparent,
                                                modifier = Modifier.size(22.dp),
                                                strokeWidth = 2.5.dp
                                            )
                                        } else {
                                            Text(
                                                text = "SIMPAN PASSWORD BARU",
                                                fontFamily = interFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Kirim Ulang OTP row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (cooldownSeconds > 0) {
                                            Text(
                                                text = "Kirim ulang kode dalam ${cooldownSeconds}s",
                                                fontFamily = interFontFamily,
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        } else {
                                            Text(
                                                text = "Belum menerima kode? ",
                                                fontFamily = interFontFamily,
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = "Kirim Ulang",
                                                fontFamily = interFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF1976D2),
                                                modifier = Modifier.clickable {
                                                    forgotPasswordViewModel.sendResetOtp(targetEmail)
                                                }
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Batal",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .clickable {
                                                navController.navigate("login") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // =====================================================
                        // STEP 3: TAMPILAN SUKSES GANTI PASSWORD
                        // =====================================================
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White,
                            shadowElevation = 20.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "Password Diperbarui!",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Kata sandi akunmu berhasil diubah. Sekarang kamu dapat masuk ke Senimatika menggunakan kata sandi baru.",
                                    fontFamily = interFontFamily,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(26.dp))

                                Button(
                                    onClick = {
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF16A34A)
                                    )
                                ) {
                                    Text(
                                        text = "Masuk ke Akun",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
