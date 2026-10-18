package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.LambdaProject.MathArt.data.repository.AuthRepository
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    oobCode: String
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepo = remember {
        AuthRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )
    }

    var isVerifyingCode by remember { mutableStateOf(true) }
    var codeErrorMessage by remember { mutableStateOf<String?>(null) }
    var targetEmail by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var formErrorMessage by remember { mutableStateOf<String?>(null) }
    var isResetSuccess by remember { mutableStateOf(false) }

    // Validasi token oobCode saat pertama dibuka
    LaunchedEffect(oobCode) {
        isVerifyingCode = true
        codeErrorMessage = null
        val result = authRepo.verifyPasswordResetCode(oobCode)
        result.fold(
            onSuccess = { email ->
                targetEmail = email
                isVerifyingCode = false
            },
            onFailure = { error ->
                codeErrorMessage = error.message ?: "Tautan reset kata sandi tidak valid atau sudah kedaluwarsa."
                isVerifyingCode = false
            }
        )
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
        // Lingkaran dekorasi latar belakang
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isVerifyingCode) {
                // Tampilan Memverifikasi Token
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1976D2),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memeriksa tautan reset...",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            } else if (codeErrorMessage != null) {
                // Tampilan Token Expired / Invalid
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Tautan Kedaluwarsa",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = codeErrorMessage ?: "Tautan reset kata sandi ini sudah kedaluwarsa atau sudah pernah digunakan sebelumnya.",
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                navController.navigate("ForgotPassword") {
                                    popUpTo("login") { inclusive = false }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text(
                                text = "Kirim Ulang Link Reset",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            } else if (isResetSuccess) {
                // Tampilan Sukses Ganti Password
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Password Diperbarui!",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Kata sandi akunmu berhasil diubah. Silakan masuk kembali menggunakan kata sandi baru.",
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
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
            } else {
                // Form Buat Kata Sandi Baru
                Text(
                    text = "Kata Sandi Baru",
                    fontSize = 28.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Atur kata sandi baru untuk akun: $targetEmail",
                    fontSize = 13.5.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

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
                        // Input Password Baru
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = {
                                Text(
                                    text = "Password Baru",
                                    fontFamily = interFontFamily,
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(16.dp),
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
                            label = {
                                Text(
                                    text = "Konfirmasi Password Baru",
                                    fontFamily = interFontFamily,
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1976D2),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8F9FE),
                                unfocusedContainerColor = Color(0xFFF8F9FE)
                            )
                        )

                        // Pesan Error Form
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

                        Spacer(modifier = Modifier.height(4.dp))

                        // Tombol Simpan Password Baru
                        Button(
                            onClick = {
                                formErrorMessage = null
                                if (newPassword.length < 6) {
                                    formErrorMessage = "Kata sandi minimal 6 karakter."
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    formErrorMessage = "Konfirmasi kata sandi tidak cocok."
                                    return@Button
                                }

                                isSubmitting = true
                                coroutineScope.launch {
                                    val result = authRepo.confirmPasswordReset(oobCode, newPassword)
                                    isSubmitting = false
                                    result.fold(
                                        onSuccess = {
                                            isResetSuccess = true
                                        },
                                        onFailure = { err ->
                                            formErrorMessage = err.message ?: "Gagal memperbarui kata sandi."
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2),
                                disabledContainerColor = Color(0xFF1976D2).copy(alpha = 0.85f),
                                disabledContentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            ),
                            enabled = !isSubmitting && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    trackColor = Color.Transparent,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = "Simpan Password Baru",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "Batal",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier
                                .padding(top = 4.dp)
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
    }
}
