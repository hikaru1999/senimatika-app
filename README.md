# 🎨 Senimatika - Catatan Pembaruan & Dokumentasi Aplikasi

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Version](https://img.shields.io/badge/Version-4.3.3-success)](#)

Selamat datang di **Senimatika** (Seni & Matematika), aplikasi pembelajaran matematika interaktif berbasis petualangan (*gamified learning*) yang memadukan konsep logika berhitung, eksplorasi dunia (*Mathventure*), serta pertarungan kuis kompetitif (*Math Arena*).

Dokumen ini memuat rangkuman pembaruan fitur, arsitektur progresi level, sistem reward, dan penyegaran tampilan visual (*UI Refreshment*) terbaru.

---

## 🌟 Sorotan Pembaruan Terbaru (What's New)

### 1. 🗺️ Peta Wilayah Interaktif untuk Mode Eksplorasi (Mathventure)
*Implementasi File:* [`DynamicMapModal.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/ui/Pages/Exploration/DynamicMapModal.kt)
- **Visualisasi Peta Penuh:** Pemain kini dapat membuka modal peta interaktif untuk melihat seluruh tata letak dunia penjelajahan.
- **Pin Lokasi & Animasi Real-Time:** Menampilkan posisi pemain saat ini secara presisi dengan animasi detak (*pulsing radar*).
- **Statistik Penjelajahan Komprehensif:** Menyajikan persentase area yang telah dijelajahi (*Exploration Progress*), total peti harta karun terbuka (*Chests*), bos yang berhasil ditaklukkan (*Bosses Defeated*), serta stasiun matematika yang terselesaikan (*Solved Stations*).
- **Kompatibilitas Kabut & Malam:** Terintegrasi dengan mekanisme *Fog of War* dan *Night Mode*, sehingga bagian wilayah yang belum diinjak tetap diselimuti misteri hingga dijelajahi pemain.

---

### 2. ⚔️ Mode Duel Math Arena (Multiplayer 1v1 Real-Time)
*Implementasi File:* [`DuelMatchmakingOverlay.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/ui/Pages/Multiplayer/DuelMatchmakingOverlay.kt)
- **Pertarungan Adu Ketangkasan Cepat:** Tantang lawan dalam duel kuis matematika waktu nyata satu lawan satu.
- **Matchmaking Radar Interaktif:** Animasi radar pemindaian lawan dengan efek audio dinamis saat mencari musuh.
- **Tampilan Head-to-Head (VS Screen):** Menampilkan profil kedua pemain secara berhadapan lengkap dengan Avatar, Bingkai Avatar (*Avatar Frame*), Gelar (*Title Badge*), dan Level pemain sebelum pertandingan dimulai.
- **Hitung Mundur Pertandingan:** Menghadirkan ketegangan jelang duel kuis dimulai dengan transisi halus.

---

### 3. 🎯 Sistem Misi Terstruktur di Mathventure
*Implementasi File:* [`Mission.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/data/model/Mission.kt)
- Menghadirkan ragam tujuan spesifik di setiap rute petualangan untuk memicu strategi bermain yang variatif.
- **Tipe Misi yang Tersedia:**
  - `REACH_FINISH` : Capai bendera akhir petualangan.
  - `DEFEAT_BOSS` : Kalahkan monster bos matematika penjaga gerbang.
  - `COLLECT_CHEST` : Temukan dan buka peti harta karun tersembunyi.
  - `NO_POWER_UP` : Tantangan ekstra menyelesaikan rute tanpa menggunakan bantuan *power-up*.
  - `NO_BOSS_COMBAT` : Selesaikan penjelajahan dengan strategi menghindar tanpa memicu pertarungan bos.
- **Hadiah Misi:** Pemain yang berhasil menuntaskan misi mendapatkan imbalan langsung berupa **Bonus Koin** dan **Bonus EXP**.

---

### 4. 📈 Laporan Belajarku / Analitik Performa Siswa *(Eksperimental)*
*Implementasi File:* [`LearningReportPage.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/ui/Pages/Profile/LearningReportPage.kt)
- **Pelacakan Waktu Belajar Aktif:** Kartu hero interaktif yang mencatat akumulasi durasi belajar pengguna dalam hitungan jam dan menit.
- **Ringkasan Statistik Pembelajaran:**
  - Jumlah kuis online & skor tertinggi yang diraih.
  - Akumulasi poin kuis arena dan kuis mandiri yang dikerjakan.
  - Jumlah pencapaian (*Achievements*) yang sudah dibuka dan saldo koin terkumpul.
- **Riwayat Belajar Terperinci:** Memantau progres penyelesaian materi pelajaran matematika untuk evaluasi belajar mandiri maupun guru.

---

### 5. 🎭 Kustomisasi Profil: Avatar Picker & Bingkai Avatar Kosmetik
*Implementasi File:* [`AvatarPickerModal.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/ui/Pages/Profile/AvatarPickerModal.kt), [`AvatarFrameHelper.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/utils/AvatarFrameHelper.kt)
- **Modal Pemilih Dua Tab:** Kemudahan berpindah antara tab **Pilih Avatar** dan tab **Pilih Bingkai**.
- **Koleksi Avatar Beragam:** Pemain bebas memilih karakter ilustrasi yang mewakili identitas diri mereka.
- **Bingkai Avatar Kosmetik Eksklusif:** Dilengkapi kalibrasi presisi posisi mahkota/ornamen kosmetik pada avatar:
  - 🥉 **Bronze Rune** *(Unlocks Level 3)*: Bingkai perunggu bertatahkan batu safir.
  - 🌌 **Celestial Ring** *(Unlocks Level 6)*: Lingkaran kosmis astral dengan pendaran cahaya cyan.
  - 👑 **Sun Crown** *(Unlocks Level 10)*: Mahkota surya emas bercahaya dengan sayap suci.
  - 🌸 **Mystic Blossom** *(Unlocks Level 15)*: Ornamen kristal mistik legendaris bertabur kelopak bunga.
- **Pratinjau Langsung (Live Preview):** Tampilan gabungan avatar dan bingkai terpasang langsung dapat dilihat sebelum disimpan ke profil.

---

## 🏆 Tangga Progresi 15 Level & Sistem Reward

*Implementasi File:* [`LevelProgression.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/data/model/LevelProgression.kt)

Pemain mengumpulkan saldo EXP kumulatif untuk meniti 15 jenjang kemahiran, membuka fitur game baru, emblem, tag gelar prestisius, dan hadiah legendaris:

| Level | Gelar (Title) | Syarat EXP | Jenis Hadiah | Rincian Hadiah yang Diperoleh |
| :---: | :--- | :---: | :---: | :--- |
| **1** | **Novice** | 0 EXP | Title | Tag Awal *"Novice"* |
| **2** | **Apprentice** | 300 EXP | Exploration | Emblem *"Book of Beginnings"* & **Mode Eksplorasi Terbuka** |
| **3** | **Learner** | 700 EXP | Frame | Emblem *"Curious Eye"* & Bingkai Avatar *"Bronze Rune"* |
| **4** | **Explorer** | 1.200 EXP | Power-Up | Power-up *Chrono Freeze* (2x) & Tag *"Explorer"* |
| **5** | **Thinker** | 1.800 EXP | Emblem | Emblem *"Crystal of Insight"* & Tag *"Thinker"* |
| **6** | **Solver** | 2.500 EXP | Frame | Emblem *"Breaker of Barriers"* & Bingkai Avatar *"Celestial Ring"* |
| **7** | **Strategist** | 3.400 EXP | Power-Up | Power-up *Torch* (1x) & Tag *"Strategist"* |
| **8** | **Trailblazer** | 4.350 EXP | Power-Up | Power-up *Healing Vial* (3x) & Tag *"Trailblazer"* |
| **9** | **Mathematician** | 5.500 EXP | Emblem | Emblem *"Order of Numbers"* & Tag *"Mathematician"* |
| **10** | **Expert** | 6.700 EXP | Frame | *Magic Key* (2x) & Bingkai Avatar *"Sun Crown"* |
| **11** | **Virtuoso** | 8.400 EXP | Emblem | Emblem *"Seal of Mastery"* & Tag Mewah *"Virtuoso"* |
| **12** | **Grandmaster** | 10.200 EXP | Power-Up | Power-up *Battle Shield* (3x) & Tag Mewah *"Grandmaster"* |
| **13** | **Prodigy** | 12.300 EXP | Power-Up | Item *Lantern* (1x) & Tag Mewah *"Prodigy"* |
| **14** | **Sage** | 14.750 EXP | Power-Up | Tag Mewah *"Sage"* & Paket Power-up Tempur Lengkap |
| **15** | **Scholar** | 17.500 EXP | **Legendary** | **Emblem Prestisius *"The Myth & The Legend"*** & **Bingkai Avatar *"Mystic Blossom"*** |

> [!TIP]
> **Klaim Hadiah Level:** Pemain dapat mengakses jendela modal katalog level (*Level Road*) melalui header dashboard atau profil untuk mengklaim hadiah di tiap level yang telah tercapai!

---

## ⚡ Sumber Perolehan EXP (EXP Sources)

Sistem progresi dirancang adil dan seimbang agar mendorong pemahaman materi secara konsisten:

1. **Math Arena:**
   - **Mode Mandiri:** Menuntaskan kuis per topik matematika dengan bonus evaluasi kecepatan dan ketelitian.
   - **Mode Duel:** Memenangkan duel kuis matematika 1v1 melawan kompetitor.
   - *Formula Cerdas:* Dilengkapi bonus akurasi (50%), kecepatan menjawab (25%), dan streak jawaban benar (25%).
2. **Mathventure (Mode Eksplorasi):**
   - Menjelajahi ubin wilayah baru di peta.
   - Menyelesaikan tantangan kuis di stasiun pos matematika.
   - Menemukan dan membuka peti harta karun (*chests*).
   - Mengalahkan monster dan boss matematika.
   - Menuntaskan target misi objektif (*Missions*).
3. **Pencapaian (Achievements):**
   - Meraih milestone belajar tertentu, rekor streak kuis, dan penyelesaian seluruh materi bab.
4. **Mekanisme Anti-Grinding (*Repeat-Play Multiplier*):**
   - Pengulangan sesi materi/kuis yang sama menggunakan sistem reduksi perolehan EXP secara bertahap (1.0x → 0.8x → 0.6x → 0.4x → 0.3x) guna memotivasi siswa mempelajari topik-topik baru.

---

## 🎁 Variasi Hadiah & Kosmetik Profil

- 🎖️ **Emblem:** Lencana kehormatan visual yang disematkan pada profil. Mengaktifkan emblem juga memicu efek gaya tipografi **WordArt** berwarna gradien khusus pada nama pengguna.
- 🏷️ **Tag Gelar (Title Badges):** Label status tingkat kemahiran (mulai dari *Novice* hingga *Scholar*) dengan palet warna dan ornamen gradien khusus.
- 🖼️ **Avatar Frame:** Bingkai kosmetik berornamen yang membungkus foto profil pengguna di seluruh tampilan aplikasi.
- 🧪 **Power-Up Tempur & Penjelajahan:** *Chrono Freeze*, *Battle Shield*, *Torch*, *Lantern*, *Healing Vial*, dan *Magic Key* yang disimpan di dalam tas (*Inventory Bag*) untuk membantu menyelesaikan rute-rute menantang.

---

## 🌅 Penyegaran Tampilan Antarmuka (UI Refreshment)

### 🌄 Dashboard Header Dinamis
*Implementasi File:* [`DashboardHeader.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/ui/Pages/Dashboard/DashboardHeader.kt)
- **Tema Langit Real-Time 4 Waktu (Sky Themes):**
  - 🌅 **Pagi (04.00 - 10.59):** Palet *Sunrise Orange* & *Peach Morning Sky*.
  - ☀️ **Siang (11.00 - 14.59):** Palet *Royal Sky Blue* cerah bertenaga.
  - 🌇 **Sore (15.00 - 17.59):** Palet *Sunset Amber*, *Soft Rose*, & *Twilight Lavender*.
  - 🌙 **Malam (18.00 - 03.59):** Palet *Moonlit Indigo Mist* & *Pale Lavender*.
- **Siluet Langit Artistik:** Ornamen ilustrasi siluet matahari pagi/siang/sore dan bulan malam yang muncul dinamis mengikuti jam perangkat.
- **Nama Pengguna WordArt:** Efek teks nama pengguna artistik yang menyesuaikan dengan emblem aktif yang dipilih.
- **Indikator Progres & Akses Cepat:** Bar progres EXP, sapaan dinamis, tanggal terformat bahasa Indonesia, serta tautan instan ke dialog *Level Road*.

### 👤 Profile Header yang Ditingkatkan
*Implementasi File:* [`ProfileHeader.kt`](file:///c:/Users/Helvetica/Documents/Android%20Project/LearnApplication/app/src/main/java/com/LambdaProject/MathArt/ui/Pages/Profile/ProfileHeader.kt)
- Tampilan kartu profil modern memadukan foto avatar berbingkai (*Avatar Frame*), badge gelar (*Title Badge*), dan emblem aktif.
- Visualisasi bar progres EXP menuju level berikutnya dengan perhitungan sisa EXP yang transparan.
- Akses instan satu ketukan untuk mengganti Avatar & Bingkai, membuka modal *Level Road*, atau meninjau laporan belajar.

---

## 🛠️ Tumpukan Teknologi (Tech Stack)

- **Bahasa Pemrograman:** [Kotlin](https://kotlinlang.org/) (JVM 11, Target SDK 35)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) dengan Material Design 3
- **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
- **Backend & Database:** [Firebase Firestore](https://firebase.google.com/docs/firestore), [Firebase Authentication](https://firebase.google.com/docs/auth)
- **Manajemen State & Navigasi:** Jetpack Navigation Compose, ViewModel, Coroutines & Flow

---

## 📂 Struktur Berkas Utama Pembaruan

```text
app/src/main/java/com/LambdaProject/MathArt/
├── data/
│   └── model/
│       ├── LevelProgression.kt         # Definisi 15 level, sistem reward & kalkulator EXP
│       └── Mission.kt                  # Definisi objektif & misi penjelajahan Mathventure
├── ui/
│   ├── Pages/
│   │   ├── Dashboard/
│   │   │   └── DashboardHeader.kt      # Header dashboard dengan tema langit 4 waktu & WordArt
│   │   ├── Exploration/
│   │   │   ├── DynamicMapModal.kt      # Modal peta interaktif untuk eksplorasi Mathventure
│   │   │   └── BagModal.kt             # Modal inventaris power-up & perlengkapan pemain
│   │   ├── Multiplayer/
│   │   │   └── DuelMatchmakingOverlay.kt # Overlay matchmaking radar untuk mode duel 1v1
│   │   └── Profile/
│   │       ├── AvatarPickerModal.kt    # Dialog pemilihan avatar & bingkai kosmetik
│   │       ├── LearningReportPage.kt   # Halaman analitik performa belajar siswa (Eksperimental)
│   │       ├── LevelRoadModal.kt       # Modal tangga klaim reward 15 level & manajemen gelar
│   │       └── ProfileHeader.kt        # Header halaman profil dengan avatar berbingkai
│   └── components/
│       ├── LevelEmblem.kt              # Komponen visual lencana emblem & WordArt
│       └── LevelTagBadge.kt            # Komponen badge gelar level
└── utils/
    ├── AvatarFrameHelper.kt            # Kalibrasi posisi & aset bingkai avatar
    └── AvatarHelper.kt                 # Manajemen katalog avatar pengguna
```

---

*Dikembangkan dengan penuh dedikasi oleh Tim Pengembang Senimatika.*
