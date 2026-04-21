package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.*
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.ExplorationAudioManager
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText

@Composable
fun ChestPopup(
    reward: Reward,
    onCollect: () -> Unit,
    audio: ExplorationAudioManager
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        when (reward.type) {
            RewardType.COIN -> audio.playSfx("coin")
            RewardType.SCROLL -> audio.playSfx("scroll")
            RewardType.POWER_UP -> audio.playSfx("powerup")
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val isScroll = reward.type == RewardType.SCROLL

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource () },
                indication = null
            ) {

            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .width(if (isScroll) 380.dp else 320.dp)
                .clickable(enabled = false) { },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (reward.type) {
                RewardType.COIN -> {
                    CoinRewardView(reward.amount)
                    Spacer(modifier = Modifier.height(24.dp))
                    ActionButton(onCollect)
                }
                RewardType.SCROLL -> {
                    ScrollRewardView(reward.title, reward.content)
                    ActionButton(onCollect)
                }
                RewardType.POWER_UP -> {
                    PowerUpRewardView(reward.powerUp)
                    Spacer(modifier = Modifier.height(24.dp))
                    ActionButton(onCollect)
                }
            }
        }
    }
}

@Composable
fun CoinRewardView(amount: Int) {
    val animatedAmount by animateIntAsState(
        targetValue = amount,
        animationSpec = tween(1000, easing = LinearOutSlowInEasing),
        label = "coin"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Kamu menemukan ...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFBF00)
        )

        Spacer(modifier = Modifier.height(64.dp))

        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            SunRayEffect(modifier = Modifier.fillMaxSize())
            Image(
                painter = painterResource(R.drawable.ic_stack_coins),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "+ $animatedAmount",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD600)
        )
        Text(
            text = "Koin Emas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFFFD600)
        )
    }
}

@Composable
fun ScrollRewardView(title: String, content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_scroll_open),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                /*fillMaxSize()*/
                .fillMaxWidth(0.65f)
                .fillMaxHeight(0.7f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.ifEmpty { "MATERI BARU" }.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA10000).copy(alpha = 0.8f),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (content.contains("$")) {
                MathText(
                    text = content,
                    color = Color(0xFF3E2723),
                    fontSize = 14,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = content,
                    fontFamily = interFontFamily,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF3E2723),
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PowerUpRewardView(powerUp: PowerUpType?) {
    val (iconRes, title, desc, rayColor) = when (powerUp) {
        PowerUpType.FREEZE_TIMER -> Quadruple(
            R.drawable.ic_pu_freeze,
            "Chrono Freeze",
            "Menghentikan timer kuis selama 5 detik",
            Color(0xFF81D4FA)
        )
        PowerUpType.STREAK_PROTECTION -> Quadruple(
            R.drawable.ic_pu_shield,
            "Battle Shield",
            "Proteksi Streak Jika Menjawab Soal Salah",
            Color(0xFF08FF27)
        )
        PowerUpType.REMOVE_TWO_OPTIONS -> Quadruple(
            R.drawable.ic_pu_magic,
            "Truth Filter",
            "Menghapus 2 jawaban salah",
            Color(0xFFCE93D8)
        )
        null -> Quadruple(R.drawable.ic_assignment, "Unknown", "", Color.White)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Kamu menemukan ...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFBF00)
        )

        Spacer(modifier = Modifier.height(64.dp))

        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            SunRayEffect(
                modifier = Modifier.fillMaxSize(),
                rayColor = rayColor
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = Color(0xFF455A64)
        )
    }
}

@Composable
fun ActionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(64.dp)
            .width(160.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ui_button_action),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
