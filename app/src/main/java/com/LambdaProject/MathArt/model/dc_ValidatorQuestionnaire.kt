package com.LambdaProject.MathArt.model

import kotlinx.serialization.Serializable

@Serializable
data class ValidatorQuestion(
    val id: String,
    val text: String,
    val aspectId: Int,
    val indicator: String
) {
    companion object {
        val allQuestions = listOf(
            ValidatorQuestion(
                "m1",
                "Materi yang disajikan dalam media pembelajaran sesuai dengan kurikulum yang digunakan.",
                1,
                "Ketepatan"
            ),
            ValidatorQuestion(
                "m2",
                "Materi yang disajikan sesuai dengan Standar Kompetensi (SK) dan Kompetensi Dasar (KD).",
                1,
                "Kepentingan"
            ),
            ValidatorQuestion("m3", "Materi yang disajikan lengkap.", 1, "Kelengkapan"),
            ValidatorQuestion("m4", "Penjelasan materi yang disajikan jelas.", 1, "Keseimbangan"),
            ValidatorQuestion("m5", "Materi yang disajikan menarik.", 1, "Minat/perhatian"),
            ValidatorQuestion(
                "m6",
                "Materi dijelaskan dengan urutan yang benar.",
                1,
                "Kesesuaian dengan situasi siswa"
            ),
            ValidatorQuestion("m7", "Terdapat soal-soal latihan.", 2, "Kesempatan belajar"),
            ValidatorQuestion(
                "m8",
                "Materi yang disajikan sesuai dengan level siswa kelas XI.",
                2,
                "Bantuan untuk belajar"
            ),
            ValidatorQuestion(
                "m9",
                "Penjelasan yang disajikan dapat memudahkan siswa dalam memahami materi.",
                2,
                "Bantuan untuk belajar"
            ),
            ValidatorQuestion(
                "m10",
                "Konten yang terdapat pada media pembelajaran dapat memotivasi semangat belajar siswa.",
                2,
                "Kualitas memotivasi"
            ),
            ValidatorQuestion(
                "m11",
                "Terdapat sumber belajar lain berupa tautan ke laman.",
                2,
                "Hubungan dengan program pembelajaran lain"
            ),
            ValidatorQuestion(
                "m12",
                "Media sesuai untuk digunakan dalam kegiatan belajar secara berkelompok.",
                2,
                "Kualitas sosial interaksi instruksional"
            ),
            ValidatorQuestion(
                "m13",
                "Terdapat evaluasi untuk mengukur penguasaan materi.",
                2,
                "Kualitas tes dan penilaiannya"
            ),
            ValidatorQuestion(
                "m14",
                "Media yang digunakan mampu memberikan pengalaman belajar bagi siswa.",
                2,
                "Dampak bagi siswa"
            ),
            ValidatorQuestion(
                "m15",
                "Media pembelelajaran dapat digunakan dalam kegiatan belajar-mengajar di kelas.",
                2,
                "Dampak bagi guru dan pembelajaran"
            ),
            ValidatorQuestion(
                "me1",
                "Perintah yang terdapat di dalam media dapat diulang kapan pun.",
                1,
                "Fleksibilitas instruksional"
            ),
            ValidatorQuestion(
                "me2",
                "Evaluasi yang terdapat di dalam media dapat meningkatkan motivasi belajar siswa.",
                1,
                "Kualitas Memotivasi"
            ),
            ValidatorQuestion(
                "me3",
                "Jenis huruf yang digunakan terlihat jelas dan mudah dibaca.",
                2,
                "Keterbacaan"
            ),
            ValidatorQuestion(
                "me4",
                "Bahasa yang digunakan sesuai dengan PUEBI.",
                2,
                "Keterbacaan"
            ),
            ValidatorQuestion(
                "me5",
                "Terdapat petunjuk penggunaan media.",
                2,
                "Kemudahan Penggunaan"
            ),
            ValidatorQuestion("me6", "Media mudah untuk dioperasikan.", 2, "Kemudahan Penggunaan"),
            ValidatorQuestion(
                "me7",
                "Gambar yang disajikan terlihat jelas dan tidak terpecah-pecah.",
                2,
                "Kualitas tampilan"
            ),
            ValidatorQuestion(
                "me8",
                "Animasi yang digunakan dalam media sesuai dengan tingkatan siswa SMA.",
                2,
                "Kualitas tampilan"
            ),
            ValidatorQuestion(
                "me9",
                "Tampilan di setiap halaman konsisten.",
                2,
                "Kualitas tampilan"
            ),
            ValidatorQuestion(
                "me10",
                "Warna teks dalam media yang disajikan selaras dengan latar belakang yang digunakan.",
                2,
                "Kualitas tampilan"
            ),
            ValidatorQuestion(
                "me11",
                "Semua tombol yang terdapat di dalam media berfungsi dengan semestinya.",
                2,
                "Kualitas penanganan jawaban"
            ),
            ValidatorQuestion(
                "me12",
                "Media dapat dibuka dengan cepat.",
                2,
                "Kualitas pengelolaan program"
            ),
            ValidatorQuestion(
                "me13",
                "Media yang dikembangkan dapat dengan mudah diakses oleh pengguna.",
                2,
                "Kualitas pendokumentasian"
            )
        )
    }
}

@Serializable
data class ValidatorResponse(
    val questionId: String,
    val selectedValue: Int
)

@Serializable
data class ValidatorSession(
    val validatorId: String,
    val role: String,
    val responses: List<ValidatorResponse>,
    val lastUpdated: Long = System.currentTimeMillis(),
    val name: String? = null,
    val comment: String? = null,
    val institution: String? = null,
    val decision: Decision? = null,
)

@Serializable
data class KuesionerResult(
    val userId: String = "",
    val name: String = "",
    val institution: String = "",
    val role: String = "",
    val responses: Map<String, Int> = emptyMap(),
    val decision: Decision = Decision.LAYAK,
    val comment: String? = null,
    val deviceInfo: String? = null,
    val submittedAt: Long = 0L
)

@Serializable
enum class ValidatorRole {
    MEDIA,
    MATERI
}

@Serializable
enum class Decision {
    LAYAK,
    LAYAK_DENGAN_REVISI,
    TIDAK_LAYAK
}

val Decision.displayText: String
    get() = when (this) {
        Decision.LAYAK -> "Layak Digunakan"
        Decision.LAYAK_DENGAN_REVISI -> "Layak Digunakan dengan Revisi"
        Decision.TIDAK_LAYAK -> "Tidak Layak Digunakan"
    }