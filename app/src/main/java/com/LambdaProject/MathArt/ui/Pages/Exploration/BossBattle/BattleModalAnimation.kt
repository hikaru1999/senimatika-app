package com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.ViewModels.BossQuizViewModel
import kotlinx.coroutines.delay

@Composable
fun BattleIntroAnimation() {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }

    val slideLeft by animateDpAsState(if (startAnim) 0.dp else (-300).dp, tween(500))
    val slideRight by animateDpAsState(if (startAnim) 0.dp else 300.dp, tween(500))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PLAYER", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 32.sp, modifier = Modifier.offset(x = slideLeft))
            Text(" VS ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("BOSS", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 32.sp, modifier = Modifier.offset(x = slideRight))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("BATTLE START!", color = Color.Yellow, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun CountdownAnimation(value: Int) {
    val scale by animateFloatAsState(
        targetValue = 1.5f,
        animationSpec = repeatable(3, tween(1000), repeatMode = RepeatMode.Reverse)
    )
    Text(
        text = value.toString(),
        color = Color.White,
        fontSize = 120.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.scale(scale)
    )
}

@Composable
fun QuizSummaryAnimated(viewModel: BossQuizViewModel, onFinish: (Boolean) -> Unit) {
    var visibleItems by remember { mutableIntStateOf(0) }
    val results = viewModel.questionResults

    LaunchedEffect(Unit) {
        for (i in 1..results.size + 2) {
            delay(600)
            visibleItems = i
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF1A1A1A),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("BATTLE RESULT", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                results.forEachIndexed { index, result ->
                    AnimatedVisibility(
                        visible = visibleItems > index,
                        enter = fadeIn() + expandVertically()
                    ) {
                        ResultBattleItem(index + 1, result)
                    }
                }

                if (visibleItems > results.size) {
                    AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn()) {
                        BattleHpSummary(viewModel.playerHp, viewModel.bossHp)
                    }
                }
            }

            if (visibleItems > results.size + 1) {
                val isWin = viewModel.bossHp <= 0f || (viewModel.playerHp > viewModel.bossHp && viewModel.playerHp > 0)
                Button(
                    onClick = {
                        onFinish(isWin)
                        viewModel.closeQuiz()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isWin) Color(0xFF4CAF50) else Color.Gray)
                ) {
                    Text("SELESAI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}