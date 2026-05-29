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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.*
import com.LambdaProject.MathArt.data.TranslateIndicator
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriTranslasi(currentPage: Int, myPage: Int, onNext: () -> Unit) {
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_translation),
                contentDescription = "Translasi",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Translasi (Pergeseran)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
            )
            Text(
                text = "Translasi merupakan perpindahan geometri yang menggeser setiap titik suatu objek atau ruang dengan jarak yang sama dengan arah tertentu.",
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
                    text = "Sifat-sifat Translasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TranslateIndicator.forEachIndexed { index, poin ->
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
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                text = "Penerapan pada Batik Parang",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFontFamily,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Perhatikan motif batik Parang berikut! Kita dapat menemukan pola yang dihasilkan dari proses translasi.",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF455A64)
            )
            
            Image(
                painter = painterResource(id = R.drawable.img_parang_ann),
                contentDescription = "Batik Parang Translasi",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Text(
                text = "Motif yang ditandai dengan warna merah menunjukkan bahwa terdapat bentuk hasil translasi pada motif batik Parang di atas.",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = Color.Gray
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Mari Menyimak",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFontFamily,
                color = Color(0xFF1A237E)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black
            ) {
                YouTubePlayer(youtubeUrl = "https://youtu.be/baskAXQZxAc")
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF1F8E9),
            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Waktunya Mencoret!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tandai bagian dari motif batik berikut yang berhubungan dengan hasil translasi",
                    fontFamily = interFontFamily,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF388E3C)
                )
                Spacer(modifier = Modifier.height(16.dp))
                PictureAnnotation(
                    imageRes = R.drawable.img_batik_crop,
                    explanationImageRes = R.drawable.img_batik_ans,
                    explanationText = "Motif batik di atas merupakan motif Gajah Oling dari Banyuwangi, Jawa Timur. Contoh hasil translasi dapat dilihat pada gambar berikut."
                )
            }
        }

        Button(
            onClick = {
                if (userId != null) {
                    updateAccessiblePage(userId, 2)
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
            Text(
                text = "Buka: Refleksi",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
        
        Spacer(Modifier.height(32.dp))
    }
}
