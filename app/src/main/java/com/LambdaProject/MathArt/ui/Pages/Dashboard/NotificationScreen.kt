package com.LambdaProject.MathArt.ui.Pages.Dashboard

import com.LambdaProject.MathArt.helveticaFont
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.ViewModels.NotificationViewModel
import com.LambdaProject.MathArt.data.model.NotificationItem
import com.LambdaProject.MathArt.interFontFamily

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
        if (isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(), contentAlignment = Alignment.Center) {
                Text("Tidak ada notifikasi", fontFamily = interFontFamily, fontWeight = FontWeight.Medium)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationSwipeItem(
                        notification = notification,
                        onDelete = { viewModel.deleteNotification(notification) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSwipeItem(
    notification: NotificationItem,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val isDismissing = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val backgroundColor = if (isDismissing) Color(0xFFFFEBEE) else Color.Transparent
            val iconTint = if (isDismissing) Color(0xFFD32F2F) else Color.Transparent

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp) // Sesuai padding Card
                    .background(
                        backgroundColor,
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDismissing) {
                        Text(
                            "Hapus",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = iconTint,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color(0xFFF0F4FF)
                ) {
                    Image(
                        painter = painterResource(id = notification.iconResId),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.message,
                        fontWeight = FontWeight.Medium,
                        fontFamily = interFontFamily,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Black,
                        fontFamily = helveticaFont,
                        fontSize = 18.sp,
                        color = Color(0xFF1A237E),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF78DF4F).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LENCANA",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                .format(Date(notification.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
