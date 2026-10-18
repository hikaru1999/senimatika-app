package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.data.service.EmailService
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmailVerificationModal(
    isEmailVerified: Boolean,
    email: String,
    onResendEmail: (onResult: (Boolean, String) -> Unit) -> Unit = {},
    onCheckStatus: (onResult: (Boolean) -> Unit) -> Unit = {},
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var internalVerified by remember { mutableStateOf(false) }
    val effectiveVerified = isEmailVerified || internalVerified

    var isSending by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var cooldownSeconds by remember { mutableIntStateOf(0) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds -= 1
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp)),
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (effectiveVerified) Color(0xFFE0F2FE) else Color(0xFFEFF6FF),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (effectiveVerified) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF1D9BF0),
                                        modifier = Modifier.size(19.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (effectiveVerified) "Email Terverifikasi" else "Verifikasi Email",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (effectiveVerified) {
                    // --- TAMPILAN TERVERIFIKASI (1 TEMA DENGAN VERIFIED BADGE) ---
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.size(84.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Lapisan dasar putih agar centang cutout tetap putih pekat
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Terverifikasi",
                                tint = Color(0xFF1D9BF0),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Akun Kamu Terverifikasi!",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Alamat email $email telah terverifikasi secara resmi. Akunmu kini aman dan siap digunakan.",
                        fontFamily = interFontFamily,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D9BF0)
                        )
                    ) {
                        Text(
                            text = "Tutup",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                } else {
                    // --- TAMPILAN OTP (SEDERHANA & BERSIH) ---
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Masukkan Kode OTP",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Kode 6-digit telah dikirimkan ke alamat email:",
                        fontFamily = interFontFamily,
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Email Pill Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = email.ifEmpty { "Email akun kamu" },
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF1E293B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }

                    // Status Message Banner (Animated)
                    AnimatedVisibility(
                        visible = statusMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        statusMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isStatusError) Color(0xFFFEE2E2) else Color(0xFFE0F2FE),
                                border = BorderStroke(
                                    1.dp,
                                    if (isStatusError) Color(0xFFFCA5A5) else Color(0xFFBAE6FD)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    fontFamily = interFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isStatusError) Color(0xFF991B1B) else Color(0xFF0369A1),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Input Field OTP 6-Digit
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
                        maxLines = 1,
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
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tombol Verifikasi Tunggal (Primary Action)
                    Button(
                        onClick = {
                            if (otpInput.length != 6 || isVerifyingOtp) return@Button
                            isVerifyingOtp = true
                            statusMessage = null
                            coroutineScope.launch {
                                val result = EmailService.verifyEmailOtp(userId, otpInput, email)
                                isVerifyingOtp = false
                                result.fold(
                                    onSuccess = {
                                        internalVerified = true
                                        statusMessage = "Selamat! Email berhasil diverifikasi."
                                        isStatusError = false
                                        onCheckStatus { /* update parent state */ }
                                    },
                                    onFailure = { err ->
                                        statusMessage = err.message ?: "Kode verifikasi tidak cocok."
                                        isStatusError = true
                                    }
                                )
                            }
                        },
                        enabled = otpInput.length == 6 && !isVerifyingOtp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = if (isVerifyingOtp) Color(0xFF2563EB).copy(alpha = 0.85f) else Color(0xFFE2E8F0),
                            disabledContentColor = if (isVerifyingOtp) Color.White else Color(0xFF94A3B8)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (isVerifyingOtp) {
                            CircularProgressIndicator(
                                color = Color.White,
                                trackColor = Color.Transparent,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Verifikasi Kode",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Aksi Kirim Ulang OTP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                color = Color(0xFF2563EB),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mengirim kode baru...",
                                fontFamily = interFontFamily,
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B)
                            )
                        } else if (cooldownSeconds > 0) {
                            Text(
                                text = "Kirim ulang kode dalam ${cooldownSeconds} detik",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            Text(
                                text = "Belum menerima kode? ",
                                fontFamily = interFontFamily,
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Kirim Ulang",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.clickable {
                                    isSending = true
                                    statusMessage = null
                                    onResendEmail { success, msg ->
                                        isSending = false
                                        statusMessage = msg
                                        isStatusError = !success
                                        if (success) {
                                            cooldownSeconds = 60
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Cek folder Spam jika kode tidak muncul di Kotak Masuk.",
                        fontFamily = interFontFamily,
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
