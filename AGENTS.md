# AnimeDong — Project Prompt untuk AI Coding Agent (Kotlin Native)

> Cara pakai: file ini adalah `AGENTS.md` di root project Kotlin.
> Tempel seluruh isinya di awal chat dengan AI agent sebelum mulai kerja.
> KHUSUS Kotlin native — jangan bawa-bawa Flutter.
> File ini MENGGANTIKAN `donghive-prompt.md` + `donghive-firebase-prompt.md`
> untuk project Kotlin.

## 1. Konteks

Kamu membantu membangun **AnimeDong**, aplikasi Android native (Kotlin)
untuk streaming donghua subtitle Indonesia. Ini BUKAN Donghive — hasil
reverse-engineering Donghive 5.7 hanya dipakai untuk memahami FITUR,
bukan untuk menyalin desain, nama, atau asetnya.

Identitas baru (wajib dipakai di semua tempat):
- Nama app: **AnimeDong**
- Package: `com.animedong.app` (ganti total dari `com.donghive.app`,
  jangan ada sisa string "donghive" di kode/resource)
- Bahasa UI: Indonesia santai. Dark theme.

Ada DUA backend, JANGAN diubah strukturnya:
- **Anime**: `https://www.sankavollerei.web.id` (base URL default, bisa diganti
  via Remote Config `api_base_url`)
- **Donghua**: `http://168.110.213.108/donghua` — API milik sendiri di Oracle
  VPS (bisa diganti via Remote Config `donghua_api_base_url`)
- Keduanya **tanpa auth**. Tidak ada endpoint login, search, atau user.
  Jangan mengasumsikan endpoint yang tidak ada di daftar.

## 2. Backend contract (sumber kebenaran)

### 2a. Anime (sankavollerei)

Semua response dibungkus: `{ "ok": true, "statusCode": 200, "message": "", "data": ... }`

| Method | Endpoint | Dipakai untuk | Catatan |
|---|---|---|---|
| GET | `/anime/home` | Home | `data.ongoing.animeList[]`, `data.completed.animeList[]` |
| GET | `/anime/schedule` | Jadwal | Array `{ day, anime_list[] }`, day = Senin..Minggu |
| GET | `/anime/anime/{animeId}` | Detail | Termasuk `episodeList[{episodeId, title, eps, date}]` |
| GET | `/anime/episode/{episodeId}` | Player | Termasuk `defaultStreamingUrl`, `server.qualities[{title, serverList[{title, serverId}]}]`, prev/next |
| GET | `/anime/server/{serverId}` | Player | Return URL video final |

### 2b. Donghua (API VPS sendiri)

Response TIDAK dibungkus — payload langsung di root.

| Method | Endpoint | Dipakai untuk | Catatan |
|---|---|---|---|
| GET | `/?page=N` | Home | `{page, results[{section, cards[]}]}`. Section: `terpopuler_hari_ini`, `rilisan_terbaru`, `movie`, `rekomendasi`. Card = kartu EPISODE: `slug` = `"{serial}-episode-{n}-subtitle-indonesia"` (ada varian `-tamat-`); turunkan slug serial via `DonghuaSlugs.seriesSlugFromCard` |
| GET | `/schedule` | Jadwal | `{days[], results[{day, items[]}]}`. Item: `{eps, slug, thumbnail, title}` — slug SUDAH slug serial. Day bisa `"Jum'at"` -> normalisasi ke `"Jumat"` |
| GET | `/{seriesSlug}` | Detail | `{result{name, thumbnail, status, tipe, rating(string), studio, genre[string], sinopsis{paragraphs[]}, episode[{date, episode(string!), slug, subtitle}]}}` |
| GET | `/episode/{episodeSlug}` | Player | `{result{name, players[{name, url}]}}` — URL = halaman EMBED langsung (ok.ru, abyssplayer, dsb), BUKAN file video |

**ATURAN KERAS (berlaku dua backend):**
- Label section di UI harus jujur mengikuti nama section API.
  DILARANG menampilkan data anime dengan label "Donghua ..." atau sebaliknya.
