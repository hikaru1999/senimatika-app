package com.LambdaProject.MathArt.Data

import com.LambdaProject.MathArt.model.CategoryItem
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.model.AchievementItem
import com.LambdaProject.MathArt.model.MaterialItem

val sampleMaterials = listOf(
    MaterialItem("transformasi_geometri", "Transformasi Geometri", R.drawable.img_geo, "11 SMA"),
    MaterialItem("lingkaran","Bangun Datar Lingkaran", R.drawable.img_circ, "11 SMA")
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