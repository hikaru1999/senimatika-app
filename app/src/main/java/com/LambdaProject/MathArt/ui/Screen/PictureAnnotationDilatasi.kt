package com.LambdaProject.MathArt.ui.Screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun PictureAnnotationDilatasi(@DrawableRes imageRes: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val imageBitmap = ImageBitmap.imageResource(context.resources, imageRes)
    var showExplanation by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clip(RoundedCornerShape(5.dp))
                .pointerInput(Unit)
                {
                    detectDragGestures (
                        onDragStart = { offset -> currentPath = listOf(offset) },
                        onDrag = { change, _ -> currentPath = currentPath + change.position},
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
                    drawLine(color = Color.Red, currentPath[i], currentPath[i + 1], strokeWidth = 15f)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
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
                text = "Motif batik di atas merupakan motif Sekar Jagad yang berasal dari Solo dan Yogyakarta. Contoh hasil dilatasi dapat dilihat pada gambar berikut.\n",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Justify
            )
            Image(
                painter = painterResource(id = R.drawable.img_batik_sekar_ans),
                contentDescription = "Batik Sekar Jagad",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}