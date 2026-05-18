package com.LambdaProject.MathArt

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.DataAchievements
import com.LambdaProject.MathArt.data.model.AchievementItem

@Composable
fun AchievementSnackbar(item: AchievementItem?, visible: Boolean) {
    val context = LocalContext.current
    var lastItem by remember { mutableStateOf<AchievementItem?>(null) }

    LaunchedEffect(item) {
        if (item != null) {
            lastItem = item
        }
    }

    val dynamicResId = remember(lastItem) {
        lastItem?.let { achievement ->
            val match = DataAchievements.find { it.name == achievement.name }
            match?.imageRes ?: R.drawable.obj_sack
        } ?: R.drawable.obj_sack
    }

    AnimatedVisibility(
        visible = visible && lastItem != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        lastItem?.let { badge ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .shadow(12.dp, CircleShape),
                color = Color(0xFFFFF8E1),
                shape = CircleShape,
                border = BorderStroke(1.5.dp,
                    Color(0xFFFFB300).copy(alpha = 0.4f))
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
                        Image(
                            painter = painterResource(id = dynamicResId),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Achievement Unlocked!", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(badge.name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A237E))
                    }
                }
            }
        }
    }
}