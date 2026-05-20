package com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.data.MAX_BAG_WEIGHT
import com.LambdaProject.MathArt.data.model.ExplorationAudioManager
import com.LambdaProject.MathArt.ui.Pages.Exploration.BagModal
import com.LambdaProject.MathArt.ui.components.MathText
import com.LambdaProject.MathArt.utils.ZoomableImageOverlay
import kotlinx.coroutines.delay

@Composable
fun BossQuizModal(
    viewModel: BossQuizViewModel,
    viewModelMAP: MapViewModel,
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
            !isFinishingByAttack
        ) {
            audio.playIntenseWarning(R.raw.timer_intense)
        } else {
            audio.stopIntenseWarning()
        }

        when (viewModel.phase) {
            BossBattlePhase.INTRO -> {
                delay(2200)
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

    LaunchedEffect(Unit) {
        viewModel.onFinalAttackTrigger = {
            audio.stopBGMWithFade(duration = 2000)
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
            BossBattlePhase.QUIZ -> QuizContent(viewModel,  viewModelMAP, maxBagWeight = viewModelMAP.maxBagWeight, inventory, onUpdateInventory, audio)
            BossBattlePhase.BATTLE_ANIMATION -> {
                BattleActionOverlay(
                    text = viewModel.battleAnimationText,
                    subText = viewModel.battleAnimationSubText
                )
            }
            BossBattlePhase.SUMMARY -> QuizSummaryAnimated(viewModel, onFinish, audio)
        }
    }
}

@Composable
fun QuizContent(
    viewModel: BossQuizViewModel,
    viewModelMAP: MapViewModel,
    maxBagWeight: Float = MAX_BAG_WEIGHT,
    inventory: Inventory,
    onUpdateInventory: (Inventory) -> Unit,
    audio: ExplorationAudioManager
) {
    val question = viewModel.currentQuestion ?: return
    var isMathReady by remember(question.question) { mutableStateOf(false) }
    var typedAnswer by remember(question) { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isInventoryModalVisible by remember { mutableStateOf(false) }
    var isFinishingByAttack by remember { mutableStateOf(false) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = zoomImageUrl != null) {
        zoomImageUrl = null
    }

    LaunchedEffect(viewModel.bossTimeLeftMillis, viewModel.phase, viewModel.isChronoFreezeActive) {
        val isTimeCritical = viewModel.bossTimeLeftMillis in 1..4000

        if (viewModel.phase == BossBattlePhase.QUIZ && isTimeCritical && !viewModel.isChronoFreezeActive) {
            audio.playIntenseWarning(R.raw.timer_intense)
        } else {
            audio.stopIntenseWarning()
        }
    }

    LaunchedEffect(viewModel.currentQuestionIndex) {
        audio.stopIntenseWarning()
        isSubmitting = false
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.95f),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF0E7D8),
        border = BorderStroke(3.dp, Color(0xFF5D4037))
    ) {
        Box {
            Column {
                BattleHeader(
                    playerHp = viewModel.playerHp,
                    bossHp = viewModel.bossHp,
                    bossProgress = viewModel.bossThinkingProgress,
                    bossTimeLeftMillis = viewModel.bossTimeLeftMillis,
                    isChronoFreezeActive = viewModel.isChronoFreezeActive,
                    bossType = viewModel.currentBossType,
                )

                // SOAL
                Column(modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)) {
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
                            val hasFormatting = question.question.contains("$") ||
                                    question.question.contains("**") ||
                                    question.question.contains("_") ||
                                    question.question.contains("![") ||
                                    question.question.contains("[") ||
                                    question.question.contains("* ")

                            if (hasFormatting) {
                                if (!isMathReady) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color(0xFF5D4037).copy(alpha = 0.4f),
                                        strokeWidth = 2.dp
                                    )
                                }
                                MathText(
                                    text = question.question,
                                    color = Color(0xFF3E2723),
                                    fontSize = 14,
                                    textAlign = "left",
                                    onRenderComplete = { isMathReady = true },
                                    onImageClick = { url -> zoomImageUrl = url }
                                )
                            } else {
                                Text(
                                    text = question.question,
                                    color = Color(0xFF3E2723),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Left
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val isMultiAnswer = question.answerKey.size > 1
                    if (isMultiAnswer) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF3E2723).copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Pilih semua jawaban yang benar",
                                color = Color(0xFF3E2723).copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // OPSI
                    Column(modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())) {
                        if (question.questionType == "short_answer") {
                            OutlinedTextField(
                                value = typedAnswer,
                                onValueChange = {
                                    typedAnswer = it
                                    viewModel.selectedAnswers.clear()
                                    if (it.isNotBlank()) {
                                        viewModel.selectedAnswers.add(it)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                placeholder = { Text("Ketik jawabanmu di sini...", color = Color(0xFF5D4037).copy(alpha = 0.5f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF5D4037),
                                    unfocusedBorderColor = Color(0xFF5D4037).copy(alpha = 0.3f),
                                    focusedContainerColor = Color(0xFFFDF8E1),
                                    unfocusedContainerColor = Color(0xFFFDF8E1)
                                ),
                                singleLine = true
                            )
                        } else {
                            question.options.forEach { option ->
                                if (!viewModel.removedOptions.contains(option)) {
                                    val isSelected = viewModel.selectedAnswers.contains(option)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) Color(0xFFD3C5B9) else Color(0xFFFDF8E1),
                                            border = BorderStroke(2.dp, if (isSelected) Color(0xFF3D0404) else Color(0xFF3E2723).copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isMultiAnswer) {
                                                    Surface(
                                                        modifier = Modifier.size(20.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (isSelected) Color(0xFF3D0404) else Color.Transparent,
                                                        border = BorderStroke(1.5.dp, Color(0xFF3E2723).copy(alpha = 0.5f))
                                                    ) {
                                                        if (isSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.padding(2.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                }

                                                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
                                                    val hasFormatting = option.contains("$") ||
                                                            option.contains("**") ||
                                                            option.contains("_") ||
                                                            option.contains("![") ||
                                                            option.contains("[") ||
                                                            option.contains("* ")
                                                    if (hasFormatting) {
                                                        if (!isMathReady) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(24.dp),
                                                                color = Color(0xFF5D4037).copy(alpha = 0.4f),
                                                                strokeWidth = 2.dp
                                                            )
                                                        }

                                                        MathText(
                                                            text = option,
                                                            color = /* if (isSelected) Color(0xFF3E2723) else */ Color(0xFF3E2723),
                                                            fontSize = 13,
                                                            textAlign = "left",
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            onRenderComplete = { isMathReady = true }
                                                        )
                                                    } else {
                                                        Text(
                                                            text = option,
                                                            color = /* if (isSelected) Color(0xFF3E2723) else */ Color(0xFF3E2723),
                                                            fontSize = 14.sp,
                                                            textAlign = TextAlign.Left,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    viewModel.toggleAnswer(option)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // CONTROL
                Column(modifier = Modifier.padding(16.dp)) {
                    AnimatedVisibility(
                        visible = viewModel.isStreakProtected,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            color = Color(0xFFE8F5E9),
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
                        // Inventory
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
                                if (isSubmitting) return@Button
                                isSubmitting = true

                                audio.stopIntenseWarning()
                                audio.playSfx("attack")

                                viewModel.submitAnswer(
                                    onFinalAttack = {
                                        audio.stopBGMWithFade(duration = 1500)
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3E2723) /* Color(0xFFA10000) */,
                                contentColor = Color.White
                            ),
                            enabled = viewModel.selectedAnswers.isNotEmpty() && /*!isFinishingByAttack && */ !isSubmitting,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (/* isFinishingByAttack ||*/ isSubmitting) {
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
            ZoomableImageOverlay(
                imageUrl = zoomImageUrl,
                onDismiss = { zoomImageUrl = null }
            )
        }
    }

    if (isInventoryModalVisible) {
        BagModal(
            viewModel = viewModelMAP,
            inventory = inventory,
            playerHp = viewModel.playerHp,
            maxBagWeight = viewModelMAP.maxBagWeight,
            isLanternActive = viewModelMAP.isLanternActive,
            isLeatherStrapsActive = viewModelMAP.isLeatherStrapsActive,
            isDropping = viewModelMAP.isDropping,
            isQuizActive = true,
            onClose = { isInventoryModalVisible = false },
            onUsePowerUp = { pu: PowerUpType ->

                val sfxKey = when (pu) {
                    PowerUpType.FREEZE_TIMER -> "sfx_freeze"
                    PowerUpType.STREAK_PROTECTION -> "sfx_shield"
                    PowerUpType.REMOVE_TWO_OPTIONS -> "sfx_magic"
                    PowerUpType.HEALING_VIAL -> "sfx_healing"
                    PowerUpType.LEATHER_STRAPS -> ""
                    PowerUpType.MAGIC_KEY -> ""
                    PowerUpType.BINOCULAR -> ""
                    PowerUpType.LANTERN -> ""
                    PowerUpType.TORCH -> ""
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
                Text("Damage Taken: ${result.playerDamageTaken.toInt()} HP", color = Color(0xFF3E2723), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
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
