package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriPengantar(onNext: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    val correctAnswer = "Parang"
    val isCorrect = selectedOption == correctAnswer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Pengantar",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        textAlign = TextAlign.Justify,
                        lineHeight = 24.sp
                    )
                ) {
                    append("Etnomatematika adalah sebuah ilmu yang mempelajari tentang budaya suatu bangsa dan mengaitkannya dengan Matematika.\n\n")
                    append("Pada kesempatan ini, akan dibahas kaitan salah satu budaya Indonesia yaitu batik yang dihubungkan dengan materi Matematika. ")
                    append("Batik merupakan salah satu kekayaan budaya Indonesia yang dimiliki oleh seluruh daerah yang tersebar di Indonesia.\n\n")
                    append("Selain itu, dunia juga mengakui bahwa batik merupakan salah satu unsur budaya bangsa Indonesia. ")
                    append("Oleh karena itu, kita harus bangga dengan batik yang kita miliki.")
                }
            },
            fontFamily = interFontFamily,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
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
                    text = "Tentukan nama dari motif Batik ini:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interFontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.img_batik_parang),
                    contentDescription = "Motif Batik",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Kawung", "Parang").forEach { option ->
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
                                        append("Jawaban kamu benar! Ini adalah motif batik Parang.\n\n")
                                    }
                                } else {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Jawaban kamu belum tepat. Ini adalah motif batik Parang.\n\n")
                                    }
                                }
                                append("Motif Parang memiliki bentuk seperti ombak yang berulang dan melambangkan semangat pantang menyerah. ")
                                append("Motif ini sering digunakan oleh kalangan bangsawan Jawa sebagai simbol kekuatan dan keberanian.")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = interFontFamily
                    )
                    Spacer(modifier = Modifier.align(Alignment.End))
                }

            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Transformasi Geometri",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        textAlign = TextAlign.Justify,
                        lineHeight = 24.sp
                    )
                ) {
                    append("Transformasi geometri adalah sebuah ilmu yang mempelajari proses perubahan suatu bidang geometri yang meliputi posisi, besar, dan bentuknya sendiri yang diakibatkan karena translasi, refleksi, rotasi, dilatasi, transformasi bersesuaian, perubahan skala, dan komposisi dua transformasi.\n\n")
                    append("Transformasi geometri juga dapat dinyatakan sebagai perubahan pada sebuah bidang geometri yang mencamtukan posisi, besar, dan bentuknya sendiri.\n\n")
                    append("Pada kesempatan pembelajaran ini, transformasi geometri akan dibatasi menjadi translasi, refleksi, rotasi, dan dilatasi.")
                }
            },
            fontFamily = interFontFamily,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (userId != null) {
                    updateAccessiblePage(userId, 1)
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
                text = "Buka: Translasi",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}