# 🎨 Senimatika: Adventure-Based Math Learning

**Senimatika** adalah aplikasi mobile berbasis Android yang menggabungkan elemen petualangan (RPG) dengan pembelajaran matematika interaktif. Aplikasi ini dirancang untuk memberikan pengalaman belajar yang imersif melalui eksplorasi peta, tantangan area, dan pertempuran bos yang semuanya diselesaikan dengan logika matematika.

---

## 🚀 Fitur Utama

- **Exploration Map**: Menjelajahi dunia fantasi dengan sistem navigasi joystick yang halus dan fitur *Fog of War*.
- **Boss Battle Quiz**: Pertempuran real-time melawan bos di mana setiap serangan ditentukan oleh kecepatan dan ketepatan menjawab soal.
- **Station Quiz**: Tantangan statis di berbagai titik peta untuk membuka akses ke area baru.
- **MathText Rendering**: Dukungan penuh untuk notasi matematika kompleks (KaTeX) dan format teks Markdown.
- **Hybrid Zoom System**: Fitur zoom gambar native untuk memperjelas grafik soal tanpa mengganggu performa rendering teks.
- **Inventory & Power-Ups**: Gunakan item strategis seperti *Freeze Timer* atau *Healing Vial* untuk membantu menyelesaikan kuis sulit.

---

## 🛠️ Tech Stack & Optimasi

Proyek ini dibangun dengan fokus pada performa tinggi, bahkan untuk perangkat **entry-level** (RAM 3-4GB):

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI).
- **Backend**: [Firebase](https://firebase.google.com/) (Realtime Database, Authentication, & Cloud Storage).
- **Architecture**: MVVM (Model-View-ViewModel) dengan Clean Architecture.
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) (Dagger).
- **Asynchronous**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & Flow.
- **Math Rendering**: [KaTeX](https://katex.org/) terintegrasi via WebView yang dioptimalkan dengan *Lambda Offset* untuk mencegah *recomposition* berlebih.
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) untuk manajemen memori gambar yang efisien.

---

## 📉 Optimasi Performa (Portfolio Highlights)

Aplikasi ini mengimplementasikan beberapa teknik optimasi tingkat lanjut:
1. **Lambda-based Approach**: Menggerakkan karakter di peta menggunakan fase *Placement* (bukan *Recomposition*) untuk mencapai 60 FPS yang stabil.
2. **Hybrid Zoom Component**: Mengisolasi interaksi zoom gambar ke komponen Native Compose guna mengurangi beban CPU pada WebView.
3. **State Flow Optimization**: Menggunakan `StateFlow` untuk sinkronisasi timer kuis guna menghindari *UI Thread blocking*.

---

## 📦 Cara Menjalankan Proyek

Karena alasan keamanan dan efisiensi repositori, beberapa file telah dikecualikan (`.gitignore`):

1. **Firebase Configuration**:
    - Anda perlu menyediakan file `google-services.json` milik Anda sendiri.
    - Letakkan file tersebut di folder `app/`.
2. **Multimedia Assets**:
    - File audio dan video berukuran besar tidak disertakan dalam repositori ini.
3. **Setup**:
    - Clone repositori ini.
    - Lakukan `Build Project` di Android Studio (versi Flamingo atau lebih baru).
    - Pastikan koneksi internet aktif untuk sinkronisasi Firebase.

---

## 📄 Lisensi

Proyek ini dibuat untuk tujuan edukasi dan portofolio pribadi. Seluruh aset gambar dan suara adalah milik masing-masing kreator aslinya.

---

© 2026 Senimatika Team