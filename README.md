# AnimeDong — Kotlin Native

Aplikasi Android streaming donghua & anime subtitle Indonesia —
**Kotlin native** (Jetpack Compose + Clean Architecture + MVVM),
nyambung ke DUA backend:

| Konten | Backend | Base URL default |
|---|---|---|
| Anime | sankavollerei (tanpa auth) | `https://www.sankavollerei.web.id` |
| Donghua | API sendiri di Oracle VPS (tanpa auth) | `http://168.110.213.108/donghua` |

> Catatan: folder masih bernama `donghive-kotlin` (historis), tapi seluruh
> identitas app sudah diganti jadi **AnimeDong** (`com.animedong.app`).
> UI/UX sengaja dibuat beda dari Donghive — hasil reverse-engineering
> Donghive 5.7 hanya dipakai sebagai referensi fitur.

## Fitur

- Beranda dengan toggle **Anime | Donghua**: spotlight, rail, dan grid
  per tipe konten (section mengikuti nama asli dari API — tidak ada
  label karangan)
- Jadwal mingguan per tipe konten (chip hari Senin–Minggu)
- Detail + Bookmark (Room, kolom `content_type`), Riwayat nonton
- Player anime: Media3 ExoPlayer + ganti server (resolve serverId)
- Player donghua: WebView + pilih server (URL embed langsung dari API)
- Firebase: Remote Config, Crashlytics, Auth (Google), Firestore,
  FCM, Analytics, App Check (Play Integrity)
- AdMob: banner (Beranda & Detail) + interstitial maksimal **1x per 30 menit**
  untuk user non-premium, hanya di jeda natural (tidak memotong video)
- Premium: status dari Firestore `users/{uid}/subscription/current`;
  user premium = nol iklan
- Larangan Private DNS: dialog non-dismissable dengan tombol
  **Buka Pengaturan DNS** dan **Restart AnimeDong**

## Cara jalanin

```bash
# Buka di Android Studio (Koala+), sync Gradle, Run.
# Atau via command line:
./gradlew installDebug
```

Butuh: JDK 17, Android SDK 35.

## Setup wajib sebelum run

1. **Firebase**: buat project di Firebase Console untuk package
   `com.animedong.app`, download `google-services.json` asli → taruh di
   `app/`. Tanpa file ini app tidak akan jalan (disengaja, anti-mock).
   Aktifkan: Authentication (Google), Firestore, FCM, Crashlytics,
   Remote Config, App Check (Play Integrity).
2. **Google Sign-In**: isi `GOOGLE_WEB_CLIENT_ID` di
   `data/auth/AuthManager.kt` dengan Web Client ID dari Firebase Console.
3. **AdMob**: build debug otomatis pakai test ad unit ID. Untuk release,
   ganti `admobAppId` di `app/build.gradle.kts` (blok `release`) dengan
   App ID asli, dan isi `admob_banner_unit_id` /
   `admob_interstitial_unit_id` di Remote Config.
4. **Remote Config keys** (lihat `data/remote/RemoteConfigManager.kt`
   untuk default): `api_base_url`, `donghua_api_base_url`,
   `default_quality`, `server_picker_enabled`, `min_app_version`,
   `ads_enabled`, `admob_banner_unit_id`, `admob_interstitial_unit_id`,
   `blocked_dns_hosts`, `debug_force_premium`.
5. **HTTP API donghua**: cleartext hanya diizinkan untuk host
   `168.110.213.108` via `app/src/main/res/xml/network_security_config.xml`.
   Kalau base URL donghua pindah host/HTTPS, sesuaikan file itu.

## Struktur

```
app/src/main/java/com/animedong/app/
├── MainActivity.kt              # + dialog blokir Private DNS (cek tiap onResume)
├── AnimeDongApp.kt              # init Firebase, App Check, MobileAds, Remote Config
├── di/AppContainer.kt           # manual DI
├── core/
│   ├── network/                 # AnimeApiService + DonghuaApiService
│   │                            # (base URL bisa diganti via Remote Config)
│   ├── theme/                   # AnimeDongTheme (aksen kuning #FFC107, navy gelap)
│   └── ui/AnimeCard.kt
├── data/
│   ├── dto/                     # DTO persis struktur JSON backend (+ DonghuaDtos)
│   ├── local/                   # Room v2 (watch_history, bookmarks + content_type)
│   │                            # + PrefsManager (DataStore)
│   ├── remote/RemoteConfigManager.kt
│   ├── repository/AnimeRepository.kt    # DTO -> domain mapping (anime + Room)
│   ├── repository/DonghuaRepository.kt # DTO -> domain mapping (donghua)
│   ├── ads/AdManager.kt         # banner + interstitial (throttle 30 mnt)
│   ├── auth/AuthManager.kt      # Google Sign-In via Credential Manager
│   ├── billing/BillingRepositoryImpl.kt  # status premium dari Firestore
│   ├── dns/DnsChecker.kt        # deteksi Private DNS + restart app
│   └── messaging/               # FCM service
├── domain/
│   ├── model/                   # model bersih untuk UI
│   └── billing/BillingRepository.kt    # interface status premium
└── presentation/
    ├── navigation/NavGraph.kt   # route: detail/{type}/{id}, player/{type}/{id}
    ├── home/ detail/ schedule/ library/
    ├── player/                  # PlayerScreen (anime, ExoPlayer)
    │                            # + DonghuaPlayerScreen (donghua, WebView)
    └── profile/                 # layar "Saya": auth + status premium + upgrade
```

## Alur nonton

1. Beranda → toggle Anime/Donghua → tap poster → `detail/{type}/{id}`
2. Detail → tap episode → (cek interstitial maks 1x/30 mnt) → `player/{type}/{id}`
3. Player anime: autoplay `defaultStreamingUrl` (Media3 ExoPlayer);
   "Ganti Server" → bottom sheet kualitas → server list
   → `GET anime/server/{serverId}` → URL final → play
4. Player donghua: WebView membuka URL embed dari
   `GET /episode/{episodeSlug}` → `players[{name, url}]`;
   "Ganti Server" → pilih nama server → WebView reload
5. Keluar player anime → posisi tersimpan ke Room `watch_history`;
   donghua hanya tercatat "sudah ditonton" (embed tidak bisa resume)

## TODO sebelum production

- [ ] `resolveServerUrl()`: cek bentuk asli `data` dari `/anime/server/{id}`
      (pernah 404 karena ID expired), sesuaikan parsing
- [ ] Integrasi Google Play Billing asli di `BillingRepositoryImpl.launchPurchase()`
      (saat ini stub "segera hadir")
- [ ] Verifikasi intent `android.settings.PRIVATE_DNS_SETTINGS` di HP asli
      (ada fallback ke Wireless Settings)
- [ ] Cron/server terpisah untuk push notifikasi episode baru via FCM
- [ ] `app-ads.txt`, signing release, `bundleRelease`
