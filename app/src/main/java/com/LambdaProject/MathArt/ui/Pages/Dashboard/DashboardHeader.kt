package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun DashboardHeader(navController: NavController, userName: String, viewModel: DashboardViewModel = viewModel()) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return
    val context = LocalContext.current
    val username by viewModel.username.collectAsState()

    /* val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val savedUserName = sharedPreferences.getString("USERNAME_KEY", userName) */

    val hasNewNotification by viewModel.hasNewNotification.observeAsState(false)

    LaunchedEffect(userId) {
        viewModel.listenForNewNotifications(userId)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xffF7FAFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Halo, $username!",
                    fontFamily = interFontFamily,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Mau belajar apa hari ini?",
                    fontFamily = interFontFamily,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Box (
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = {
                        viewModel.clearNotifications()
                        navController.navigate("notification")
                    }
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_bell),
                        contentDescription = "Notifikasi",
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (hasNewNotification) {
                    Badge(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = (-15).dp, y = 14.dp),
                        containerColor = Color.Red
                    )
                }
            }
        }
    }
}

