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
import com.LambdaProject.MathArt.*
import com.LambdaProject.MathArt.Data.stepOfReflect
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriRefleksi(onNext: () -> Unit) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val correctAnswer = "Sido Mulyo"
    val isCorrect = selectedOption == correctAnswer

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
            text = "Refleksi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        textAlign = TextAlign.Justify,
                        lineHeight = 24.sp
                    )
                ) {
                    append("Refleksi merupakan perpindahan geometri yang memindahkan setiap titik suatu objek dengan menggunakan sifat bayangan cermin.\n")

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
                    text = "Sifat-sifat Refleksi",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0XFF2BA2FF)
                )
                Spacer(modifier = Modifier.height(12.dp))
                stepOfReflect.forEachIndexed{ index, poin ->
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
                    append("Perhatikan motif batik berikut! Kita dapat menemukan pola yang dihasilkan dari proses refleksi.\n")

                }
            },
            fontFamily = interFontFamily,
            style = MaterialTheme.typography.bodyLarge
        )
        Image(
            painter = painterResource(id = R.drawable.img_batik_refleksi),
            contentDescription = "Batik Refleksi",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Motif yang ditandai dengan warna merah menunjukkan bahwa terdapat bentuk hasil refleksi pada motif batik di atas.",
            fontFamily = interFontFamily,
            textAlign = TextAlign.Justify
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FFF5), shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Kuis Singkat!",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Berdasarkan ilustrasi di atas, apa nama dari motif batik tersebut?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interFontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Sido Mulyo", "Sido Luhur").forEach { option ->
                        val isSelected = selectedOption == option
                        OutlinedButton(
                            onClick = {
                                if (!isAnswered) {
                                    selectedOption = option
                                    isAnswered = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) Color(0xFFF4F4F4) else Color(0xFFFFFFFF)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Text(
                                text = option,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isAnswered && isSelected && isCorrect) Color.Blue else Color.Black
                            )
                        }
                    }
                }

                if (isAnswered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = ParagraphStyle(
                                    textAlign = TextAlign.Justify,
                                    lineHeight = 22.sp
                                )
                            ) {
                                if (isCorrect) {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Jawaban kamu benar! motif batik di atas merupakan Sido Mulyo.\n\n")
                                    }
                                } else {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Jawaban kamu belum tepat. Motif batik di atas merupakan Sido Mulyo.\n\n")
                                    }
                                }
                                append("Batik Sido Mulyo berasal dari Surakarta, Jawa Tengah, dan Daerah Istimewa Yogyakarta. Motif ini memiliki makna yaitu kebahagiaan dan ketentraman.\n\n")
                                append("Biasanya, motif batik ini digunakan dalam acara pernikahan adat Jawa")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = interFontFamily
                    )
                    Spacer(modifier = Modifier.align(Alignment.End))
                }

            }
        }
        Spacer(Modifier.height(16.dp))
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
                    text = "Tandai bagian dari motif batik berikut yang berhubungan dengan hasil refleksi",
                    fontFamily = interFontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))
                PictureAnnotation(
                    imageRes = R.drawable.img_batik_sido_asih,
                    explanationImageRes = R.drawable.img_batik_sido_asih_ans,
                    explanationText = "Motif batik di atas merupakan motif Sido Asih dari Daerah Istimewa Yogyakarta. Contoh hasil refleksi dapat dilihat pada gambar berikut.\n"
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (userId != null) {
                    updateAccessiblePage(userId, 3)
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
                text = "Buka: Rotasi",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}