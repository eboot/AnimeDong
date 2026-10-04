# AnimeDong - Nonton Donghua & Anime Sub Indo

Aplikasi streaming dan katalog Donghua (Animasi 3D Mandarin) dan Anime modern, cepat, dan lengkap untuk Android yang dibangun menggunakan **Kotlin**, **Jetpack Compose**, **Material 3**, dan **Room Database**.

---

## Fitur Utama

1. **Beranda (Home)**:
   - **Spotlight Hero Banner**: Carousel donghua unggulan (Battle Through the Heavens, Perfect World, Soul Land II, Renegade Immortal) dengan tombol *Nonton Sekarang*.
   - **Pencarian Real-Time**: Pencarian cepat berdasarkan judul Indonesia/Inggris, judul pinyin Mandarin, atau genre.
   - **Filter Kategori & Status**: Filter chip interaktif (Semua, Xianxia, Aksi, Kultivasi, Fantasi, Sci-Fi, Komedi, Romansa, Ongoing, Tamat).
   - **Episode Baru Hari Ini**: Deretan rilis episode harian terbaru dengan tag episode dan kualitas HD.
   - **Top Popular Chart**: Peringkat donghua terpopuler dengan rating bintang dan jumlah penayangan.
   - **Koleksi Lengkap**: Grid donghua responsif dengan badge status dan episode.

2. **Detail Donghua**:
   - Sampul backdrop heroik & cover poster HD.
   - Informasi lengkap: Judul asli Hanzi/Pinyin, studio animasi, status rilis, rating, tahun, dan genre.
   - Sinopsis cerita interaktif yang dapat diperluas.
   - **Daftar Episode Interaktif**: Pengelompokan episode (1-25, 26-50, dst.) dengan durasi, tanggal rilis, dan tombol tonton instan.
   - Tombol **Favorit / Bookmark** tersimpan langsung ke database lokal.
   - Rekomendasi donghua serupa.

3. **Pemutar Video (Video Player)**:
   - Pemutar video dengan kontrol interaktif: Putar/Jeda, Maju 10 detik, Mundur 10 detik, Slider linier timeline.
   - **Multi-Server Streaming**: Server VIP (1080p Ultra HD), Server Fast CDN (720p HD), dan Server Mirror Hemat Kuota.
   - **Pengaturan Pemutar**: Pengatur kecepatan (0.75x hingga 2.0x), pemilihan resolusi, dan sakelar otomatis putar episode selanjutnya.
   - **Pemilih Episode Cepat**: Mengganti episode langsung dari bawah pemutar tanpa kembali ke menu.
   - **Diskusi Penonton**: Kolom komentar interaktif untuk berbagi tanggapan tanpa spoiler.

4. **Jadwal Rilis Mingguan (Schedule)**:
   - Kalender rilis harian (Senin hingga Minggu) sesuai waktu WIB.
   - Jam tayang tepat, indikator status rilis, dan tombol pengingat (notifikasi).

5. **Koleksi & Pustaka (Library)**:
   - **Favorit**: Menyimpan judul-judul kesukaan Anda secara permanen.
   - **Riwayat Tonton**: Melacak progres tontonan dengan penanda waktu terakhir dan progress bar untuk melanjutkan kapan saja.
   - **Unduhan**: Menyimpan episode untuk ditonton secara offline saat bepergian atau hemat kuota.
   - Didukung penyimpanan lokal reaktif **Room Database**.

6. **Profil, Akun Google & Pengaturan (Profile)**:
   - **Google Sign-In Native (Credential Manager)**: Masuk mudah dan aman dengan akun Google tanpa perlu konfigurasi Firebase.
   - **Sinkronisasi Data Akun**: Sinkronkan daftar Favorit dan Riwayat tontonan ke profil pengguna.
   - **Mode Tamu & Profil VIP**: Transisi mulus antara akun Tamu dan Akun Google dengan lencana VIP aktif.
   - **Penyimpanan Sesi Lokal**: Terintegrasi ke Room Database (`UserSessionEntity`).
   - Pengaturan kualitas streaming bawaan dan unduhan via Wi-Fi.
   - Pembersih cache aplikasi (Cache cleaner).
   - Tautan komunitas Telegram Donghua Indonesia.

---

## Arsitektur & Teknologi

- **Bahasa**: Kotlin 2.2
- **UI Framework**: Jetpack Compose dengan Material Design 3
- **Autentikasi**: Android Credential Manager (`androidx.credentials` + `com.google.android.libraries.identity.googleid`)
- **Database Lokal**: Android Room Database (KSP) dengan reaktif `Flow` & `StateFlow`
- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Image Loader**: Coil Compose
- **Media**: Android Native Media Player Engine dengan custom Compose overlay
- **Adaptive Launcher Icon**: Ikon bertema ornamen sarang lebah emas & naga donghua (`#12131C`)
