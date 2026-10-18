package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.LambdaProject.MathArt.data.repository.AuthRepository
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EmailVerificationResultScreen(
    navController: NavController,
    oobCode: String
) {
    val authRepo = remember {
        AuthRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )
    }

    var isLoading by remember { mutableStateOf(true) }
    var isSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(oobCode) {
        isLoading = true
        errorMessage = null
        val result = authRepo.applyActionCode(oobCode)
        isLoading = false
        result.fold(
            onSuccess = {
                isSuccess = true
            },
            onFailure = { err ->
                isSuccess = false
                errorMessage = err.message ?: "Tautan verifikasi tidak valid atau sudah kedaluwarsa."
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
        // Lingkaran dekoratif
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
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(32.dp)),
                color = Color.White,
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF1976D2),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Memverifikasi email...",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                    } else if (isSuccess) {
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
                            text = "Email Terverifikasi! 🎉",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Selamat! Alamat email akunmu telah berhasil diverifikasi. Akun Senimatika kamu sekarang terlindungi dan siap digunakan secara penuh.",
                            fontFamily = interFontFamily,
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Button(
                            onClick = {
                                if (currentUser != null) {
                                    navController.navigate("dashboard/${currentUser.displayName ?: "User"}") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            )
                        ) {
                            Text(
                                text = if (currentUser != null) "Masuk ke Dashboard" else "Masuk ke Akun",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Verifikasi Gagal",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = errorMessage ?: "Tautan verifikasi ini mungkin sudah kedaluwarsa atau sudah pernah digunakan sebelumnya.",
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Button(
                            onClick = {
                                if (currentUser != null) {
                                    navController.navigate("profile") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
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
                                text = if (currentUser != null) "Buka Menu Profil" else "Kembali ke Login",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
