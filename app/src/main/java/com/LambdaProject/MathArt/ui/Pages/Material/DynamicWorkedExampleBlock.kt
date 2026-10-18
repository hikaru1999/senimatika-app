package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.LambdaProject.MathArt.data.model.WorkedExampleStep
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText

@Composable
fun DynamicWorkedExampleBlock(block: ContentBlock) {
    val steps = block.steps ?: emptyList()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFFBEB),
        border = BorderStroke(1.2.dp, Color(0xFFFDE68A)),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Badge on top, Title below
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEA580C)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = block.badge?.ifBlank { "WORKED EXAMPLE" } ?: "CONTOH SOAL",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            fontFamily = interFontFamily,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (!block.title.isNullOrBlank()) {
                    val titleText = block.title
                    val titleHasMath = remember(titleText) {
                        titleText.contains("$") || titleText.contains("\\")
                    }
                    if (titleHasMath) {
                        MathText(
                            text = titleText,
                            fontSize = 17,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    } else {
                        Text(
                            text = titleText,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            // Problem Statement Box (Soal)
            if (!block.problemText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Permasalahan:",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1D4ED8)
                        )
                        val probHasMath = remember(block.problemText) {
                            block.problemText.contains("$") || block.problemText.contains("\\")
                        }
                        if (probHasMath) {
                            MathText(
                                text = block.problemText,
                                fontSize = 14,
                                color = Color(0xFF1E293B),
                                textAlign = "justify"
                            )
                        } else {
                            Text(
                                text = block.problemText,
                                fontFamily = interFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Justify
                            )
                        }

                        if (!block.imageUrl.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            AsyncImage(
                                model = block.imageUrl,
                                contentDescription = "Ilustrasi Soal",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Step-by-Step Solution Container
            if (steps.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Langkah Penyelesaian:",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF334155)
                    )

                    steps.forEachIndexed { index, step ->
                        WorkedExampleStepItem(step = step, stepNumber = index + 1)
                    }
                }
            }

            // Final Answer / Conclusion Box
            if (!block.finalAnswer.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.2.dp, Color(0xFF86EFAC)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selesai",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Jawaban Akhir:",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF15803D)
                            )
                            Spacer(Modifier.height(2.dp))
                            val faText = block.finalAnswer
                            val faHasMath = remember(faText) {
                                faText.contains("$") || faText.contains("\\")
                            }
                            if (faHasMath) {
                                MathText(
                                    text = faText,
                                    fontSize = 14,
                                    color = Color(0xFF14532D),
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = faText,
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF14532D)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkedExampleStepItem(step: WorkedExampleStep, stepNumber: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Step Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$stepNumber",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily
                        )
                    }
                }

                Text(
                    text = step.stepTitle,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                color = Color(0xFFF1F5F9),
                thickness = 1.dp
            )

            // Step Content (Supports KaTeX equations and multi-line markdown)
            val contentHasMath = remember(step.stepContent) {
                step.stepContent.contains("$") || step.stepContent.contains("\\")
            }
            if (contentHasMath) {
                MathText(
                    text = step.stepContent,
                    fontSize = 14,
                    color = Color(0xFF334155),
                    textAlign = "justify"
                )
            } else {
                Text(
                    text = step.stepContent,
                    fontFamily = interFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
            }

            // Tip / Hint Box if present
            if (!step.tip.isNullOrBlank()) {
                val tipText = step.tip
                val tipHasMath = remember(tipText) {
                    tipText.contains("$") || tipText.contains("\\")
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tip",
                            tint = Color(0xFFD97706),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        if (tipHasMath) {
                            MathText(
                                text = tipText,
                                fontSize = 12,
                                color = Color(0xFF92400E)
                            )
                        } else {
                            Text(
                                text = tipText,
                                fontFamily = interFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
