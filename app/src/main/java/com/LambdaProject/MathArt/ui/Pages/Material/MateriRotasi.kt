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
import com.LambdaProject.MathArt.Data.stepOfRotate
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MateriRotasi(onNext: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_rotation),
            contentDescription = "Rotation",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Rotasi",
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
                    append("Rotasi merupakan perpindahan geometri dengan cara diputar lewat suatu pusat dan sudut tertentu.\n\n")
                    append("Pada rotasi, terdapat sudut sebesar (a) dan pusat (x,y) yang telah disepakati. Besarnya sudut pada rotasi berlawanan arah dengan jarum jam.\n\n")
                    append("Jika sudut pada rotasi searah dengan jarum jam maka besarnya (-a). \n")
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
                    text = "Sifat-sifat Translasi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF2BA2FF)
                )
                Spacer(modifier = Modifier.height(12.dp))
                stepOfRotate.forEachIndexed{ index, poin ->
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
            text = "Perhatikan motik batik di bawah ini!",
            fontFamily = interFontFamily,
            textAlign = TextAlign.Justify,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Penerapan rotasi suatu objek dapat dilihat pada motif Simbut berikut",
            fontFamily = interFontFamily,
            textAlign = TextAlign.Justify,
        )
        Image(
            painter = painterResource(id = R.drawable.img_batik_simbut),
            contentDescription = "Batik Refleksi",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (userId != null) {
                    updateAccessiblePage(userId, 4)
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
                text = "Buka: Dilatasi",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}