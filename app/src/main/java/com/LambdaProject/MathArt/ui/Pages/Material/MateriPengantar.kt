package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.*
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriPengantar(currentPage: Int, myPage: Int, onNext: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    val correctAnswer = "Parang"
    val isCorrect = selectedOption == correctAnswer

    LaunchedEffect(currentPage) {
        if (currentPage != myPage) {
            YouTubePlayerManager.pauseAll()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section: Title & Video
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Pengantar Etnomatematika",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E),
                    letterSpacing = (-0.5).sp
                )
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black
            ) {
                YouTubePlayer(youtubeUrl = "https://youtu.be/1M86rKgqdzo")
            }
        }

        // Section: Description
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        textAlign = TextAlign.Justify,
                        lineHeight = 26.sp
                    )
                ) {
                    append("Etnomatematika adalah sebuah ilmu yang mempelajari tentang budaya suatu bangsa dan mengaitkannya dengan Matematika.\n\n")
                    append("Pada kesempatan ini, akan dibahas kaitan salah satu budaya Indonesia yaitu batik yang dihubungkan dengan materi Matematika. ")
                    append("Batik merupakan salah satu kekayaan budaya Indonesia yang dimiliki oleh seluruh daerah yang tersebar di Indonesia.")
                }
            },
            fontFamily = interFontFamily,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF455A64)
        )

        // Section: Mini Quiz
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF0F4FF),
            border = BorderStroke(1.dp, Color(0xFF5294FF).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF5294FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "KUIS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Cek Pemahamanmu!",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A237E)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tentukan nama dari motif Batik ini:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontFamily = interFontFamily,
                    color = Color(0xFF37474F)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.img_batik_parang),
                    contentDescription = "Motif Batik",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Kawung", "Parang").forEach { option ->
                        val isSelected = selectedOption == option
                        val buttonColor = if (isSelected) Color(0xFF5294FF) else Color.White
                        val contentColor = if (isSelected) Color.White else Color(0xFF1A237E)
                        
                        Surface(
                            onClick = {
                                if (!isAnswered) {
                                    selectedOption = option
                                    isAnswered = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = buttonColor,
                            border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFFCFD8DC)),
                            shadowElevation = if (isSelected) 4.dp else 0.dp
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = contentColor
                            )
                        }
                    }
                }

                if (isAnswered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(if (isCorrect) "Jawaban kamu benar! " else "Jawaban kamu belum tepat. ")
                                }
                                append("Ini adalah motif batik Parang. Motif Parang memiliki bentuk seperti ombak yang berulang dan melambangkan semangat pantang menyerah.")
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = interFontFamily,
                            color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }

        // Section: Transformasi Geometri Intro
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Transformasi Geometri",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
            )
            Text(
                text = "Transformasi geometri adalah proses perubahan posisi, ukuran, atau bentuk suatu bidang. Fokus kita kali ini: Translasi, Refleksi, Rotasi, dan Dilatasi.",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify,
                lineHeight = 24.sp,
                color = Color(0xFF455A64)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black
            ) {
                YouTubePlayer(youtubeUrl = "https://youtu.be/akQBAB5cyIk")
            }
        }

        // Bottom Navigation Button
        Button(
            onClick = {
                if (userId != null) {
                    updateAccessiblePage(userId, 1)
                }
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Buka: Translasi",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Spacer(Modifier.width(8.dp))
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}
