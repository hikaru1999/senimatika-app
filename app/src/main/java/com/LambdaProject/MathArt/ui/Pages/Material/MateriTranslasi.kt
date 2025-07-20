package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.*
import com.LambdaProject.MathArt.Data.stepOfTranslate
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
            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_translation),
            contentDescription = "Translasi",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Translasi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Justify
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        textAlign = TextAlign.Justify,
                        lineHeight = 24.sp
                    )
                ) {
                    append("Translasi merupakan perpindahan geometri yang menggeser setiap titik suatu objek atau ruang dengan jarak yang sama dengan arah tertentu.\n")
                }
            },
            fontFamily = interFontFamily,
            style = MaterialTheme.typography.bodyLarge
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0XFFE8F9FE))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Sifat-sifat Translasi",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2BA2FF)
                )
                Spacer(modifier = Modifier.height(12.dp))
                stepOfTranslate.forEachIndexed{ index, poin ->
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "${index + 1}. ",
                            fontFamily = interFontFamily,
                        )
                        Text(
                            text = poin,
                            fontFamily = interFontFamily,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        textAlign = TextAlign.Justify,
                        lineHeight = 24.sp
                    )
                ) {
                    append("Perhatikan motif batik Parang berikut! Kita dapat menemukan pola yang dihasilkan dari proses translasi.\n")

                }
            },
            fontFamily = interFontFamily,
            style = MaterialTheme.typography.bodyLarge
        )

        Image(
            painter = painterResource(id = R.drawable.img_parang_ann),
            contentDescription = "Batik Parang Translasi",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Motif yang ditandai dengan warna merah menunjukkan bahwa terdapat bentuk hasil translasi pada motif batik Parang di atas.\n",
            fontFamily = interFontFamily,
            textAlign = TextAlign.Justify
        )
        Text(
            text = "Nah, sekarang perhatikan motif batik di bawah ini:",
            fontFamily = interFontFamily,
            textAlign = TextAlign.Justify
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.img_batik_garutan_sida_luhur),
            contentDescription = "Batik Parang Translasi",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Dapatkah kamu menentukan mana motif yang merepresentasikan translasi dari batik di atas? Yuk kita simak video ini!",
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Justify
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            MyYouTubePlayer(videoId = "baskAXQZxAc")
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp)
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FFF5), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Waktunya Mencoret!",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tandai bagian dari motif batik berikut yang berhubungan dengan hasil translasi",
                    fontFamily = interFontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))
                PictureAnnotation(
                    imageRes = R.drawable.img_batik_crop,
                    explanationImageRes = R.drawable.img_batik_ans,
                    explanationText = "Motif batik di atas merupakan motif Gajah Oling dari Banyuwangi, Jawa Timur. Contoh hasil translasi dapat dilihat pada gambar berikut.\n"

                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (userId != null) {
                    updateAccessiblePage(userId, 2)
                }
                onNext ()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0E60DD)
            )
        ) {
            Text(
                text = "Buka: Refleksi",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}