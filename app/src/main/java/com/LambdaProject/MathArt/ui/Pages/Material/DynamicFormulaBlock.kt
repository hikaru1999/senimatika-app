package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.data.model.ContentBlock
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText

@Composable
fun DynamicFormulaBlock(block: ContentBlock) {
    val variables = block.variables ?: emptyList()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF0F7FF),
        border = BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Badge on top, Title below
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2563EB)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = block.badge?.ifBlank { "FORMULA" } ?: "FORMULA",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            fontFamily = interFontFamily,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (!block.title.isNullOrBlank()) {
                    val titleText = block.title
                    val titleHasMath = remember(titleText) {
                        titleText.contains("$") || titleText.contains("\\")
                    }
                    if (titleHasMath) {
                        MathText(
                            text = titleText,
                            fontSize = 17,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    } else {
                        Text(
                            text = titleText,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            // Formula Showcase Box
            if (!block.formula.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MathText(
                            text = block.formula,
                            fontSize = 18,
                            color = Color(0xFF1E3A8A),
                            textAlign = "center",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Formula Description
            if (!block.description.isNullOrBlank()) {
                val descHasMath = remember(block.description) {
                    block.description.contains("$") || block.description.contains("\\")
                }
                if (descHasMath) {
                    MathText(
                        text = block.description,
                        fontSize = 14,
                        color = Color(0xFF334155)
                    )
                } else {
                    Text(
                        text = block.description,
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF334155),
                        lineHeight = 22.sp
                    )
                }
            }

            // Variable Breakdown Table (Keterangan Card matching Image 2)
            val validVariables = remember(variables) {
                variables.filter { it.symbol.isNotBlank() || it.meaning.isNotBlank() }
            }
            if (validVariables.isNotEmpty()) {
                val variablesContent = remember(validVariables) {
                    validVariables.joinToString("\n\n") { variable ->
                        val sym = variable.symbol.trim()
                        val mathSym = when {
                            sym.isBlank() -> ""
                            sym.contains("$") -> sym
                            else -> "$$sym$"
                        }
                        val meaning = variable.meaning.trim()
                        if (meaning.isNotBlank()) {
                            "$mathSym = $meaning"
                        } else {
                            mathSym
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.2.dp, Color(0xFF99F6E4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "KETERANGAN:",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color(0xFF0F766E),
                            letterSpacing = 0.5.sp
                        )

                        MathText(
                            text = variablesContent,
                            fontSize = 14,
                            color = Color(0xFF334155),
                            textAlign = "left"
                        )
                    }
                }
            }

            // Tips / Note
            if (!block.note.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tips",
                            tint = Color(0xFFD97706),
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        val noteText = block.note
                        val noteHasMath = remember(noteText) {
                            noteText.contains("$") || noteText.contains("\\")
                        }
                        if (noteHasMath) {
                            MathText(
                                text = noteText,
                                fontSize = 12,
                                color = Color(0xFF92400E)
                            )
                        } else {
                            Text(
                                text = noteText,
                                fontFamily = interFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
