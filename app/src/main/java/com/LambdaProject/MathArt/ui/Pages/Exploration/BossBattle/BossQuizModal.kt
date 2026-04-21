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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.LambdaProject.MathArt.data.model.ExplorationAudioManager
import com.LambdaProject.MathArt.ui.Pages.Exploration.BagModal
import com.LambdaProject.MathArt.ui.components.MathText
import com.LambdaProject.MathArt.ui.Pages.Exploration.SunRayEffect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BossQuizModal(
    viewModel: BossQuizViewModel,
    inventory: Inventory,
    onUpdateInventory: (Inventory) -> Unit,
    onFinish: (Boolean) -> Unit,
    audio: ExplorationAudioManager
) {
    var isFinishingByAttack by remember { mutableStateOf(false) }
    LaunchedEffect(
        viewModel.bossTimeLeftMillis,
        viewModel.phase,
        viewModel.isChronoFreezeActive,
        isFinishingByAttack
    ) {
        val isTimeCritical = viewModel.bossTimeLeftMillis in 1..4000

        if (viewModel.phase == BossBattlePhase.QUIZ &&
            isTimeCritical &&
            !viewModel.isChronoFreezeActive &&
            !isFinishingByAttack // KUNCI: Jangan play jika sedang menyerang
        ) {
            audio.playIntenseWarning(R.raw.timer_intense)
        } else {
            audio.stopIntenseWarning()
        }

        when (viewModel.phase) {
            BossBattlePhase.INTRO -> {
                delay(1500)
                audio.playBGM(R.raw.sfx_quiz_bgm)
            }
            BossBattlePhase.SUMMARY -> {
                audio.stopBGMWithFade(duration = 1500)
                val isWin = viewModel.bossHp <= 0f || (viewModel.playerHp > viewModel.bossHp && viewModel.playerHp > 0)
                if (isWin) {
                    delay(500)
                    audio.playSfx("quizVictory")
                } else {
                    audio.playSfx("quizFailed")
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        when (viewModel.phase) {
            BossBattlePhase.INTRO -> BattleIntroAnimation(bossType = viewModel.currentBossType)
            BossBattlePhase.COUNTDOWN -> CountdownAnimation(viewModel.countdownValue)
            BossBattlePhase.QUIZ -> QuizContent(viewModel, inventory, onUpdateInventory, audio)
            BossBattlePhase.SUMMARY -> QuizSummaryAnimated(viewModel, onFinish, audio)
            else -> {}
        }
    }
}

@Composable
fun QuizContent(
    viewModel: BossQuizViewModel,
    inventory: Inventory,
    onUpdateInventory: (Inventory) -> Unit,
    audio: ExplorationAudioManager
) {
    val question = viewModel.currentQuestion ?: return
    var isInventoryModalVisible by remember { mutableStateOf(false) }
    var isFinishingByAttack by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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

    LaunchedEffect(viewModel.bossTimeLeftMillis, viewModel.phase, viewModel.isChronoFreezeActive) {
        val isTimeCritical = viewModel.bossTimeLeftMillis in 1..4000

        if (viewModel.phase == BossBattlePhase.QUIZ && isTimeCritical && !viewModel.isChronoFreezeActive) {
            // Panggil suara intens (misal: sfx_heartbeat atau sfx_clock_ticking)
            audio.playIntenseWarning(R.raw.timer_intense)
        } else {
            // Berhenti jika soal dijawab, waktu habis, atau Chrono Freeze aktif
            audio.stopIntenseWarning()
        }
    }

    LaunchedEffect(viewModel.currentQuestionIndex) {
        audio.stopIntenseWarning()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.95f),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF0E7D8),
        border = BorderStroke(3.dp, Color(0xFF5D4037))
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
                    isChronoFreezeActive = viewModel.isChronoFreezeActive,
                    bossType = viewModel.currentBossType,
                    modifier = bossModifier
                )

                Text(
                    text = "Question ${viewModel.currentQuestionIndex + 1}/${viewModel.totalQuestions}",
                    color = Color(0xFF3E2723),
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
                        color = Color(0xFFE5DCC3),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center, 
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (question.question.contains("$")) {
                                MathText(text = question.question, color = Color(0xFF3E2723), fontSize = 14)
                            } else {
                                Text(question.question, color = Color(0xFF3E2723), fontSize = 13.sp, textAlign = TextAlign.Center)
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
                                    color = if (isSelected) Color(0xFFD3C5B9) else Color(0xFFFDF8E1),
                                    border = BorderStroke(2.dp, if (isSelected) Color(0xFF3D0404) else Color(0xFF3E2723).copy(alpha = 0.2f))
                                ) {
                                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
                                        if (option.contains("$")) {
                                            MathText(
                                                text = option,
                                                color = if (isSelected) Color(0xFF3E2723) else Color(0xFF3E2723),
                                                fontSize = 13,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                        } else {
                                            Text(
                                                text = option,
                                                color = if (isSelected) Color(0xFF3E2723) else Color(0xFF3E2723),
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5.2 Alur Visual: Bawah (Controls)
                Column(modifier = Modifier.padding(16.dp)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = viewModel.isStreakProtected,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            color = Color(0xFFE8F5E9), // Hijau muda transparan
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF2E7D32).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_pu_shield),
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Battle Shield Aktif",
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tombol Inventory (Backpack)
                        Surface(
                            onClick = { isInventoryModalVisible = true },
                            modifier = Modifier.size(36.dp),
                            color = Color(0xFF5D4037),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_backpack),
                                    contentDescription = "Inventory",
                                    tint = Color(0xFFF0E7D8),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Tombol Attack
                        Button(
                            onClick = {
                                val isLastQuestion = viewModel.currentQuestionIndex == viewModel.totalQuestions - 1

                                if (isLastQuestion) {
                                    viewModel.submitAnswer()
                                    audio.playSfx("attack")
                                    coroutineScope.launch {
                                        audio.stopIntenseWarning()
                                        isFinishingByAttack = true
                                        audio.stopBGMWithFade(duration = 1000)

                                        delay(1750)
                                    }
                                } else {
                                    audio.stopIntenseWarning()
                                    audio.playSfx("attack")
                                    viewModel.submitAnswer()
                                } },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3E2723) /* Color(0xFFA10000) */, // Merah Marun
                                contentColor = Color.White
                            ),
                            enabled = viewModel.selectedAnswers.isNotEmpty() && !isFinishingByAttack,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isFinishingByAttack) {
                                // Tampilkan loading spinner kecil di dalam tombol
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "ATTACK!",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isInventoryModalVisible) {
        BagModal(
            inventory = inventory,
            isQuizActive = true,
            onClose = { isInventoryModalVisible = false },
            onUsePowerUp = { pu: PowerUpType ->

                val sfxKey = when (pu) {
                    PowerUpType.FREEZE_TIMER -> "sfx_freeze"
                    PowerUpType.STREAK_PROTECTION -> "sfx_shield"
                    PowerUpType.REMOVE_TWO_OPTIONS -> "sfx_magic"
                }

                audio.playSfx(sfxKey)

                viewModel.usePowerUp(
                    type = pu,
                    inventory = inventory,
                    onUpdateInventory = onUpdateInventory
                )
                isInventoryModalVisible = false

            },
            powerUpCooldowns = viewModel.powerUpCooldowns,
        )
    }
}

@Composable
fun ResultBattleItem(index: Int, result: QuestionResult) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE5DCC3)),
        border = BorderStroke(2.dp, if (result.isCorrect) Color(0xFF5D4037) else Color.Red.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Soal $index", color = Color(0xFF3E2723), fontWeight = FontWeight.Bold)
                if (result.wasFast && result.isCorrect) {
                    Text("CRITICAL ATTACK!", color = Color(0xFFA10000), fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
            @Suppress("DEPRECATION")
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Damage Dealt: ${result.playerDamageDealt.toInt()} HP", color = Color(0xFF000849), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Damage Taken: -${result.playerDamageTaken.toInt()} HP", color = Color(0xFF3E2723), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun BattleHpSummary(playerHp: Float, bossHp: Float, bossName: String) {
    val bossDefeated = bossHp <= 0f
    val bossSurrendered = playerHp > bossHp && bossHp > 0f
    
    val statusText = when {
        bossDefeated -> "${bossName.uppercase()} DIKALAHKAN!"
        bossSurrendered -> "${bossName.uppercase()} MENYERAH!"
        else -> "BATTLE ENDED"
    }
    
    val statusColor = when {
        bossDefeated || bossSurrendered -> Color(0xFF3E2723)
        else -> Color(0xFFA10000)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(statusText, color = statusColor, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScoreColumn("SISA HP PLAYER", playerHp.toInt(), Color(0xFF000849))
            ScoreColumn("SISA HP ${bossName.uppercase()}", bossHp.toInt(), Color(0xFFA10000))
        }
    }
}

@Composable
fun ScoreColumn(title: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(score.toString(), color = color, fontSize = 36.sp, fontWeight = FontWeight.Black)
    }
}
