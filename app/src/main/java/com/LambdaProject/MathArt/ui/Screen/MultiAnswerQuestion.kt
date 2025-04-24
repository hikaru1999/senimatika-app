package com.LambdaProject.MathArt.ui.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun MultiAnswerQuestion(
    question: String,
    options: List<String>,
    correctAnswer: Set<Int>,
    onAnswered: (Boolean) -> Unit
) {
    val selectedAnswer = remember { mutableStateListOf<Int>() }
    var showResult by remember { mutableStateOf(false) }

    Text(text = question, fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    options.forEachIndexed{ index, option ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (selectedAnswer.contains(index)) {
                        selectedAnswer.remove(index)
                    } else {
                        selectedAnswer.add(index)
                    }
                }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = selectedAnswer.contains(index),
                onCheckedChange = {
                    if (it) selectedAnswer.add(index)
                    else selectedAnswer.remove(index)
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = option, fontFamily = interFontFamily)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            showResult = true
            val isCorrect = selectedAnswer.toSet() == correctAnswer
            onAnswered(isCorrect)
        }
    ) {
        Text("Periksa", fontFamily = interFontFamily)
    }

    if (showResult) {
        Spacer(modifier = Modifier.height(12.dp))
        val isCorrect = selectedAnswer.toSet() == correctAnswer
        Text(
            text = if (isCorrect) "Jawaban kamu benar!" else "Jawaban kamu belum tepat!",
            color = if(isCorrect) Color(0xFF2BA2FF) else Color.Red,
            fontWeight = FontWeight.Bold,
            fontFamily = interFontFamily
        )
    }
}