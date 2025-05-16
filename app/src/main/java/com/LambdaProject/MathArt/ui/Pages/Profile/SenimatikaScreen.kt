package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.LambdaProject.MathArt.Data.infoSenimatika
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
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
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
                text = infoSenimatika[0],
                textAlign = TextAlign.Justify,
                fontFamily = interFontFamily,
            )
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Dipublikasikan oleh:",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_unpar),
                        contentDescription = "Presenter Logo",
                        modifier = Modifier.size(75.dp),
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_unpar_stem),
                        contentDescription = "Presenter Logo",
                        modifier = Modifier.height(75.dp).wrapContentWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Dikembangkan oleh:",
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
                        text = "Lead Project:",
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = ParagraphStyle(
                                    textAlign = TextAlign.Justify,
                                    lineHeight = 16.sp
                                )
                            ) {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp )) {
                                    append("Melania Eva Wulanningtyas, M.Pd\n")
                                }
                                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("Universitas Katolik Parahyangan\n")
                                }
                                append("Bandung, Jawa Barat, Indonesia")

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
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Design & Programming: ",
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
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
            }
        }
    }
}