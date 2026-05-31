# Senimatika: Android-based Mathematics Learning Platform

**Senimatika** adalah aplikasi mobile berbasis Android yang memadukan elemen matematika, etnomatematika, dan gamifikasi. Aplikasi ini dirancang untuk memberikan pengalaman belajar matematika yang interaktif melalui kuis, eksplorasi peta, dan tantangan matematika yang semuanya diselesaikan dengan logika matematika.

---

## Fitur Utama

- **Annotation**: Gunakan fitur "coretan" untuk menandai pola/unsur dalam karya atau kebudayaan yang dikaitkan dengan konsep matematika.
- **Asynchronous Online Quiz**: Selesaikan kuis yang tersedia dengan tepat dan cepat. Adu skill matematika dengan pengguna lainnya untuk memperoleh skor tertinggi pada leaderboard.
- **Exploration Map**: Belajar matematika dengan menjelajahi peta fantasi. Ambil harta karun sebanyak-banyaknya dan pergi ke titik finsih untuk mengekstrak hasil buruan.
- **Boss Battle Quiz**: Pertempuran real-time melawan bos pada peta pantasi di mana setiap serangan ditentukan oleh kecepatan dan ketepatan menjawab kuis.
- **Station Quiz**: Tantangan spesifik di berbagai titik peta untuk membuka akses ke area tertentu.
- **MathText Rendering**: Dukungan penuh untuk notasi matematika kompleks (LaTeX) dan format teks Markdown.
- **Hybrid Zoom System**: Fitur zoom gambar native untuk memperjelas grafik soal tanpa mengganggu performa rendering teks.
- **Inventory & Power-Ups**: Gunakan item strategis seperti *Freeze Timer* atau *Healing Vial* untuk membantu menyelesaikan kuis sulit dalam melawan boss.

---

## Tech Stack & Optimasi

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI).
- **Admin & Management**: [SenimatikaAdmin](https://github.com/hikaru1999/senimatika-admin) — Dashboard khusus untuk manajemen konten kuis, monitoring leaderboard, dan pengelolaan data eksplorasi secara terpusat.
- **Backend**: [Firebase](https://firebase.google.com/) (Realtime Database, Authentication, & Cloud Storage).
- **Architecture**: MVVM (Model-View-ViewModel) dengan Clean Architecture.
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) (Dagger).
- **Asynchronous**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & Flow.
- **Math Rendering**: [KaTeX](https://katex.org/) terintegrasi via WebView yang dioptimalkan dengan *Lambda Offset* untuk mencegah *recomposition* berlebih.
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) untuk manajemen memori gambar yang efisien.

---

## Cara Menjalankan Proyek

Karena alasan keamanan dan efisiensi repositori, beberapa file telah dikecualikan (`.gitignore`):

1. **Firebase Configuration**:
    - Anda perlu menyediakan file `google-services.json` milik Anda sendiri.
2. **Multimedia Assets**:
    - File audio dan video berukuran besar tidak disertakan dalam repositori ini.
3. **Setup**:
    - Clone repositori ini.
    - Lakukan `Build Project` di Android Studio.
    - Pastikan koneksi internet aktif untuk sinkronisasi Firebase.

---

## Lisensi

Proyek ini dibuat untuk tujuan edukasi dan portofolio pribadi. Seluruh aset gambar dan suara adalah milik masing-masing kreator aslinya.

---

© 2026 Senimatika Team