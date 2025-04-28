package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun ProfileHeader(username: String, email: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xffF7FAFF))
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Foto Profil",
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = username,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = email,
                    fontFamily = interFontFamily,
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}