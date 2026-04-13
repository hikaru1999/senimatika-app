package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.ViewModels.ValidatorViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.Decision
import com.LambdaProject.MathArt.data.model.ValidatorQuestion
import com.LambdaProject.MathArt.data.model.ValidatorRole
import com.LambdaProject.MathArt.data.model.displayText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationSummaryScreen(
    viewModel: ValidatorViewModel = hiltViewModel(),
    role: ValidatorRole,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    val result by viewModel.result.collectAsState()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    LaunchedEffect(Unit) {
        viewModel.loadKuesionerResult(role)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ringkasan Penilaian",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily
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
        result?.let { data ->
            val enumRole = ValidatorRole.valueOf(data.role.uppercase())
            val decisionColor = when (data.decision) {
                Decision.LAYAK -> Color(0xFF2E7D32) // Hijau
                Decision.LAYAK_DENGAN_REVISI -> Color(0xFFEF6C00) // Oranye
                Decision.TIDAK_LAYAK -> Color(0xFFC62828) // Merah
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding)
                    .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                FormSection(title = "Identitas Validator") {
                    Text(
                        text = "Nama: ${data.name}",
                        fontFamily = interFontFamily
                    )
                    Text(
                        text = "Institusi: ${data.institution}",
                        fontFamily = interFontFamily
                    )
                    Text(
                        text = "Validator Ahli: ${enumRole.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        fontFamily = interFontFamily
                    )
                }
                FormSection(title = "Kesimpulan Validasi") {
                    Text(
                        buildAnnotatedString {
                            append("Kesimpulan: ")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = decisionColor)
                            ) {
                                append(data.decision.displayText)
                            }
                        },
                        fontFamily = interFontFamily
                    )
                    Text(
                        text = "Divalidasi pada: ${formatTimestamp(data.submittedAt)}",
                        fontFamily = interFontFamily
                    )
                }
                FormSection(title = "Rincian Validasi") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        val questions = remember(data.role) {
                            runCatching {
                                when (ValidatorRole.valueOf(data.role.uppercase())) {
                                    ValidatorRole.MATERI -> ValidatorQuestion.allQuestions.filter { it.id.startsWith("m") && !it.id.startsWith("me") }
                                    ValidatorRole.MEDIA -> ValidatorQuestion.allQuestions.filter { it.id.startsWith("me") }
                                }
                            }.getOrElse {
                                emptyList()
                            }
                        }

                        questions.forEachIndexed { index, question ->
                            val answer = data.responses[question.id] ?: 3
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFF7F9FC),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "${index + 1}. ${question.text}",
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Justify
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LikertScaleSlider(
                                        selectedValue = answer,
                                        onSelect = { },
                                        enabled = false
                                    )
                                }
                            }
                        }
                    }
                }
                FormSection(title = "Komentar") {
                    data.comment?.let {
                        Text(
                            text = it,
                            fontFamily = interFontFamily
                        )
                    }
                }
                Button(
                    onClick = {
                        isLoading = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(2500)
                            navController.popBackStack()
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Text(
                            text = "Kembali",
                            color = Color.White,
                            fontFamily = interFontFamily
                        )
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
    return sdf.format(Date(timestamp))
}
