package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.LambdaProject.MathArt.data.model.ContentBlock
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText

@Composable
fun DynamicInArticleQuiz(block: ContentBlock) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    val correctAnswer = block.correctAnswer.orEmpty()
    val isCorrect = selectedOption?.trim().equals(correctAnswer.trim(), ignoreCase = true)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF0F4FF),
        border = BorderStroke(1.dp, Color(0xFF5294FF).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Color(0xFF0284C7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = block.badge?.ifBlank { "KUIS CEPAT" } ?: "KUIS CEPAT",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        fontFamily = interFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = block.prompt?.ifBlank { "Cek Pemahamanmu!" } ?: "Cek Pemahamanmu!",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            }

            if (!block.question.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                val qHasMath = remember(block.question) {
                    block.question.contains("$") || block.question.contains("\\")
                }
                if (qHasMath) {
                    MathText(
                        text = block.question,
                        fontSize = 15,
                        color = Color(0xFF37474F)
                    )
                } else {
                    Text(
                        text = block.question,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        fontFamily = interFontFamily,
                        color = Color(0xFF37474F)
                    )
                }
            }

            if (!block.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = block.imageUrl,
                    contentDescription = "Gambar Soal Kuis",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            val options = block.options ?: emptyList()
            if (options.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                if (options.size <= 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        options.forEach { option ->
                            InArticleOptionButton(
                                option = option,
                                isSelected = selectedOption == option,
                                modifier = Modifier.weight(1f),
                                onSelect = {
                                    if (!isAnswered) {
                                        selectedOption = option
                                        isAnswered = true
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        options.forEach { option ->
                            InArticleOptionButton(
                                option = option,
                                isSelected = selectedOption == option,
                                modifier = Modifier.fillMaxWidth(),
                                onSelect = {
                                    if (!isAnswered) {
                                        selectedOption = option
                                        isAnswered = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (isAnswered) {
                Spacer(modifier = Modifier.height(16.dp))
                val feedbackColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                val feedbackBg = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = feedbackBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = feedbackColor
                        )
                        Spacer(Modifier.width(10.dp))
                        val expl = if (isCorrect) {
                            block.explanation?.ifBlank { "Jawaban kamu benar!" } ?: "Jawaban kamu benar!"
                        } else {
                            if (!block.explanation.isNullOrBlank()) {
                                block.explanation
                            } else {
                                "Kurang tepat. Jawaban yang benar adalah: $correctAnswer"
                            }
                        }
                        val expHasMath = remember(expl) { expl.contains("$") || expl.contains("\\") }
                        if (expHasMath) {
                            MathText(
                                text = expl,
                                fontSize = 14,
                                color = feedbackColor
                            )
                        } else {
                            Text(
                                text = expl,
                                color = feedbackColor,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InArticleOptionButton(
    option: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val buttonColor = if (isSelected) Color(0xFF5294FF) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF1A237E)

    val hasMath = remember(option) {
        option.contains("$") || option.contains("\\") || option.contains("{") || option.contains("^") || option.contains("_")
    }

    Surface(
        onClick = onSelect,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = buttonColor,
        border = BorderStroke(1.2.dp, if (isSelected) Color.Transparent else Color(0xFFCFD8DC)),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasMath) {
                val formattedMath = if (!option.contains("$")) "$option$" else option
                MathText(
                    text = formattedMath,
                    fontSize = 14,
                    color = textColor,
                    textAlign = "center",
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = option,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = interFontFamily,
                    fontSize = 14.sp
                )
            }

            // Transparent overlay to ensure clicking anywhere on the WebView/Box triggers onSelect smoothly
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSelect
                    )
            )
        }
    }
}
