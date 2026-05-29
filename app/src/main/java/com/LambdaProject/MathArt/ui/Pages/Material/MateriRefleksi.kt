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
import com.LambdaProject.MathArt.data.ReflectIndicator
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriRefleksi(currentPage: Int, myPage: Int, onNext: () -> Unit) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val correctAnswer = "Sido Mulyo"
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_reflection),
                contentDescription = "Refleksi",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Refleksi (Pencerminan)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
            )
            Text(
                text = "Refleksi merupakan perpindahan geometri yang memindahkan setiap titik suatu objek dengan menggunakan sifat bayangan cermin.",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify,
                lineHeight = 24.sp,
                color = Color(0xFF455A64)
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE3F2FD),
            border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Sifat-sifat Refleksi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(16.dp))
                ReflectIndicator.forEachIndexed { index, poin ->
                    Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = poin,
                            fontFamily = interFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify,
                            lineHeight = 20.sp,
                            color = Color(0xFF0D47A1)
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Eksplorasi Budaya",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFontFamily,
                color = Color(0xFF1A237E)
            )
            Image(
                painter = painterResource(id = R.drawable.img_batik_refleksi),
                contentDescription = "Batik Refleksi",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Motif yang ditandai dengan warna merah menunjukkan adanya bentuk hasil refleksi pada motif batik di atas.",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF0F4FF),
            border = BorderStroke(1.dp, Color(0xFF5294FF).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Kuis Cepat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.img_batik_parang_sido),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Berdasarkan ilustrasi di atas, apa nama dari motif batik tersebut?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = interFontFamily,
                    color = Color(0xFF37474F)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Sido Mulyo", "Sido Luhur").forEach { option ->
                        val isSelected = selectedOption == option
                        Surface(
                            onClick = { if (!isAnswered) { selectedOption = option; isAnswered = true } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF5294FF) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFFCFD8DC))
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF1A237E)
                            )
                        }
                    }
                }
                
                if (isAnswered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isCorrect) "Tepat sekali! Ini adalah Batik Sido Mulyo." else "Hampir benar! Ini adalah Batik Sido Mulyo.",
                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black
        ) {
            YouTubePlayer(youtubeUrl = "https://youtu.be/Cv_zkDAU6UY")
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF1F8E9),
            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Waktunya Mencoret!", fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(12.dp))
                PictureAnnotation(
                    imageRes = R.drawable.img_batik_sido_asih,
                    explanationImageRes = R.drawable.img_batik_sido_asih_ans,
                    explanationText = "Motif batik di atas merupakan motif Sido Asih dari Yogyakarta. Contoh hasil refleksi dapat dilihat pada gambar berikut."
                )
            }
        }

        Button(
            onClick = { if (userId != null) updateAccessiblePage(userId, 3); onNext() },
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Buka: Rotasi", fontFamily = interFontFamily, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}
