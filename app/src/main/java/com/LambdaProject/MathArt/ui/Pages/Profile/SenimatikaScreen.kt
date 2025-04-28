package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenimatikaScreen(navController: NavController) {
    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tentang Senimatika",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo_blue),
                contentDescription = "Logo Senimatika",
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = ParagraphStyle(
                            textAlign = TextAlign.Justify,
                            lineHeight = 20.sp
                        )
                    ) {
                        append("Senimatika merupakan platform pembelajaran matematika interaktif yang menggabungkan konsep matematika dengan kekayaan budaya lokal melalui pendekatan etnomatematika. ")
                        append("Senimatika hadir untuk menjadikan proses belajar lebih kontekstual, menyenangkan, dan bermakna.\n")
                    }
                },
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Senimatika dipresentasikan oleh:",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_unpar),
                    contentDescription = "Presenter Logo",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop
                )
                Image(
                    painter = painterResource(id = R.drawable.img_unpar_stem),
                    contentDescription = "Presenter Logo",
                    modifier = Modifier.height(40.dp).wrapContentWidth(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Senimatika dikembangkan oleh:",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = ParagraphStyle(
                                    textAlign = TextAlign.Justify,
                                    lineHeight = 16.sp
                                )
                            ) {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp )) {
                                    append("Ardhika Fajar Ramadhan, M.Pd\n")
                                }
                                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("EdTech Enthusiast\n")
                                }
                                append("Sleman, DIY, Indonesia")

                            }
                        },
                        fontFamily = interFontFamily,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {  }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_linkedin),
                        contentDescription = "LinkedIn",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {  }
                    )
                }

                /* Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                } */
            }
        }
    }
}