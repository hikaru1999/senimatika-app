package com.LambdaProject.MathArt.ui.Screen

import com.LambdaProject.MathArt.helveticaFont
import com.LambdaProject.MathArt.model.NotificationItem
import com.LambdaProject.MathArt.model.getAchievementIcon

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.navigation.NavController

import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val isLoading = remember { mutableStateOf(true) }
    val notificationsState = remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    LaunchedEffect(userId) {
        Firebase.firestore.collection("userAchievements")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationScreen", "Firestore error: ${error.message}")
                    isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("NotificationScreen", "Snapshot received: ${snapshot.documents.size} documents")
                    val notifList = snapshot.documents.mapNotNull { doc ->
                        val title = doc.getString("achievementName") ?: return@mapNotNull null
                        val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                        NotificationItem(
                            title = title,
                            message = "Hore!! Ada Lencana Baru Terbuka!",
                            timestamp = timestamp,
                            iconResId = getAchievementIcon(title)
                        )
                    }
                    notificationsState.value = notifList
                }
                isLoading.value = false
            }
    }

    Scaffold(
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
                }
            )
        }
    ) { padding ->
        when {
            isLoading.value -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            notificationsState.value.isEmpty() -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada notifikasi",
                        fontFamily = helveticaFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            else -> {
                LazyColumn(contentPadding = padding) {
                    items(notificationsState.value) { notification ->
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
