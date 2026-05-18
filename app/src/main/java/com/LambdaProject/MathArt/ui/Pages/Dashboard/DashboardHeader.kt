package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DashboardHeader(
    navController: NavController,
    userName: String,
    viewModel: DashboardViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return
    val username by viewModel.username.collectAsState()
    val hasNewNotification by viewModel.hasNewNotification.observeAsState(false)

    LaunchedEffect(userId) {
        viewModel.listenForNewNotifications(userId)
    }

    // Mengambil inisial nama untuk Avatar
    val initial = if (username.isNotEmpty()) username.take(1).uppercase() else "U"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFADD6FF),
                        Color(0xFFF7FAFF),
                        /* Color(0xFFF7FAFF)*/
                    )
                )
            )
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Profil Avatar Bulat dengan Gradient
            /* Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily
                )
            }

            Spacer(modifier = Modifier.width(12.dp)) */

            // Teks Sapaan
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Halo, $username!",
                    fontFamily = interFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A237E)
                )
                Text(
                    text = "Apa yang ingin kamu pelajari?",
                    fontFamily = interFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    letterSpacing = 0.2.sp
                )
            }

            // Tombol Notifikasi dengan Efek Kaca/Elevasi Ringan
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    onClick = {
                        viewModel.clearNotifications()
                        navController.navigate("notification")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = Color.LightGray)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bell),
                            contentDescription = "Notifikasi",
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFF1A237E)
                        )
                    }
                }

                // Dot Indikator Notifikasi yang lebih "hidup"
                if (hasNewNotification) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = 2.dp, y = (-2).dp)
                            .background(Color.White, CircleShape)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
            }
        }
    }
}