- Player donghua = WebView (embed page), BUKAN ExoPlayer.

Field item anime: `title, poster, episodes, releaseDay, latestReleaseDate`,
id bisa bernama `animeId` ATAU `slug` — normalisasi ke satu field `id`.

**ATURAN KERAS:**
- `serverId` itu dinamis dan BISA EXPIRED (pernah 404). Selalu ambil fresh
  dari response episode. DILARANG hardcode atau cache lama.
- Kalau butuh field/endpoint yang tidak ada di tabel, TANYA DULU.
  Jangan ngarang response backend.

## 3. Arsitektur Kotlin (wajib diikuti)

- MVVM + Clean Architecture per-feature: `data/` (DTO, Retrofit service,
  Room) / `domain/` (model bersih, repository interface) /
  `presentation/` (Activity/Fragment/screen, ViewModel).
- DUA Retrofit service, satu per backend: `AnimeApiService` (sankavollerei)
  dan `DonghuaApiService` (API donghua). UI dilarang memegang DTO
  mentah — selalu lewat domain model yang dipetakan di repository.
  `ContentType` (ANIME/DONGHUA) menandai asal setiap model & route
  (`detail/{type}/{id}`, `player/{type}/{id}`).
- Coroutines + Flow untuk async. Satu DI framework saja (Hilt disarankan);
  pilih satu dan konsisten di seluruh project.
- Room, dua tabel (v2, migrasi MIGRATION_1_2):
  - `watch_history(episode_id PK, anime_id, title, position_ms, updated_at, content_type)`
  - `bookmarks(anime_id PK, title, poster, created_at, content_type)`
  - Operasi lokal terpusat di `AnimeRepository` dengan param `contentType`
  (`DonghuaRepository` network-only).
- Base URL API wajib dari Firebase Remote Config (`api_base_url` untuk anime,
  `donghua_api_base_url` untuk donghua) — rebuild Retrofit client setiap
  nilainya berubah. Jangan hardcode.
- API donghua masih HTTP (belum HTTPS): cleartext diizinkan KHUSUS untuk
  host `168.110.213.108` via `res/xml/network_security_config.xml`.
  Kalau base URL pindah host, tambah `<domain>` di file itu.

## 4. UI/UX — ikut desain referensi (screenshot user)

Desain mengikuti 5 screenshot referensi dari user (tersimpan di
`workspace/user/media_library/`): aksen kuning emas (`#FFC107`) di atas
background navy sangat gelap (`#0E1218`), kartu navy (`#1A2130`) rounded
16-20dp, judul section dengan bar kuning vertikal, bottom nav pill kuning
untuk tab aktif.

