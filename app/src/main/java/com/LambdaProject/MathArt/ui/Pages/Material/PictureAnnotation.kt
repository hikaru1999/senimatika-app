package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.MaterialTheme
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun PictureAnnotation(
    @DrawableRes imageRes: Int,
    @DrawableRes explanationImageRes: Int,
    explanationText: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paths = remember { mutableStateListOf<List<Offset>>() }
    val imageBitmap = ImageBitmap.imageResource(context.resources, imageRes)
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var showExplanation by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clip(RoundedCornerShape(5.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> currentPath = listOf(offset) },
                        onDrag = { change, _ -> currentPath = currentPath + change.position },
                        onDragEnd = {
                            paths.add(currentPath)
                            currentPath = emptyList()
                        }
                    )
                }
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Gambar Interaktif",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                paths.forEach { path ->
                    for (i in 0 until path.size - 1) {
                        drawLine(
                            color = Color.Red,
                            start = path[i],
                            end = path[i + 1],
                            strokeWidth = 15f
                        )
                    }
                }
                for (i in 0 until currentPath.size - 1) {
                    drawLine(
                        color = Color.Red,
                        start = currentPath[i],
                        end = currentPath[i + 1],
                        strokeWidth = 15f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { paths.clear() },
                shape = RoundedCornerShape(5.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
                Spacer(Modifier.width(4.dp))
                Text(text = "Reset", fontFamily = interFontFamily)
            }
            Spacer(Modifier.width(5.dp))
            Button(
                onClick = { showExplanation = true },
                enabled = paths.isNotEmpty(),
                shape = RoundedCornerShape(5.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Cek Jawaban")
                Spacer(Modifier.width(4.dp))
                Text("Cek Jawaban", fontFamily = interFontFamily)
            }
        }

        if (showExplanation) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = explanationText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Justify,
                fontFamily = interFontFamily
            )
            Image(
                painter = painterResource(id = explanationImageRes),
                contentDescription = "Penjelasan",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun DynamicPictureAnnotation(
    baseImageUrl: String,
    solutionImageUrl: String,
    explanationText: String,
    instruction: String? = null,
    modifier: Modifier = Modifier
) {
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var showExplanation by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (!instruction.isNullOrBlank()) {
            Text(
                text = instruction,
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF388E3C),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F8E9))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> currentPath = listOf(offset) },
                        onDrag = { change, _ -> currentPath = currentPath + change.position },
                        onDragEnd = {
                            paths.add(currentPath)
                            currentPath = emptyList()
                        }
                    )
                }
        ) {
            AsyncImage(
                model = baseImageUrl,
                contentDescription = "Gambar Interaktif",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                paths.forEach { path ->
                    for (i in 0 until path.size - 1) {
                        drawLine(
                            color = Color.Red,
                            start = path[i],
                            end = path[i + 1],
                            strokeWidth = 14f
                        )
                    }
                }
                for (i in 0 until currentPath.size - 1) {
                    drawLine(
                        color = Color.Red,
                        start = currentPath[i],
                        end = currentPath[i + 1],
                        strokeWidth = 14f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { paths.clear() },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
                Spacer(Modifier.width(4.dp))
                Text(text = "Reset", fontFamily = interFontFamily)
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = { showExplanation = true },
                enabled = paths.isNotEmpty(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Cek Jawaban")
                Spacer(Modifier.width(4.dp))
                Text("Cek Jawaban", fontFamily = interFontFamily)
            }
        }

        if (showExplanation) {
            Spacer(modifier = Modifier.height(16.dp))
            if (explanationText.isNotBlank()) {
                Text(
                    text = explanationText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B5E20),
                    textAlign = TextAlign.Justify,
                    fontFamily = interFontFamily,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            if (solutionImageUrl.isNotBlank()) {
                AsyncImage(
                    model = solutionImageUrl,
                    contentDescription = "Penjelasan Kunci",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

