package com.LambdaProject.MathArt.Data

import androidx.compose.ui.graphics.Color
import com.LambdaProject.MathArt.model.CategoryItem
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.model.AchievementItem
import com.LambdaProject.MathArt.model.MaterialItem
import com.LambdaProject.MathArt.model.OnlineQuizQuestion
import com.LambdaProject.MathArt.model.OnlineQuizDesc
import com.LambdaProject.MathArt.model.QuestionType
import com.LambdaProject.MathArt.model.ScoreType
import com.LambdaProject.MathArt.model.ScorestreakState

val sampleMaterials = listOf(
    MaterialItem("transformasi_geometri", "Transformasi Geometri", R.drawable.img_geo, "11 SMA"),
    MaterialItem("lingkaran","Bangun Datar Lingkaran", R.drawable.img_circ, "11 SMA")
)

val sampleOnlineQuiz = listOf(
    OnlineQuizDesc(
        id = "transformasi_geometri",
        title = "Transformasi Geometri",
        description = "Uji pemahamanmu dalam dunia geometri dengan kuis seru ini! Pelajari bagaimana bentuk-bentuk berubah melalui pencerminan, translasi, rotasi, dan dilatasi.\n\n" +
                "Apakah kamu bisa mengenali pola transformasi dan memprediksi hasilnya dengan tepat?",
        durationMinutes = 17,
        coints = 3000,
        imageRes = R.drawable.img_geo,
        classLevel = "11 SMA",
        numberQuestion = 10
    ),
)

val sampleCategories = listOf(
    CategoryItem("Aritmetika", R.drawable.ic_arithmetic),
    CategoryItem("Aljabar", R.drawable.ic_algebra),
    CategoryItem("Geometri", R.drawable.ico_geometry),
    CategoryItem("Trigonometri", R.drawable.ico_trigo),
    CategoryItem("Kalkulus", R.drawable.ico_calculus),
    CategoryItem("Statistika", R.drawable.ico_statistic)
)

val sampleAchievements = listOf(
    AchievementItem("Penjelajah", R.drawable.ic_explorer, isUnlocked = false),
    AchievementItem("Pemula", R.drawable.img_rocket_pemula, isUnlocked = false),
    AchievementItem("Profesional", R.drawable.img_pros, isUnlocked = false),
    AchievementItem("Ilmuan", R.drawable.ic_scientist, isUnlocked = false),
    AchievementItem("Math Magician", R.drawable.ic_magician, isUnlocked = false),
    AchievementItem("Master Aljabar", R.drawable.ic_algebra_master, isUnlocked = false),
    AchievementItem("Master Geometri", R.drawable.ic_geometry_master, isUnlocked = false),
    AchievementItem("Ahli Ramalan", R.drawable.ic_statistic_master, isUnlocked = false),
    AchievementItem("Ahli Matematika", R.drawable.ic_expert, isUnlocked = false),
    AchievementItem("Ahli Kalkulus", R.drawable.ic_calculus_master, isUnlocked = false),
    AchievementItem("Jenius Matematika", R.drawable.ic_genius, isUnlocked = false)
)

val sampleMaterialStep = mapOf("transformasi_geometri" to listOf(
    "Pengantar",
    "Translasi",
    "Refleksi",
    "Rotasi",
    "Dilatasi",
    "Kuis: Transformasi Geometri"
    )
)

