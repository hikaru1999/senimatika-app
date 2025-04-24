package com.LambdaProject.MathArt

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.model.CategoryItem

@Composable
fun CategoryCard(category: CategoryItem) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD7EBFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = category.imageRes),
                contentDescription = category.name,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(5.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}