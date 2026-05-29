package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.LambdaProject.MathArt.data.infoSenimatika
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenimatikaScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(0xFFF8F9FE),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tentang Senimatika",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A237E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Kembali",
                            tint = Color(0xFF1A237E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFFE3F2FD))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_logo_blue),
                    contentDescription = "Logo Senimatika",
                    modifier = Modifier.size(160.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Column(
                modifier = Modifier
                    .offset(y = (-24).dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Apa itu Senimatika?",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF1A237E)
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = infoSenimatika[0],
                    textAlign = TextAlign.Justify,
                    fontFamily = interFontFamily,
                    lineHeight = 22.sp,
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                DividerSection(title = "Dipublikasikan oleh")
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_unpar),
                        contentDescription = "UNPAR",
                        modifier = Modifier.height(60.dp).wrapContentWidth(),
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_unpar_stem),
                        contentDescription = "UNPAR STEM",
                        modifier = Modifier.height(60.dp).wrapContentWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                DividerSection(title = "Tim Pengembang")
                
                Spacer(modifier = Modifier.height(16.dp))

                DeveloperCard(
                    role = "Lead Project",
                    name = "Melania Eva Wulanningtyas, M.Pd",
                    institution = "Universitas Katolik Parahyangan",
                    location = "Bandung, Jawa Barat, Indonesia"
                )

                Spacer(modifier = Modifier.height(12.dp))

                DeveloperCard(
                    role = "Design & Programming",
                    name = "Ardhika Fajar Ramadhan, M.Pd",
                    institution = "EdTech Enthusiast",
                    location = "Sleman, DIY, Indonesia"
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DividerSection(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF1A237E)
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFFEEEEEE))
    }
}

@Composable
fun DeveloperCard(role: String, name: String, institution: String, location: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8F9FE),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE8EAF6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = role,
                fontFamily = interFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontFamily = interFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A237E)
            )
            Text(
                text = institution,
                fontFamily = interFontFamily,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = Color.DarkGray
            )
            Text(
                text = location,
                fontFamily = interFontFamily,
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ContactIcon(iconRes = R.drawable.ic_assessment)
                ContactIcon(iconRes = R.drawable.ic_logo_linkedin)
            }
        }
    }
}

@Composable
fun ContactIcon(iconRes: Int) {
    Surface(
        modifier = Modifier.size(32.dp).clickable { },
        color = Color.White,
        shape = CircleShape,
        border = BorderStroke(1.dp, Color(0xFFE8EAF6))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF1A237E)
            )
        }
    }
}
