# Google Sign-In Integration via Android Credential Manager

Implementasi sistem autentikasi akun Google resmi menggunakan **Android Credential Manager** (tanpa ketergantungan Firebase), yang menghubungkan profil pengguna dengan data lokal (Favorit dan Riwayat tontonan) di **AnimeDong**.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Keputusan arsitektur berikut telah dikonfirmasi berdasarkan preferensi Anda:

- **Metode Autentikasi**: Menggunakan **Android Credential Manager & Google Identity Services** bawaan AndroidX (`androidx.credentials`), sehingga **tidak memerlukan Firebase Console** ataupun file `google-services.json`.
- **Cakupan Data Akun**: Akun Google akan menampung data profil (Nama, Email, Foto Profil) serta menautkan kepemilikan daftar **Favorit** dan **Riwayat Tontonan** pengguna di database Room lokal dengan opsi *Cloud Backup/Export*.
- **Fallback & Guest Mode**: Pengguna tetap dapat menikmati streaming tanpa login (sebagai *Tamu / Guest*), dan dapat menautkan akun Google kapan saja dengan 1 klik melalui layar Profil atau saat menekan tombol bookmark/favorit.

---

## 1. Overview & Core Concept

- **Apa yang Dibangun**: Modul login Google native berbasis *One-Tap Sign-In / Credential Manager* yang menampilkan bottom-sheet pemilihan akun Google yang tersimpan di perangkat Android.
- **Target Pengguna**: Pecinta anime & donghua yang ingin menyimpan identitas profil dan memelihara daftar tontonan serta riwayat episode tanpa risiko kehilangan data saat berganti profil.
- **Nilai Utama**: Proses login cepat dalam 1 detik tanpa mengisi formulir kata sandi manual, zero external cloud dependency (privasi terjaga), dan pengalaman antarmuka yang mulus sesuai standar Material 3.

---

## 2. User Experience & Visual Design

### Alur Interaksi Pengguna (User Flows)

1. **Titik Masuk Login**:
   - Di **Layar Profil**: Kartu akun menampilkan tombol *"Masuk dengan Akun Google"* dengan logo resmi Google berwarna putih/abu modern.
   - Di **Layar Detail**: Jika pengguna belum login dan menekan tombol bookmark, muncul banner halus *"Login dengan Google untuk mencadangkan tontonanmu"*.
2. **Proses Autentikasi**:
   - Saat tombol ditekan, Android Credential Manager memunculkan dialog native *Google One Tap* di bagian bawah layar.
   - Pengguna memilih akun Google yang ada di ponsel.
   - Status berubah menjadi *Loading* dengan indikator progress emas khas AnimeDong.
3. **Kondisi Berhasil Login**:
   - Kartu Profil langsung menampilkan foto profil avatar Google pengguna, nama lengkap, email, serta lencana keanggotaan *"Akun Terhubung"*.
   - Menyediakan tombol *"Keluar (Sign Out)"* dan tombol *"Cadangkan & Sinkronkan Data"*.
4. **Kondisi Logout**:
   - Menghapus sesi aktif secara aman dan mengembalikan profil ke mode *Tamu (Guest)* tanpa menghapus riwayat tontonan lokal.

### Desain Visual & Tema

- **Tombol Google Sign-In**: Kartu interaktif berlatar belakang gelap (`#1A1E2E`) dengan aksen border tipis (`#262C42`), menampilkan ikon G-Logo resmi, teks *"Lanjutkan dengan Google"*, dan efek *ripple*.
- **Kartu Profil Pengguna**:
  - Avatar lingkaran berukuran 64dp yang memuat foto dari Google (`AsyncImage` Coil) dengan bingkai gradasi emas `DongHiveGold` ke `DongHiveOrange`.
  - Chip status *"Google Verified"* berwarna hijau emerald lembut.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Android Credential Manager vs. Firebase Auth**
  - *Pendekatan*: Menggunakan `androidx.credentials:credentials`.
  - *Alasan*: Menjawab kebutuhan Anda untuk tidak memerlukan setup project Firebase, tidak memerlukan upload `google-services.json`, dan ringan tanpa SDK analytics yang berat.
  - *Alternatif*: Firebase Auth dihindari karena memerlukan konfigurasi konsol eksternal dan API Key cloud.

- **Decision 2: Penyimpanan Sesi Pengguna**
  - *Pendekatan*: Menyimpan sesi pengguna (DisplayName, Email, PhotoUrl, IdToken) di `EncryptedSharedPreferences` / `DataStore` atau Room `UserSessionEntity`.
  - *Alasan*: Cepat diakses saat aplikasi dibuka tanpa perlu autentikasi ulang setiap kali meluncurkan aplikasi.

---

## 4. Technical Architecture & Data Strategy

### System Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                       AnimeDong UI                          │
│                                                             │
│   ┌─────────────────────┐       ┌───────────────────────┐   │
│   │    ProfileScreen    │       │     DetailScreen      │   │
│   │ [Google Login Btn]  │       │  [Bookmark / Fav Btn] │   │
│   └──────────┬──────────┘       └───────────┬───────────┘   │
└──────────────┼──────────────────────────────┼───────────────┘
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    DongHiveViewModel                        │
│   - userAuthState: StateFlow<AuthState>                     │
│   - signInWithGoogle()                                      │
│   - signOut()                                               │
│   - syncUserData()                                          │
└──────────────────────────────┬──────────────────────────────┘
                               │
               ┌───────────────┴───────────────┐
               ▼                               ▼
┌──────────────────────────────┐ ┌────────────────────────────┐
│   AuthRepository / Client    │ │     DongHiveDatabase       │
│   (Android Credential Mgr)   │ │     (Room Persistence)     │
│                              │ │                            │
│  - GetCredentialRequest      │ │  - UserSessionEntity       │
│  - GoogleIdTokenCredential   │ │  - FavoriteEntity          │
│  - One-Tap Account Picker    │ │  - HistoryEntity           │
└──────────────────────────────┘ └────────────────────────────┘
```

### Data Model & State

```kotlin
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(
        val userId: String,
        val displayName: String,
        val email: String,
        val photoUrl: String?
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

### Tahapan Eksekusi:

1. **Dependensi**: Menambahkan dependensi `androidx.credentials:credentials` dan `androidx.credentials:credentials-play-services-auth` pada `app/build.gradle.kts`.
2. **Auth Service**: Membuat `GoogleAuthManager.kt` untuk menangani `CredentialManager.create(context).getCredential(...)`.
3. **State & ViewModel**: Mengintegrasikan `userProfileState` di `DongHiveViewModel` agar reaktif di semua layar.
4. **Pembaruan Layar Profil**: Menghubungkan tombol login Google, kartu profil dinamis, dan fungsi logout.
5. **Verifikasi**: Uji kompilasi dengan `compile_applet`.
