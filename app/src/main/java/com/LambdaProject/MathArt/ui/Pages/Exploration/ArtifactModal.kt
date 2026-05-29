package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.Color
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
                                artifact.content.contains("* ")

                        if (hasFormatting) {
                            if (!contentReady) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(30.dp),
                                    color = Color(0xFF3E2723)
                                )
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
