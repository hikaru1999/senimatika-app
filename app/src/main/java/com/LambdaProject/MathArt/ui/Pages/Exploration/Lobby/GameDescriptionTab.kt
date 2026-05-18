package com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDescriptionTab(
    name: String,
    description: String,
    levelType: String,
    hasFog: Boolean,
    hasNight: Boolean,
    hasCombined: Boolean,
    onBack: () -> Unit,
    onStartExploration: () -> Unit,
    isResume: Boolean = false
) {
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFE3F2FD))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo_blue),
                contentDescription = null,
                modifier = Modifier.size(180.dp)
            )
        }

        Column(
            modifier = Modifier
                .offset(y = (-30).dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFontFamily,
                color = Color(0xFF1A237E),
                textAlign = TextAlign.Center
            )

            Row (
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Surface(
                    color = if (levelType == "TUTORIAL") Color(0xFFFF9800) else Color(0xFFE8EAF6),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (levelType == "TUTORIAL") "TUTORIAL EKSPLORASI" else "MATH EXPLORATION",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (levelType == "TUTORIAL") Color.White else Color(0xFF3F51B5),
                        letterSpacing = 1.sp
                    )
                }

                if (hasCombined) {
                    VisibilityTag(text = "Combined Mode", color = Color(0xFFFF6D00))
                } else {
                    if (hasFog) VisibilityTag(text = "Fog of War", color = Color(0xFF03A9F4))
                    if (hasNight) VisibilityTag(text = "Night Mode", color = Color(0xFF7E57C2))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                if (hasCombined) {
                    VisibilityTag(text = "Combined Mode", color = Color(0xFFFF6D00))
                } else {
                    if (hasFog) VisibilityTag(text = "Fog of War", color = Color(0xFF03A9F4))
                    if (hasNight) VisibilityTag(text = "Night Mode", color = Color(0xFF7E57C2))
                }
            } */

            Text(
                text = description,
                textAlign = TextAlign.Center,
                fontFamily = interFontFamily,
                lineHeight = 24.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (!isLoading) {
                        if (isResume) {
                            isLoading = true
                        }
                        onStartExploration()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    disabledContainerColor = Color.Gray),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (isResume) "LANJUTKAN PETUALANGAN" else "MULAI PETUALANGAN",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        fontFamily = interFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun VisibilityTag(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.9f),
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
    }
}
