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
import com.LambdaProject.MathArt.data.RotateIndicator
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriRotasi(currentPage: Int, myPage: Int, onNext: () -> Unit) {
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
                painter = painterResource(id = R.drawable.img_rotation),
                contentDescription = "Rotasi",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Section: Definition
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Rotasi (Perputaran)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
            )
            Text(
                text = "Rotasi merupakan perpindahan geometri dengan cara diputar lewat suatu pusat dan sudut tertentu. Besarnya sudut pada rotasi berlawanan arah dengan jarum jam.",
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
                    text = "Sifat-sifat Rotasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(16.dp))
                RotateIndicator.forEachIndexed { index, poin ->
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

        // Section: Batik Simbut
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Simetri pada Batik Simbut",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFontFamily,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Penerapan rotasi suatu objek dapat dilihat pada motif Simbut berikut. Perhatikan pola perputaran yang dihasilkan.",
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF455A64)
            )
            
            Image(
                painter = painterResource(id = R.drawable.img_batik_simbut),
                contentDescription = "Batik Simbut",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // Section: Video
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Video Pembelajaran",
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
                YouTubePlayer(youtubeUrl = "https://youtu.be/iUt_Clfri4w")
            }
        }

        // Bottom Button
        Button(
            onClick = { if (userId != null) updateAccessiblePage(userId, 4); onNext() },
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Buka: Dilatasi", fontFamily = interFontFamily, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}