val transform_geo = mapOf(
    "transformasi_geometri" to listOf(
        OnlineQuizQuestion(
            questionNumber = 1,
            questionText = "Bayangan titik A(2,3) jika dicerminkan terhadap sumbu-Y adalah...",
            imageRes = null,
            choices = listOf(
                "(-2,3)",
                "(2,-3)",
                "(-2,-3)",
                "(3,2)"
            ),
            correctAnswers = listOf(0),
            type = QuestionType.MULTIPLE_CHOICE,
            durationSeconds = 90,
            basePoints = 100
        ),
        OnlineQuizQuestion(
            questionNumber = 2,
            questionText = "Tentukan jenis transformasi geometri yang terjadi pada batik di atas",
            imageRes = R.drawable.img_batik_bunga_melati,
            choices = listOf(
                "Dilatasi",
                "Rotasi",
                "Translasi",
                "Refleksi"
            ),
            correctAnswers = listOf(1, 2, 3),
            type = QuestionType.CHECKBOX,
            durationSeconds = 45,
            basePoints = 100
        ),
        OnlineQuizQuestion(
            questionNumber = 3,
            questionText = "Suatu segitiga memiliki titik-titik A(2,1), B(6,1), dan C(2,5). Segitiga ini didilatasi terhadap titik pusat (2,1) dengan faktor skala 1/4. Tentukan luas segitiga hasil dilatasi tersebut!.",
            imageRes = null,
            correctTextAnswers = listOf("0.5", "0,5", "0, 5", "0. 5"),
            type = QuestionType.SHORT_ANSWER,
            durationSeconds = 180,
            basePoints = 300
        ),
        OnlineQuizQuestion(
            questionNumber = 4,
            questionText = "Pilih semua pernyataan yang benar mengenai transformasi geometri:",
            choices = listOf(
                "Refleksi mempertahankan bentuk dan ukuran",
                "Dilatasi bisa mengubah ukuran bangun",
                "Translasi memutar bangun",
                "Rotasi mengubah posisi bangun"
            ),
            correctAnswers = listOf(0, 1, 3),
            type = QuestionType.CHECKBOX,
            durationSeconds = 45,
            basePoints = 100
        ),
        OnlineQuizQuestion(
            questionNumber = 5,
            questionText = "Bayangan titik D(a,b) oleh rotasi terhadap titik pusat O sebesar 90 derajat searah jarum jam adalah D'(-6,4). Tentukan nilai 3a + 2b",
            imageRes = null,
            type = QuestionType.SHORT_ANSWER,
            correctTextAnswers = listOf("-24", "- 24"),
            durationSeconds = 120,
            basePoints = 250
        ),
        OnlineQuizQuestion(
            questionNumber = 6,
            questionText = "Perhatikan soal di atas!",
            imageRes = R.drawable.oq_soal_1,
            type = QuestionType.MULTIPLE_CHOICE,
            choices = listOf(
                "B(24,-26)",
                "B(-26,24)",
                "B(-10, -20)",
                "B(10,20)"
            ),
            correctAnswers = listOf(0),
            durationSeconds = 90,
            basePoints = 150
        ),
        OnlineQuizQuestion(
            questionNumber = 7,
            questionText = "Berikan tanda centang pada koordinat bayangan yang sesuai!",
            imageRes = R.drawable.oq_soal_2,
            type = QuestionType.CHECKBOX,
            choices = listOf(
                "C'(4, 5)",
                "D'(0.7, 2.1)",
                "E'(-2.8, 3)",
                "F'(3.1, 1.2)"
            ),
            correctAnswers = listOf(1,2,3),
            durationSeconds = 120,
            basePoints = 250
        ),
        OnlineQuizQuestion(
            questionNumber = 8,
            questionText = "Tentukan jenis transformasi geometri yang terjadi pada perisai Talawang di atas!",
            imageRes = R.drawable.oq_soal_3,
            type = QuestionType.CHECKBOX,
            choices = listOf(
                "Translasi",
                "Refleksi",
                "Rotasi",
                "Dilatasi"
            ),
            correctAnswers = listOf(0,1),
            durationSeconds = 45,
            basePoints = 100
        ),
        OnlineQuizQuestion(
            questionNumber = 9,
            questionText = "Tentukan jenis transformasi geometri yang terjadi pada batik di atas",
            imageRes = R.drawable.img_kain_besurek,
            type = QuestionType.CHECKBOX,
            choices = listOf(
                "Translasi",
                "Refleksi",
                "Rotasi",
                "Dilatasi"
            ),
            correctAnswers = listOf(0,2,3),
            durationSeconds = 45,
            basePoints = 100
        ),
        OnlineQuizQuestion(
            questionNumber = 10,
            questionText = "Bayangan kurva y = 2x² - 4x + 5 jika dicerminkan terhadap sumbu Y, lalu dilanjutkan dengan dilatasi terhadap pusat (1, −2) dan faktor skala 2 adalah ...",
            imageRes = null,
            choices = listOf(
                "x² − 6x − y + 17 = 0",
                "2x² + 4x − y + 5 = 0",
                "x² + 6x + y + 17 = 0",
                "x² + 6x − y + 17 = 0"
            ),
            correctAnswers = listOf(3),
            type = QuestionType.MULTIPLE_CHOICE,
            durationSeconds = 240,
            basePoints = 500
        ),
    )
)

