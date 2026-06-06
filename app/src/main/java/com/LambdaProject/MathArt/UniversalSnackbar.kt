package com.LambdaProject.MathArt

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.DataAchievements
import com.LambdaProject.MathArt.data.model.AchievementItem

//import androidx.compose.ui.tooling.preview.Preview
//
//
//@Preview(showBackground = true, name = "Mode Achievement")
//@Composable
//fun PreviewAchievementSnackbar() {
//    Box(modifier = Modifier.fillMaxSize()) {
//        UniversalSnackbar(
//            achievementItem = AchievementItem(
//                name = "Pakar Geometri",
//                isUnlocked = true
//            ),
//            visible = true
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Mode Network Lost")
//@Composable
//fun PreviewNetworkLostSnackbar() {
//    Box(modifier = Modifier.fillMaxSize()) {
//        UniversalSnackbar(
//            isNetworkLost = true,
//            remainingSeconds = 12,
//            visible = true
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Gallery Universal Snackbar")
//@Composable
//fun PreviewSnackbarGallery() {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(10.dp),
//        verticalArrangement = Arrangement.spacedBy(80.dp) // Jarak agar tidak bertumpuk karena AnimatedVisibility
//    ) {
//        // Karena UniversalSnackbar aslinya menggunakan statusBarsPadding(),
//        // di Gallery ini mungkin akan terlihat sangat turun.
//        // Kita tampilkan statis untuk perbandingan visual.
//
//        Text("1. Achievement Mode", fontWeight = FontWeight.Bold)
//        UniversalSnackbar(
//            achievementItem = AchievementItem(name = "Kolektor Artefak"),
//            visible = true
//        )
//
//        Text("2. Network Lost Mode", fontWeight = FontWeight.Bold)
//        UniversalSnackbar(
//            isNetworkLost = true,
//            remainingSeconds = 15,
//            visible = true
//        )
//
//        Text("3. Reconnect Success Mode", fontWeight = FontWeight.Bold)
//        UniversalSnackbar(
//            isSuccess = true,
//            visible = true
//        )
//    }
//}
@Composable
fun UniversalSnackbar(
    achievementItem: AchievementItem? = null,
    isNetworkLost: Boolean = false,
    isSuccess: Boolean = false,
    remainingSeconds: Int = 0,
    visible: Boolean
) {
//    val context = LocalContext.current
    var lastAchievement by remember { mutableStateOf<AchievementItem?>(null) }
    val isAchievementMode = achievementItem != null || (lastAchievement != null && !isNetworkLost && !isSuccess)

    LaunchedEffect(achievementItem) {
        if (achievementItem != null) {
            lastAchievement = achievementItem
        }
    }

    val backgroundColor = when {
        isNetworkLost -> Color(0xFFB7190B)
        isSuccess -> Color(0xFF2E7D32)
        isAchievementMode -> Color(0xFFFFCF3D)
        else -> Color.Gray
    }

    val borderColor = when {
        isNetworkLost -> Color(0xFFEF5350).copy(alpha = 0.6f)
        isSuccess -> Color(0xFF81C784).copy(alpha = 0.4f)
        isAchievementMode -> Color(0xFFFFB300).copy(alpha = 0.4f)
        else -> Color.Transparent
    }

    val dynamicResId = remember(lastAchievement) {
        lastAchievement?.let { ach ->
//            val match = DataAchievements.find { it.name == achievement.name }
//            match?.imageRes ?: R.drawable.obj_sack
            DataAchievements.find { it.name == ach.name }?.imageRes
        } ?: R.drawable.obj_sack
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .zIndex(1000f)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .shadow(12.dp, CircleShape),
            color = backgroundColor,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, borderColor)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isNetworkLost -> {
                            Icon(Icons.Default.WifiOff, null, tint = Color.Red, modifier = Modifier.size(24.dp))
                        }
                        isSuccess -> {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF388E3C), modifier = Modifier.size(24.dp))
                        }
                        isAchievementMode -> {
                            Image(painter = painterResource(id = dynamicResId), null, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    if (isNetworkLost) {
                        Text(
                            text = "Ups, koneksi internet terputus!",
                            fontSize = 16.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Reconnecting... (${remainingSeconds}s)",
                            fontSize = 10.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    } else if (isSuccess){
                        Text("Connected!", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.8f))
                        Text("Berhasil terhubung kembali", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                    } else if (isAchievementMode) {
                        Text(
                            text = "Achievement Unlocked!",
                            fontSize = 10.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD50000)
                        )
                        Text(
                            text = lastAchievement?.name ?: "",
                            fontSize = 16.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF020959)
                        )
                    }
                }
            }
        }
    }
}