package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.helveticaFont
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.NotificationItem
import kotlinx.coroutines.delay

@Composable
fun PvPChallengeCard(
    notification: NotificationItem,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    timeoutMillis: Long = 30000,
) {
    var timeLeft by remember { mutableStateOf(timeoutMillis / 1000) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(notification.timestamp) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft <= 0) {
            onDecline()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFf7f7f7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = notification.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = notification.message,
                        fontWeight = FontWeight.Bold,
                        fontFamily = helveticaFont,
                        fontSize = 12.sp
                    )
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontFamily = helveticaFont,
                        fontSize = 21.sp,
                        color = Color(0xff78DF4F)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("Terima", fontFamily = interFontFamily, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onDecline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("Tidak ($timeLeft)", fontFamily = interFontFamily, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}