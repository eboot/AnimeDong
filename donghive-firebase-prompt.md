# Donghive — Firebase Integration Prompt untuk AI Coding Agent

> Cara pakai: tempel ke AI agent setelah prompt utama (`donghive-prompt.md`),
> atau simpan sebagai `AGENTS.md` terpisah saat fase integrasi Firebase.
> Kerjakan BERURUTAN sesuai bagian 8 — satu layanan selesai & terverifikasi
> baru lanjut ke berikutnya.

## 0. Prasyarat (wajib sebelum coding)

1. Buat project di Firebase Console.
2. Tambahkan app Android:
   - Kotlin native: package `com.donghive.app`
   - Flutter: applicationId yang sama di `android/app/build.gradle`
3. Download `google-services.json` -> taruh di `android/app/`.
4. Tambah plugin google-services di Gradle (Kotlin) atau `flutterfire configure`
   (Flutter).
5. Jika file `google-services.json` BELUM ada: BERHENTI dan minta ke user.
   Jangan membuat data dummy / mock yang seolah-olah terhubung Firebase.

## 1. Authentication (login user)

- Aktifkan provider **Google** di Firebase Console > Authentication.
- Flutter: `firebase_auth` + `google_sign_in`.
  Kotlin: `firebase-auth` + Credential Manager (Google Sign-In).
- Hubungkan ke `ProfileScreen`: tombol "Masuk / Daftar" memicu flow login,
  tampilkan nama + foto user setelah berhasil, plus tombol Keluar.
- Setelah login, simpan `uid` — dipakai sebagai key di Firestore (bagian 2).
- **Guest mode tetap wajib jalan**: nonton tanpa login harus tetap bisa.
  Login hanya untuk sinkronisasi.

## 2. Firestore (sinkron bookmark, riwayat, preferensi)

Struktur data:

```
users/{uid}/
  bookmarks/{animeId}   { title, poster, createdAt }
  history/{episodeId}   { animeId, title, positionMs, updatedAt }
  prefs                 { defaultQuality, autoplayNext }
  tokens/{fcmToken}     { createdAt }        // untuk FCM, bagian 4
```

- Migrasi: saat user login PERTAMA kali, upload isi Room/sqflite lokal
  (`bookmarks`, `watch_history`) ke Firestore, lalu jadikan Firestore
  sumber utama. Cache lokal tetap dipertahankan untuk mode offline.
- Security rules (pasang di console):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

- User yang belum login: tetap pakai database lokal seperti sekarang.

## 3. Remote Config (pengganti base URL hardcode)

Keys yang wajib ada:

| Key | Default | Fungsi |
|---|---|---|
| `api_base_url` | `https://www.sankavollerei.web.id` | Ganti server tanpa update app |
| `default_quality` | `480p` | Kualitas awal player |
| `server_picker_enabled` | `true` | Matikan fitur ganti server darurat |
| `min_app_version` | `1` | Force update jika versi < ini |

- Fetch saat app start. Interval: 1 jam saat debug, 12 jam saat release.
- `api_base_url` MENGGANTIKAN `AppConstants.BASE_URL` — rebuild Dio/Retrofit
  client setiap nilainya berubah. Jika fetch gagal, pakai konstanta lokal.
- Verifikasi: ubah nilai di console -> restart app -> pastikan request
  mengarah ke URL baru (cek log OkHttp/Dio).

## 4. Cloud Messaging / FCM (notifikasi episode baru)

- Aktifkan FCM di console. Minta izin `POST_NOTIFICATIONS` hanya saat
  user membuka tab Jadwal / Library pertama kali (jangan saat splash).
- Simpan FCM token ke `users/{uid}/tokens` (atau koleksi `fcm_tokens`
  untuk guest).
- Alur notifikasi (butuh komponen server — Cloud Function atau cron
  eksternal, BUKAN di dalam app):
  1. Cron cek `GET /anime/home` tiap 1 jam, simpan episode terakhir per anime.
  2. Jika ada episode baru -> kirim push FCM ke user yang bookmark anime itu.
  3. Tap notifikasi -> deep link ke `/episode/{episodeId}`.
- Di dalam app: buat `FirebaseMessagingService` (Kotlin) / handler
  (Flutter) untuk menampilkan notifikasi + menangani tap.

## 5. Crashlytics (laporan crash)

- Aktifkan di console, tambah dependency + plugin Crashlytics.
- Test WAJIB: picu crash buatan di build debug, pastikan muncul di
  dashboard Firebase dalam 5 menit. Tanpa ini, integrasi belum selesai.
- Tambahkan custom key saat crash di player:
  `animeId`, `episodeId`, `serverDipakai` — supaya debug-nya gampang.
- Matikan pengiriman crash di build debug (hanya release) agar dashboard
  tidak kotor.

## 6. Analytics (perilaku user)

- Aktifkan otomatis (nyala begitu `firebase_core` dipasang).
- Event manual yang wajib di-log:
  - `episode_play` { animeId, episodeId, server }
  - `server_switch` { fromServer, toServer, quality }
  - `bookmark_add` / `bookmark_remove` { animeId }
  - `episode_completed` { animeId, episodeId }
- DILARANG mengirim PII (email, nama) sebagai parameter event.

## 7. App Check (proteksi dari scraping liar)

- Aktifkan provider **Play Integrity** di Firebase Console > App Check.
- Tambah SDK App Check, inisialisasi SEBELUM layanan Firebase lain.
- **Catatan jujur**: App Check melindungi layanan Firebase (Firestore, FCM).
  Untuk melindungi API sankavollerei dari scraper, backend juga harus
  verifikasi token App Check di setiap request — itu kerjaan backend,
  bukan app. Sampaikan batasan ini ke user, jangan klaim API sudah aman
  hanya karena App Check terpasang di app.

## 8. Urutan pengerjaan (wajib berurutan)

1. Remote Config + Crashlytics (paling gampang, fondasi)
2. Authentication
3. Firestore (termasuk migrasi data lokal)
4. FCM (app-side dulu, lalu Cloud Function/cron)
5. Analytics events
6. App Check

## 9. Aturan verifikasi (anti-slop)

- Setiap layanan selesai HANYA jika terlihat di Firebase Console
  (data masuk, crash muncul, token terdaftar). Screenshot/log console
  sebagai bukti — bukan sekadar "kode sudah ditulis".
- Dilarang mock: tidak ada `FakeFirebaseAuth`, tidak ada data dummy
  yang berpura-pura dari Firestore.
- Setiap selesai satu layanan, laporkan: file diubah + cara verifikasi
  manual di HP + apa yang terlihat di console.
