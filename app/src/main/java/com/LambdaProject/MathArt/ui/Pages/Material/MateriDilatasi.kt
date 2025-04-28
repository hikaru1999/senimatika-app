package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.Data.stepOfDilate
import com.LambdaProject.MathArt.*
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriDilatasi(onQuizNavigate: () -> Unit, onNext: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_reflection),
            contentDescription = "Refleksi",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Dilatasi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
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
                    append("Dilatasi adalah transformasi yang mengubah bentuk bangun geometri dengan memperkecil atau memperbesar tanpa mengubah bentuk asli.\n\n")
                    append("Perubahan disebabkan karena adanya faktor skala dan titik pusat dilatasi.\n")
                }
            },
            fontFamily = interFontFamily,
            style = MaterialTheme.typography.bodyLarge
        )
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(Color(0XFFE8F9FE))
            .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Sifat-sifat Dilatasi",
                    fontSize = 18.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2BA2FF)
                )
                Spacer(modifier = Modifier.height(12.dp))
                stepOfDilate.forEachIndexed{ index, poin ->
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
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        textAlign = TextAlign.Justify,
                        lineHeight = 24.sp
                    )
                ) {
                    append("Pada motif batik, kita dapat menemukan bentuk/pola yang dihasilkan dari proses dilatasi. Perhatikan motif batik di bawah:\n")

                }
            },
            fontFamily = interFontFamily,
            style = MaterialTheme.typography.bodyLarge
        )
        Image(
            painter = painterResource(id = R.drawable.img_batik_dilatasi),
            contentDescription = "Batik Dilatasi",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Motif yang ditandai dengan warna putih menunjukkan bahwa terdapat hasil dilatasi pada motif batik tersebut.",
            fontFamily = interFontFamily,
            textAlign = TextAlign.Justify
        )
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
                    text = "Tandai bagian dari motif batik berikut yang berhubungan dengan hasil dilatasi",
                    fontFamily = interFontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))
                PictureAnnotation(
                    imageRes = R.drawable.img_batik_sekar_draw,
                    explanationImageRes = R.drawable.img_batik_sekar_ans,
                    explanationText = "Motif batik di atas merupakan motif Sekar Jagad yang berasal dari Solo dan Yogyakarta. Contoh hasil dilatasi dapat dilihat pada gambar berikut.\n"

                )
            }
        }
        Button(
            onClick = {
                if (userId != null) {
                    updateAccessiblePage(userId, 5)
                }
                onQuizNavigate()
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
                text = "Ambil Kuis",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}