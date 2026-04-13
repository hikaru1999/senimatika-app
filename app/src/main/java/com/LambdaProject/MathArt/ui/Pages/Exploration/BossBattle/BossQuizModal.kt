package com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.ViewModels.BossQuizViewModel
import com.LambdaProject.MathArt.ViewModels.BossBattlePhase
import com.LambdaProject.MathArt.ViewModels.QuestionResult
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ui.components.MathText
import com.LambdaProject.MathArt.ui.Pages.Exploration.SunRayEffect
import kotlinx.coroutines.delay

@Composable
fun BossQuizModal(
    viewModel: BossQuizViewModel,
    inventory: Inventory,
    onUpdateInventory: (Inventory) -> Unit,
    onFinish: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        when (viewModel.phase) {
            BossBattlePhase.INTRO -> BattleIntroAnimation()
            BossBattlePhase.COUNTDOWN -> CountdownAnimation(viewModel.countdownValue)
            BossBattlePhase.QUIZ -> QuizContent(viewModel, inventory, onUpdateInventory)
            BossBattlePhase.SUMMARY -> QuizSummaryAnimated(viewModel, onFinish)
            else -> {}
        }
    }
}

@Composable
fun QuizContent(
    viewModel: BossQuizViewModel,
    inventory: Inventory,
    onUpdateInventory: (Inventory) -> Unit
) {
    val question = viewModel.currentQuestion ?: return
    var isInventoryModalVisible by remember { mutableStateOf(false) }

    // Screen Shake Animation for Boss
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )
    val bossModifier = if (viewModel.bossShake) Modifier.offset(x = shakeOffset.dp) else Modifier

    Surface(
        modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.95f),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1A1A1A),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box {
            // Visual Impact: SunRayEffect for Player Correct
            if (viewModel.showPlayerImpact) {
                SunRayEffect(modifier = Modifier.align(Alignment.TopStart).size(200.dp), rayColor = Color.Cyan)
            }
            if (viewModel.showBossImpact) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.3f)))
            }

            Column {
                // 5.2 Alur Visual: Layar terbagi dua (Atas: HP Bars)
                BattleHeader(
                    playerHp = viewModel.playerHp,
                    bossHp = viewModel.bossHp,
                    bossProgress = viewModel.bossThinkingProgress,
                    bossTimeLeftMillis = viewModel.bossTimeLeftMillis,
                    modifier = bossModifier
                )

                Text(
                    text = "Question ${viewModel.currentQuestionIndex + 1}/${viewModel.totalQuestions}",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 11.sp
                )

                // 5.2 Alur Visual: Tengah (Soal)
                Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .wrapContentHeight(),
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center, 
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (question.question.contains("$")) {
                                MathText(text = question.question, color = Color.White, fontSize = 13)
                            } else {
                                Text(question.question, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        question.options.forEach { option ->
                            if (!viewModel.removedOptions.contains(option)) {
                                val isSelected = viewModel.selectedAnswers.contains(option)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .pointerInteropFilter {
                                            viewModel.toggleAnswer(option)
                                            true
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFFD32F2F) else Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(2.dp, if (isSelected) Color(0xFFFFD600) else Color.Transparent)
                                ) {
                                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
                                        if (option.contains("$")) {
                                            MathText(text = option, color = Color.White, fontSize = 13)
                                        } else {
                                            Text(option, color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5.2 Alur Visual: Bawah (Controls)
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tombol Inventory (Backpack)
                        Surface(
                            onClick = { isInventoryModalVisible = true },
                            modifier = Modifier.size(36.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_backpack),
                                    contentDescription = "Inventory",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Tombol Attack
                        Button(
                            onClick = { viewModel.submitAnswer() },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            enabled = viewModel.selectedAnswers.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ATTACK!", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    if (isInventoryModalVisible) {
        QuizBagModal(
            inventory = inventory,
            onClose = { isInventoryModalVisible = false },
            onUsePowerUp = { pu: PowerUpType ->
                viewModel.usePowerUp(pu, inventory, onUpdateInventory)
                isInventoryModalVisible = false
            }
        )
    }
}

@Composable
fun ResultBattleItem(index: Int, result: QuestionResult) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, if (result.isCorrect) Color.Cyan.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Soal $index", color = Color.White, fontWeight = FontWeight.Bold)
                if (result.wasFast && result.isCorrect) {
                    Text("CRITICAL!", color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Boss Damage: -${result.playerDamageDealt.toInt()} HP", color = Color.Red, fontSize = 12.sp)
                Text("Player Damage: -${result.playerDamageTaken.toInt()} HP", color = Color.Cyan, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BattleHpSummary(playerHp: Float, bossHp: Float) {
    val bossDefeated = bossHp <= 0f
    val bossSurrendered = playerHp > bossHp && bossHp > 0f
    
    val statusText = when {
        bossDefeated -> "BOSS DIKALAHKAN!"
        bossSurrendered -> "BOSS MENYERAH!"
        else -> "BATTLE ENDED"
    }
    
    val statusColor = when {
        bossDefeated || bossSurrendered -> Color.Green
        else -> Color.Yellow
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(statusText, color = statusColor, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScoreColumn("SISA HP PLAYER", playerHp.toInt(), Color.Cyan)
            ScoreColumn("SISA HP BOSS", bossHp.toInt(), Color.Red)
        }
    }
}

@Composable
fun ScoreColumn(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(score.toString(), color = color, fontSize = 36.sp, fontWeight = FontWeight.Black)
    }
}
