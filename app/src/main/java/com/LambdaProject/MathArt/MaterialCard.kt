package com.LambdaProject.MathArt

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.model.MaterialItem

@Composable
fun MaterialCard(material: MaterialItem, isActive: Boolean, onClickLearn: (MaterialItem) -> Unit) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .padding(8.dp)
            .border(
                width = 1.dp,
                color = Color(0xff5E9DFF),
                shape = RoundedCornerShape(10.dp)
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xffFFFFFF)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(id = material.imageRes),
                contentDescription = material.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = material.title, fontWeight = FontWeight.Bold, fontFamily = interFontFamily, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = "Grade",
                    modifier = Modifier.size(25.dp),
                    tint = Color(0xff6695CD)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = material.classLevel, fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onClickLearn(material) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFF0E60DD)),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(
                        text = if (isActive) "Lanjut Belajar" else "Mulai Belajar",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xffFFFFFF)
                    )
                }
            }
        }
    }
}