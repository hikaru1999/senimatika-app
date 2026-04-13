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
import com.LambdaProject.MathArt.data.DilateIndicator
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriDilatasi(currentPage: Int, myPage: Int, onQuizNavigate: () -> Unit, onNext: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

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
        // Section: Header Image
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_reflection), // Note: Using img_reflection as per original, though title is Dilatasi
                contentDescription = "Dilatasi",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        }

        // Section: Definition
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Dilatasi (Perkalian)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
            )
            Text(
                text = "Dilatasi adalah transformasi yang mengubah bentuk bangun geometri dengan memperkecil atau memperbesar tanpa mengubah bentuk asli. Perubahan disebabkan karena adanya faktor skala dan titik pusat dilatasi.",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify,
                lineHeight = 24.sp,
                color = Color(0xFF455A64)
            )
        }

        // Section: Properties
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE3F2FD),
            border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Sifat-sifat Dilatasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(16.dp))
                DilateIndicator.forEachIndexed { index, poin ->
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

        // Section: Case Study
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Penerapan pada Motif Batik",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFontFamily,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Pada motif batik, kita dapat menemukan pola yang dihasilkan dari proses dilatasi. Perhatikan motif batik di bawah ini:",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF455A64)
            )
            Image(
                painter = painterResource(id = R.drawable.img_batik_dilatasi),
                contentDescription = "Batik Dilatasi",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Motif yang ditandai dengan warna putih menunjukkan bahwa terdapat hasil dilatasi pada motif batik tersebut.",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Section: Video
        Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black
        ) {
            MyYouTubePlayer(videoId = "yaBqwLIK2H8")
        }

        // Section: Annotation
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
                    imageRes = R.drawable.img_batik_sekar_draw,
                    explanationImageRes = R.drawable.img_batik_sekar_ans,
                    explanationText = "Motif batik di atas merupakan motif Sekar Jagad yang berasal dari Solo dan Yogyakarta. Contoh hasil dilatasi dapat dilihat pada gambar berikut."
                )
            }
        }

        // Bottom Button
        Button(
            onClick = { if (userId != null) updateAccessiblePage(userId, 5); onQuizNavigate() },
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Ambil Kuis", fontFamily = interFontFamily, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}
