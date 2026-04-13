package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.util.Log
import com.LambdaProject.MathArt.helveticaFont

import androidx.compose.foundation.Image

/* import androidx.compose.foundation.background
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import okhttp3.internal.wait */

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.ViewModels.NotificationViewModel
import com.LambdaProject.MathArt.data.model.NotificationType
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.PvPChallengeCard

import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val viewModel: NotificationViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifikasi",
                        fontFamily = helveticaFont,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            notifications.isEmpty() -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada notifikasi",
                        fontFamily = helveticaFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            else -> {
                LazyColumn(contentPadding = padding) {
                    items(notifications) { notification ->
                        if (notification.type == NotificationType.PVP_CHALLENGE) {
                            PvPChallengeCard(
                                notification = notification,
                                onAccept = {
                                    // TODO: jalankan logika menerima tantangan
                                    Log.d("Notif", "Tantangan diterima: ${notification.challengeId}")
                                },
                                onDecline = {
                                    // TODO: update challenge jadi "declined" atau hilangkan notifikasi
                                    Log.d("Notif", "Tantangan ditolak / timeout")
                                }
                            )
                        } else {
                            /* var dismissed by remember { mutableStateOf(false) }
                            val dismissState = rememberDismissState()

                            if (!dismissed) {
                                if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                                    LaunchedEffect(Unit) {
                                        dismissed = true
                                        viewModel.deleteNotification(notification)
                                    }
                                }

                                SwipeToDismiss(
                                    state = dismissState,
                                    directions = setOf(DismissDirection.EndToStart),
                                    background = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    dismissContent = {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFf7f7f7))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = notification.message,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = helveticaFont,
                                                        fontSize = 12.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = notification.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = helveticaFont,
                                                        fontSize = 21.sp,
                                                        color = Color(0xff78DF4F)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = SimpleDateFormat(
                                                            "dd MMM yyyy, HH:mm",
                                                            Locale.getDefault()
                                                        ).format(Date(notification.timestamp)),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }

                                                Image(
                                                    painter = painterResource(id = notification.iconResId),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .padding(start = 12.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            } */
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFf7f7f7))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notification.message,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = helveticaFont,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notification.title,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = helveticaFont,
                                            fontSize = 21.sp,
                                            color = Color(0xff78DF4F)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = SimpleDateFormat(
                                                "dd MMM yyyy, HH:mm", Locale.getDefault()
                                            ).format(Date(notification.timestamp)),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Image(
                                        painter = painterResource(id = notification.iconResId),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(start = 12.dp)
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

