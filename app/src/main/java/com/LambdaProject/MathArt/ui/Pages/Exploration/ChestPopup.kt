package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    isReplay: Boolean,
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
                interactionSource = remember { MutableInteractionSource() },
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
                    CoinRewardView(if (isReplay) 0 else reward.amount)
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

            if (isReplay) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Replay Mode: No Reward Will Be Extracted",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
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
    var scrollContentReady by remember(content) { mutableStateOf(false) }

    val hasFormatting = content.contains("$") ||
            content.contains("**") ||
            content.contains("_") ||
            content.contains("![") ||
            content.contains("[") ||
            content.contains("* ")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
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
                .fillMaxWidth(0.57f)
                .fillMaxHeight(0.7f)
                .padding(horizontal = 5.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
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

            if (hasFormatting) {
                if (!scrollContentReady) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp), color = Color(0xFF3E2723))
                }
                MathText(
                    text = content,
                    color = Color(0xFF3E2723),
                    fontSize = 12,
                    textAlign = "left",
                    modifier = Modifier.fillMaxWidth(),
                    onRenderComplete = { scrollContentReady = true }
                )
            } else {
                Text(
                    text = content,
                    fontFamily = interFontFamily,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF3E2723),
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
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
            "Proteksi Streak dan HP Jika Menjawab Salah",
            Color(0xFF08FF27)
        )
        PowerUpType.HEALING_VIAL -> Quadruple(
            R.drawable.ic_pu_vial,
            "Healing Vial",
            "Menambah +20 Health Point",
            Color(0xFF08FF27)
        )
        PowerUpType.BINOCULAR -> Quadruple(
            R.drawable.ic_pu_binocular,
            "Binokular",
            "Memperluas area pandangan selama 5 detik",
            Color(0xFFFFF176)
        )
        PowerUpType.MAGIC_KEY -> Quadruple(
            R.drawable.ic_pu_key,
            "Magic Key",
            "Membuka Area Tanpa Passcode dan Kuis",
            Color(0xFFEFBF04)
        )
        PowerUpType.REMOVE_TWO_OPTIONS -> Quadruple(
            R.drawable.ic_pu_magic,
            "Truth Filter",
            "Menghapus 2 jawaban salah",
            Color(0xFFCE93D8)
        )
        PowerUpType.LEATHER_STRAPS -> Quadruple(
            R.drawable.ic_pu_belt,
            "Leather Straps",
            "Menambah kapasitas ransel",
            Color(0xFFEFBF04)
        )
        PowerUpType.TORCH -> Quadruple(
            R.drawable.ic_pu_torch,
            "Torch",
            "Menerangi area sekitar selama 5 detik",
            Color(0xFFEFBF04)
        )
        PowerUpType.LANTERN -> Quadruple(
            R.drawable.ic_pu_lantern,
            "Lantern",
            "Menerangi area sekitar",
            Color(0xFFEFBF04)
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
