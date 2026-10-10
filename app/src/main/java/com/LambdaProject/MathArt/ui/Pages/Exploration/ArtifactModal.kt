package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText

@Composable
fun ArtifactInfoModal(
    artifact: MapViewModel.ArtifactData,
    onClose: () -> Unit
) {
    var contentReady by remember(artifact.content) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.85f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFDF8E1),
                border = BorderStroke(4.dp, Color(0xFF8D6E63))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    /* Text(
                        text = "LANDMARK DITEMUKAN",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF8D6E63),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp)) */

                    // Judul
                    Text(
                        text = artifact.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF3E2723)
                    )

                    // Materi
                    Surface(
                        color = Color(0xFF8D6E63).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = artifact.materi,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF8D6E63).copy(alpha = 0.2f))

                    // Konten
                    Box(modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                    ) {
                        val hasFormatting = artifact.content.contains("$") ||
                                artifact.content.contains("**") ||
                                artifact.content.contains("_") ||
                                artifact.content.contains("![") ||
                                artifact.content.contains("[") ||
                                artifact.content.contains("* ") ||
                                artifact.content.contains("- ") ||
                                artifact.content.contains("+ ") ||
                                artifact.content.contains("•") ||
                                artifact.content.contains(Regex("""\d+[\.\)]\s"""))

                        if (hasFormatting) {
                            if (!contentReady) {
                                ArtifactContentSkeleton()
                            }
                            MathText(
                                text = artifact.content,
                                color = Color(0xFF3E2723),
                                fontSize = 12,
                                textAlign = "left",
                                modifier = Modifier.fillMaxWidth(),
                                onRenderComplete = { contentReady = true }
                            )
                        } else {
                            Text(
                                text = artifact.content,
                                fontFamily = interFontFamily,
                                textAlign = TextAlign.Left,
                                color = Color(0xFF3E2723),
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF8D6E63),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun Modifier.artifactParchmentShimmer(shape: Shape = RoundedCornerShape(4.dp)): Modifier {
    val transition = rememberInfiniteTransition(label = "artifactShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "artifactShimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF8D6E63).copy(alpha = 0.10f),
                    Color(0xFF8D6E63).copy(alpha = 0.25f),
                    Color(0xFF8D6E63).copy(alpha = 0.10f)
                ),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 300f, translateAnim + 300f)
            )
        )
}

@Composable
fun ArtifactContentSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(14.dp)
                .artifactParchmentShimmer(RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(14.dp)
                .artifactParchmentShimmer(RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .height(14.dp)
                .artifactParchmentShimmer(RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(14.dp)
                .artifactParchmentShimmer(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .artifactParchmentShimmer(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(14.dp)
                .artifactParchmentShimmer(RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(14.dp)
                .artifactParchmentShimmer(RoundedCornerShape(4.dp))
        )
    }
}