val powerUpColors = listOf(
    Color(0xFF5E35B1),
    Color(0xFF0097A7),
    Color(0xFFF57C00)
)

val powerUpIcons = listOf(
    R.drawable.ic_pu_freeze,
    R.drawable.ic_pu_magic,
    R.drawable.ic_pu_shield
)

val materialGoals = mapOf("transformasi_geometri" to listOf(
    "Menemukan konsep matematika dalam motif batik yang akan dipelajari. Pada sesi ini, konsep matematika yang akan ditemukan berkaitan dengan transformasi geometri",
    )
)

val materialDescriptions = mapOf(
    "transformasi_geometri" to "Materi ini membahas transformasi geometri menggunakan pendekatan etnomatematika. Misalnya, bagaimana refleksi muncul dalam motif ukiran tradisional, atau translasi dalam pola batik.",
    "lingkaran" to "Materi ini mengeksplorasi konsep lingkaran melalui budaya lokal seperti pola tenun atau arsitektur tradisional. Siswa akan memahami elemen-elemen lingkaran secara kontekstual dan matematis."
)

val stepOfTranslate = listOf(
    "Bangun geometri yang digeser (ditranslasikan) tidak mengalami perubahan bentuk dan ukuran.",
    "Bangun geometri yang digeser (ditranslasikan) mengalami perubahan posisi."
)

val stepOfReflect = listOf(
    "Bangun geometri yang dicerminkan (direfleksikan) tidak mengalami perubahan bentuk dan ukuran.",
    "Bangun geometri yang dicerminkan (direfleksikan) mengalami perubahan posisi dengan jarak objek ke cermin sama dengan jarak bayangan ke cermin."
)

val stepOfRotate = listOf(
    "Bangun geometri yang diputar (dirotasikan) tidak mengalami perubahan bentuk dan ukuran.",
    "Bangun geometri yang diputar (dirotasikan) mengalami perubahan posisi."
)

val stepOfDilate = listOf(
    "Bangun geometri yang diperbesar (didilatasi) akan mengalami perubahan ukuran tetapi tidak mengalami peruabah bentuk jika faktor skala yang ditentukan bernilai positif.",
    "Bangun geometri yang diperbesar (didilatasi) akan mengalami perubahan ukuran dan bentuk jika faktor skala yang ditentukan bernilai negatif."
)

val infoSenimatika = listOf(
    "Senimatika merupakan platform pembelajaran matematika interaktif yang menggabungkan konsep matematika dengan kekayaan budaya lokal melalui pendekatan etnomatematika.\n\n" +
            "Senimatika hadir untuk menjadikan proses belajar lebih kontekstual, menyenangkan, dan bermakna.\n"
)

val grades = listOf("SMP", "SMA")

val kelasMap = mapOf(
    "SMP" to listOf("7", "8", "9"),
    "SMA" to listOf("10", "11", "12")
)

val sampleStates = listOf(
    ScorestreakState(
        type = ScoreType.GOOD,
        imageRes = R.drawable.ic_ss_good,
        title = "Good!",
        subtitle = "Kamu menjawab 1 soal dengan benar"
    ),
    ScorestreakState(
        type = ScoreType.COOL,
        imageRes = R.drawable.ic_ss_cool,
        title = "Cool!",
        subtitle = "Kamu menjawab 2 soal beruntun dengan benar"
    ),
    ScorestreakState(
        type = ScoreType.AWESOME,
        imageRes = R.drawable.ic_ss_awesome,
        title = "Awesome!!",
        subtitle = "Kamu menjawab benar lebih dari 2 soal secara beruntun"
    ),
    ScorestreakState(
        type = ScoreType.UPS,
        imageRes = R.drawable.ic_ss_wrong1,
        title = "Ups, coba lagi!!",
        subtitle = "Kamu menjawab salah 1 soal"
    ),
    ScorestreakState(
        type = ScoreType.NOT_FOCUSED,
        imageRes = R.drawable.ic_ss_wrong2,
        title = "Gak fokus ya??",
        subtitle = "Kamu menjawab salah lebih dari 2 soal beruntun"
    ),
    ScorestreakState(
        type = ScoreType.TIME_OUT,
        imageRes = R.drawable.ic_ss_wrong2,
        title = "Waktu habis!",
        subtitle = "Jawab soal berikutnya dengan lebih cepat"
    )
)