- BottomNavigation: Beranda | Jadwal | Koleksi | Profil (pill kuning saat aktif).
- Beranda: hero carousel SPOTLIGHT (poster + tombol "Nonton"/"Detail") +
  "Episode Baru Hari Ini" (rail horizontal) + "Donghua Terpopuler"
  (kartu ranking #1, #2...) + "Koleksi Lengkap Donghua" (grid 2 kolom).
- Jadwal: chip hari (SEN..MIN) + kartu jadwal (poster, judul, tombol
  bell + play kuning). Backend TIDAK memberi jam tayang — jangan tampilkan
  jam palsu.
- Koleksi: tab Favorit (n) | Riwayat (n) | Unduhan (n) + empty state
  ber-ikon sesuai referensi.
- Profil: kartu guest ("Pengunjung Tamu"), kartu login Google, kartu VIP
  emas, section "Pengaturan Streaming" (toggle: resolusi 1080p, autoplay
  episode berikutnya, unduh hanya via Wi-Fi — simpan di DataStore).
- Nama app tetap **AnimeDong** di semua teks UI.
- DILARANG mengarang data: rating, jumlah tayangan, dan jam tayang TIDAK
  boleh ditampilkan kecuali backend benar-benar menyediakannya.
- Setiap layar wajib handle: loading, empty state, error state (+ tombol retry).

## 5. Firebase — 7 layanan, kerjakan BERURUTAN

Prasyarat (sebelum coding):
1. Buat project Firebase baru untuk package `com.animedong.app`.
2. Download `google-services.json` ASLI → taruh di `app/`.
3. Tambah plugin google-services di Gradle.
4. Jika `google-services.json` BELUM ada: BERHENTI dan minta ke user.
   Dilarang membuat mock/dummy yang seolah-olah terhubung Firebase.

Urutan pengerjaan (satu layanan selesai & terverifikasi baru lanjut):

1. **Remote Config** — keys wajib:
   `api_base_url` (default `https://www.sankavollerei.web.id`),
   `donghua_api_base_url` (default `http://168.110.213.108/donghua`),
   `default_quality` (`480p`), `server_picker_enabled` (`true`),
   `min_app_version` (`1`), `ads_enabled` (`true`),
   `admob_banner_unit_id`, `admob_interstitial_unit_id`,
   `blocked_dns_hosts` (daftar hostname DNS yang dilarang, boleh kosong),
   `debug_force_premium` (`false`).
   Fetch saat app start; interval 1 jam (debug) / 12 jam (release).
   `api_base_url` MENGGANTIKAN konstanta base URL di kode.
2. **Crashlytics** — custom key `animeId`, `episodeId`, `serverDipakai`
   saat crash di player. Test WAJIB: picu crash buatan di build debug,
   pastikan muncul di dashboard ≤ 5 menit. Matikan pengiriman di build debug.
3. **Authentication** — Google Sign-In via Credential Manager.
   Guest mode WAJIB tetap bisa nonton tanpa login. Simpan `uid`
   sebagai key Firestore.
4. **Firestore** — struktur:
   `users/{uid}/bookmarks/{animeId}`, `users/{uid}/history/{episodeId}`,
   `users/{uid}/prefs`, `users/{uid}/subscription { isPremium, expiresAt, source }`,
   `users/{uid}/tokens/{fcmToken}`.
   Migrasi: saat login pertama, upload isi Room lokal ke Firestore.
   Security rules: hanya pemilik `uid` yang boleh baca/tulis datanya.
5. **Cloud Messaging** — minta izin `POST_NOTIFICATIONS` saat user
   pertama kali buka tab Jadwal/Bookmark (jangan saat splash).
   Simpan FCM token ke Firestore. (Cron server untuk notifikasi
   episode baru = komponen terpisah, BUKAN di dalam app.)
6. **Analytics** — event wajib: `episode_play`, `server_switch`,
   `bookmark_add`, `bookmark_remove`, `episode_completed`,
   `ad_interstitial_shown`, `premium_status_changed`.
   DILARANG mengirim PII (email, nama) sebagai parameter event.
7. **App Check** (Play Integrity) — inisialisasi SEBELUM layanan Firebase
   lain. Catatan jujur: ini melindungi layanan Firebase (Firestore, FCM),
   BUKAN API sankavollerei — itu butuh verifikasi token di backend
   (kerjaan backend, bukan app). Jangan klaim API sudah aman hanya
   karena App Check terpasang.

Aturan verifikasi: tiap layanan selesai HANYA jika terlihat di Firebase
Console (data masuk / crash muncul / token terdaftar). Dilarang mock.

## 6. Iklan — AdMob

- Dependency `play-services-ads`. Daftarkan `APPLICATION_ID` di Manifest.
- Saat debug WAJIB pakai TEST ad unit ID Google:
  banner `ca-app-pub-3940256099942544/6300978111`,
  interstitial `ca-app-pub-3940256099942544/1033173712`.
  Unit ID asli diambil dari Remote Config (`admob_banner_unit_id`,
  `admob_interstitial_unit_id`) saat release.
- Banner: tampil di Beranda & Detail anime. DILARANG menutupi kontrol
  player atau konten utama.
- Patuhi AdMob policy (tidak ada iklan menutupi konten, tidak ada klik
  paksa, tidak ada interstitial beruntun). Catat `app-ads.txt` sebagai
  bagian dari checklist rilis.

## 7. Premium — aturan iklan 30 menit

- Status premium: `users/{uid}/subscription.isPremium == true` DAN
  `expiresAt` masih berlaku. User guest (belum login) = tidak premium.
- **Non-premium**: interstitial BOLEH tampil maksimal **1x per 30 menit**.
  Simpan `last_interstitial_at` (DataStore). Tampilkan hanya jika
  `now - last >= 30 menit` DAN iklan sudah loaded. Titik tampil hanya
  di jeda natural (mis. saat membuka episode / kembali ke Beranda) —
  DILARANG memotong video yang sedang diputar.
- **Premium**: NOL iklan — banner disembunyikan, interstitial dilewati total.
- `BillingRepository` (interface) adalah SATU-SATUNYA sumber status premium,
  expose sebagai `Flow<Boolean>`. Implementasi membaca Firestore;
  flow pembelian Google Play Billing boleh stub dengan TODO yang jelas
  + Remote Config `debug_force_premium` untuk testing. Jangan mengklaim
  billing sudah jalan kalau masih stub.
- Layar Profil: tampilkan status premium + tombol "Jadi Premium".

## 8. Larangan Private DNS / DNS kustom

Alasan (tulis jujur di dialog, tanpa hype): DNS kustom / Private DNS
dapat memblokir iklan dan merusak koneksi ke server streaming,
jadi tidak diizinkan di AnimeDong.

- Cek saat splash DAN setiap `onResume`:
  `Settings.Global.getString(contentResolver, "private_dns_mode")`
  → `"hostname"` = user memakai Private DNS kustom = BLOKIR.
  `"off"` / `"opportunistic"` = OK, lanjutkan.
- Opsional: cek `LinkProperties.getDnsServers()` terhadap daftar
  `blocked_dns_hosts` dari Remote Config.
- Jika terdeteksi, tampilkan dialog yang TIDAK bisa di-dismiss:
  - Judul: "Private DNS Terdeteksi"
  - Pesan: singkat & jujur (contoh: "AnimeDong tidak mendukung Private
    DNS / DNS kustom karena dapat memblokir iklan dan mengganggu
    streaming. Matikan dulu untuk melanjutkan.")
  - Tombol 1 **"Buka Pengaturan DNS"** → coba
    `Intent("android.settings.PRIVATE_DNS_SETTINGS")`; cek
    `resolveActivity()` dulu, fallback ke
    `Settings.ACTION_WIRELESS_SETTINGS`. WAJIB verifikasi di HP asli
    (Android 9+), jangan asumsi intent-nya ada.
  - Tombol 2 **"Restart AnimeDong"** → restart penuh proses aplikasi:
    `getLaunchIntentForPackage(packageName)` + `AlarmManager` +
    `exitProcess(0)` (atau `ProcessPhoenix.triggerRebirth(context)`).

## 9. Aturan anti-slop (wajib)

- Jangan mengarang angka, statistik, atau klaim ("10.000+ anime", "tercepat")
  kecuali datanya benar-benar ada.
- Komentar kode: HAPUS yang hanya mengulang kode (`// loop through items`
  di atas loop). SIMPAN yang menjelaskan KENAPA sesuatu dilakukan.
- Tidak ada banner ASCII, emoji dekoratif, atau section divider di kode.
- Copy di UI: jujur dan langsung. Dilarang hype kosong
  ("TERBAIK", "100% GRATIS", "TANPA IKLAN" kecuali benar).
- Jangan menambah dependency/library baru tanpa menjelaskan kenapa
  yang sudah ada tidak cukup.
- Kode yang tidak dipakai: hapus, jangan dikomen.

## 10. Cara kerja

1. Sebelum menulis kode, baca backend contract di atas dan file terkait
   yang sudah ada. Jangan menebak struktur yang belum kamu baca.
2. Untuk fitur baru: mulai dari DTO -> repository -> ViewModel -> UI.
   Jangan lompat langsung ke UI.
3. Setiap selesai, laporkan: file yang diubah + cara test manualnya.
   Jangan bilang "selesai" tanpa bisa diverifikasi.
4. Kalau ada trade-off (mis. dua cara implementasi), sebutkan singkat
   dan pilih satu dengan alasan — jangan tanya balik untuk hal sepele.
