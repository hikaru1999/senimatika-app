package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.ViewModels.NotificationViewModel
import com.LambdaProject.MathArt.data.model.NotificationItem
import com.LambdaProject.MathArt.data.model.NotificationType
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val viewModel: NotificationViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isClearingAll by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.markAsSeen()
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Notifikasi",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            fontSize = 20.sp
                        )
                        if (notifications.isNotEmpty()) {
                            Surface(
                                color = Color(0xFFEEF2FF),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                            ) {
                                AnimatedContent(
                                    targetState = notifications.size,
                                    transitionSpec = {
                                        slideInVertically { it } + fadeIn() togetherWith
                                                slideOutVertically { -it } + fadeOut()
                                    },
                                    label = "notifBadgeCount"
                                ) { count ->
                                    Text(
                                        text = "$count",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                if (!isClearingAll) {
                                    isClearingAll = true
                                    coroutineScope.launch {
                                        val count = notifications.size
                                        val totalAnimDuration = (count.coerceAtMost(6) * 28L + 200L)
                                        delay(totalAnimDuration)
                                        viewModel.markAllAsRead()
                                        isClearingAll = false
                                    }
                                }
                            },
                            enabled = !isClearingAll
                        ) {
                            Text(
                                text = "Bersihkan Semua",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isClearingAll) Color(0xFF94A3B8) else Color(0xFFDC2626)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = when {
                    isLoading -> 0
                    notifications.isEmpty() -> 1
                    else -> 2
                },
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "NotificationScreenState"
            ) { screenState ->
                when (screenState) {
                    0 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF4F46E5),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEEF2FF),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum Ada Notifikasi",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pemberitahuan lencana terbuka dan progres level akan muncul di sini.",
                                fontFamily = interFontFamily,
                                fontSize = 12.5.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                    2 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(notifications, key = { _, notification -> notification.id }) { index, notification ->
                                NotificationSwipeItem(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        placementSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        fadeOutSpec = tween(180)
                                    ),
                                    index = index,
                                    notification = notification,
                                    isClearingAll = isClearingAll,
                                    onDelete = { viewModel.deleteNotification(notification) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSwipeItem(
    modifier: Modifier = Modifier,
    index: Int = 0,
    notification: NotificationItem,
    isClearingAll: Boolean = false,
    onDelete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var itemWidthPx by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    var isDeleting by remember { mutableStateOf(false) }

    // Fast sweep animation states when "Bersihkan Semua" is clicked
    val clearOffsetX = remember { Animatable(0f) }
    val clearAlpha = remember { Animatable(1f) }

    LaunchedEffect(isClearingAll) {
        if (isClearingAll) {
            val staggerDelay = (index.coerceAtMost(6) * 28L)
            delay(staggerDelay)
            launch {
                clearOffsetX.animateTo(
                    targetValue = if (itemWidthPx > 0f) itemWidthPx * 1.15f else 1200f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                )
            }
            launch {
                clearAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 160)
                )
            }
        } else {
            clearOffsetX.snapTo(0f)
            clearAlpha.snapTo(1f)
        }
    }

    // Banner fades out smoothly when item is being deleted or during clear all
    val bannerAlpha by animateFloatAsState(
        targetValue = if (isDeleting || isClearingAll) 0f else 1f,
        animationSpec = tween(150),
        label = "bannerAlpha"
    )

    // Minimal geser 3/4 (75%) lebar kartu untuk memicu hapus
    val threshold = if (itemWidthPx > 0f) itemWidthPx * 0.75f else 600f
    val isPastThreshold = -offsetX.value >= threshold

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { itemWidthPx = it.width.toFloat() }
    ) {
        // Background Delete Action Banner
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = bannerAlpha }
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = if (isPastThreshold) {
                            listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                        } else {
                            listOf(
                                Color(0xFFEF4444).copy(alpha = 0.65f),
                                Color(0xFFDC2626).copy(alpha = 0.85f)
                            )
                        }
                    )
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isPastThreshold) "Lepas untuk menghapus" else "Geser untuk menghapus",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Foreground Card with Weighted Pointer Drag & Sweep Exit
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        (offsetX.value + clearOffsetX.value).roundToInt(),
                        0
                    )
                }
                .graphicsLayer {
                    alpha = clearAlpha.value
                }
                .pointerInput(itemWidthPx, isClearingAll, isDeleting) {
                    if (isClearingAll || isDeleting) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (-offsetX.value >= threshold) {
                                    isDeleting = true
                                    // Animate full exit to left and delete
                                    offsetX.animateTo(-itemWidthPx, tween(180, easing = FastOutLinearInEasing))
                                    onDelete()
                                } else {
                                    // Snap back smoothly with natural spring physics
                                    offsetX.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                offsetX.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (isClearingAll || isDeleting) return@detectHorizontalDragGestures
                            change.consume()
                            coroutineScope.launch {
                                // Weighted drag factor (0.85f) for natural tactile feedback
                                val weightedDrag = dragAmount * 0.85f
                                val newOffset = (offsetX.value + weightedDrag).coerceIn(-itemWidthPx, 0f)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                }
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0xFF1E293B).copy(alpha = 0.12f)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Avatar
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, Color(0xFFE0E7FF))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = notification.iconResId),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        fontSize = 15.5.sp,
                        color = Color(0xFF0F172A),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = notification.message,
                        fontWeight = FontWeight.Normal,
                        fontFamily = interFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tag Badge
                        val (tagText, tagColor, tagBgColor) = remember(notification.type) {
                            when (notification.type) {
                                NotificationType.ACHIEVEMENT ->
                                    Triple("🏆 LENCANA", Color(0xFF15803D), Color(0xFFDCFCE7))
                                NotificationType.LEVEL_UP ->
                                    Triple("⭐ LEVEL UP", Color(0xFFB45309), Color(0xFFFEF3C7))
                            }
                        }

                        Surface(
                            color = tagBgColor,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, tagColor.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = tagText,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = tagColor,
                                letterSpacing = 0.3.sp
                            )
                        }

                        // Timestamp
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                    .format(Date(notification.timestamp)),
                                fontSize = 11.sp,
                                fontFamily = interFontFamily,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}



