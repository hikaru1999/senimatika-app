package com.LambdaProject.MathArt.ui.Pages.Profile

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.LambdaProject.MathArt.Data.stepOfTranslate
import com.LambdaProject.MathArt.ViewModels.ValidatorViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.Decision
import com.LambdaProject.MathArt.model.KuesionerResult
import com.LambdaProject.MathArt.model.ValidatorQuestion
import com.LambdaProject.MathArt.model.ValidatorRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireMasterScreen(
    viewModel: ValidatorViewModel,
    role: ValidatorRole,
    allQuestions: List<ValidatorQuestion>,
    userId: String,
    onSuccessSubmit: () -> Unit,
    navController: NavController
) {
    val session = viewModel.session
    var name by remember { mutableStateOf(session?.name.orEmpty()) }
    var institution by remember { mutableStateOf(session?.institution.orEmpty()) }
    var decision by remember { mutableStateOf<Decision?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var isLoadingConfirm by remember { mutableStateOf(false) }
    var isLoadingSubmit by remember { mutableStateOf(false) }

    val responses = remember(session) {
        mutableStateMapOf<String, Int>().apply {
            session?.responses?.forEach {
                this[it.questionId] = it.selectedValue
            }
        }
    }
    var comment by remember { mutableStateOf(session?.comment.orEmpty()) }

    val questions = remember(role, allQuestions) {
        when (role) {
            ValidatorRole.MATERI -> allQuestions.filter { it.id.startsWith("m") && !it.id.startsWith("me") }
            ValidatorRole.MEDIA -> allQuestions.filter { it.id.startsWith("me") }
        }
    }

    val deviceInfo = remember {
        if (role == ValidatorRole.MEDIA) {
            "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
        } else null
    }

    LaunchedEffect(session) {
        name = session?.name.orEmpty()
        institution = session?.institution.orEmpty()
        comment = session?.comment.orEmpty()

        session?.responses?.forEach {
            responses[it.questionId] = it.selectedValue
        }
    }

    LaunchedEffect(userId, role) {
        viewModel.loadSession(userId, role)
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Keluar dari Form?",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Isian Anda akan disimpan. Anda bisa melanjutkan pengisian di lain waktu.",
                    fontFamily = interFontFamily
                )
            },
            containerColor = Color.White,
            confirmButton = {
                TextButton(
                    onClick = {
                        isLoadingConfirm = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(3000)
                            viewModel.cacheSessionFromUI(
                                name = name,
                                institution = institution,
                                comment = comment,
                                responses = responses
                            )
                            showExitDialog = false
                            navController.popBackStack()
                            isLoadingConfirm = false
                        }
                    }
                ) {
                    if(isLoadingConfirm) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Text(
                            text = "Ya, Simpan & Keluar",
                            fontFamily = interFontFamily,
                            color = Color.Black
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(
                        text = "Batal",
                        fontFamily = interFontFamily,
                        color = Color.Gray
                    )
                }
            }
        )
    }

    BackHandler {
        showExitDialog = true
    }

    if (showSubmitDialog) {
        SubmitConfirmationDialog(
            onDismiss = { showSubmitDialog = false },
            onConfirm = {
                responses.forEach { (questionId, selectedValue) ->
                    viewModel.updateResponse(questionId, selectedValue)
                }

                viewModel.submit(name, institution, decision!!, comment, deviceInfo) {
                    onSuccessSubmit()

                    viewModel.clearCachedSession(userId, role)

                    viewModel.loadOwnKuesionerResult { role ->
                        navController.navigate("validation_summary/${role.name.lowercase()}")
                    }
                }

                showSubmitDialog = false
            }
        )
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Beri Penilaian Aplikasi",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                .background(Color.White)
                .padding(padding)
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF42A5F5), // Biru muda
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                FormSection(title = "Identitas Validator", titleColor = Color.White) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text(
                                text = "Nama Lengkap Beserta Gelar",
                                fontFamily = interFontFamily,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = institution,
                        onValueChange = { institution = it },
                        label = { Text("Instansi", fontFamily = interFontFamily,) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0XFFE8F9FE))
                    .padding(16.dp)
                    .clip(shape = RoundedCornerShape(12.dp))
            ) {
                Column {
                    Text(
                        text = "Semua butir pernyataan wajib ditanggapi. Validator bisa memberikan komentar/saran pada kolom komentar yang telah disediakan.\n",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2BA2FF),
                        textAlign = TextAlign.Justify
                    )
                    Text(
                        text = "Jika ingin menjawab \"Netral\", maka geser slider terlebih dahulu ke posisi lain dan kemudian geser kembali ke posisi semula (Netral)",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2BA2FF),
                        textAlign = TextAlign.Justify
                    )
                }
            }

            // 🟩 2. Seksi Kuesioner
            FormSection(title = "Pernyataan") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    questions.forEachIndexed { index, question ->
                        val existing = session?.responses?.firstOrNull { it.questionId == question.id }?.selectedValue

                        if (responses[question.id] == null && existing == null) {
                            responses[question.id] = 1
                        }

                        LaunchedEffect(question.id) {

                        }

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
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Justify
                                )
                                val selectedValue = responses[question.id] ?: 1
                                Spacer(modifier = Modifier.height(12.dp))
                                LikertScaleSlider(
                                    selectedValue = responses[question.id] ?: selectedValue,
                                    onSelect = { responses[question.id] = it }
                                )
                            }
                        }
                    }
                }
            }

            FormSection(title = "Komentar") {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Komentar atau saran perbaikan...") },
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF6C795),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                FormSection(title = "Kesimpulan Penilaian") {
                    Text(
                        text = "Media pembelajaran berbasis etnomatematika yang dikembangkan dinyatakan:",
                        fontFamily = interFontFamily,
                        textAlign = TextAlign.Justify
                    )
                    DecisionSelector(selected = decision, onSelect = { decision = it })
                }
            }

            Button(
                onClick = {
                    isLoadingSubmit = true
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2500)
                        showSubmitDialog = true
                        isLoadingSubmit = false
                    }
                },
                enabled = name.isNotBlank() && comment.isNotBlank() && decision != null && (responses.size == questions.size),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
            ) {
                if (isLoadingSubmit) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 4.dp
                    )
                } else {
                    Text(
                        text = "Kirim Penilaian",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun LikertScaleSlider(
    selectedValue: Int?,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true
) {
    val labels = listOf(
        "Sangat Tidak Setuju",
        "Tidak Setuju",
        "Setuju",
        "Sangat Setuju"
    )

    val sliderValue = (selectedValue ?: 1).toFloat()
    val roundedValue = sliderValue.roundToInt().coerceIn(1, 4)

    Column {
        Text(
            text = labels[roundedValue - 1],
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.CenterHorizontally)
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            val totalWidth = constraints.maxWidth.toFloat()
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.CenterStart)) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE53935),
                            Color(0xFFFFEB3B),
                            Color(0xFF43A047)
                        )
                    ),
                    size = size
                )
            }

            Slider(
                value = sliderValue,
                onValueChange = { onSelect(it.roundToInt().coerceIn(1, 4)) },
                valueRange = 1f..4f,
                steps = 2,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF5E9DFF),
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..4).forEach { number ->
                Text(
                    text = number.toString(),
                    fontFamily = interFontFamily,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
fun FormSection(
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun DecisionSelector(selected: Decision?, onSelect: (Decision) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Decision.values().forEach { decision ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selected == decision,
                    onClick = { onSelect(decision) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.Black,
                    )
                )
                Text(
                    text = when (decision) {
                        Decision.LAYAK -> "Layak digunakan"
                        Decision.LAYAK_DENGAN_REVISI -> "Layak digunakan dengan revisi"
                        Decision.TIDAK_LAYAK -> "Tidak layak digunakan"
                    },
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SubmitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }
    var typedText by remember { mutableStateOf("") }
    val requiredText = "Saya telah merespons semua pernyataan"
    var isLoadingFinal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Konfirmasi Pengisian",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = Color.White,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Anda hanya dapat mengirimkan kuesioner ini satu kali. Pastikan Anda telah mengisi seluruh butir pernyataan dengan benar.",
                    fontFamily = interFontFamily
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saya setuju",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Ketik kalimat berikut pada kolom yang tersedia (tanpa tanda petik):",
                    fontFamily = interFontFamily
                )
                Text(
                    text = "\"$requiredText\"",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    isLoadingFinal = true
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(3000)
                        if (isChecked && typedText == requiredText) {
                            onConfirm()
                        }
                        isLoadingFinal = false
                    }
                },
                enabled = isChecked && typedText == requiredText
            ) {
                if (isLoadingFinal) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 4.dp
                    )
                } else {
                    Text(
                        text = "Finalisasi Penilaian",
                        fontFamily = interFontFamily,
                        color = Color.Black
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    fontFamily = interFontFamily,
                    color = Color.Gray)
            }
        }
    )
}



