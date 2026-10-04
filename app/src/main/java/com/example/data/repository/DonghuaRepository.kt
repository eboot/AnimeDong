package com.example.data.repository

import com.example.data.local.DonghuaDao
import com.example.data.local.DownloadEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.model.Donghua
import com.example.data.model.Episode
import com.example.data.model.StreamServer
import com.example.data.model.UserComment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DonghuaRepository(private val dao: DonghuaDao) {

    // Reliable playable video streams
    private val sampleVideoUrl1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private val sampleVideoUrl2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    private val sampleVideoUrl3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

    private val donghuaList = listOf(
        Donghua(
            id = "btth",
            title = "Battle Through the Heavens S5",
            chineseTitle = "斗破苍穹 第五季 (Doupo Cangqiong)",
            synopsis = "Di negeri di mana sihir tidak ada, yang kuat membuat aturan dan yang lemah harus patuh. Xiao Yan, yang pernah dianggap jenius tanpa tandingan, tiba-tiba kehilangan semua kekuatannya. Setelah menemukan jiwa gurunya Yao Lao di dalam cincin pusaka, Xiao Yan memulai perjalanan kultivasi legendaris untuk menaklukkan Api Surgawi dan membalaskan dendam keluarganya di Akademi Jia Nan.",
            coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            rating = 9.8f,
            status = "Ongoing",
            releaseYear = "2024",
            studio = "Motion Magic & Tencent Penguin Pictures",
            genres = listOf("Xianxia", "Aksi", "Kultivasi", "Petualangan", "Fantasi"),
            totalEpisodes = 156,
            latestEpisode = 118,
            releaseDay = "Minggu",
            releaseTime = "10:00 WIB",
            isFeatured = true,
            isPopular = true,
            views = "4.8M"
        ),
        Donghua(
            id = "perfect-world",
            title = "Perfect World",
            chineseTitle = "完美世界 (Wanmei Shijie)",
            synopsis = "Shi Hao lahir dengan Tulang Tertinggi yang langka di dunia, namun direbut secara kejam oleh kerabatnya sendiri saat masih balita. Ditinggalkan di Desa Batu terpencil di bawah perlindungan pohon dewa Willow, Shi Hao bangkit kembali dengan tekad baja, menguasai teknik magis Kunpeng kuno dan mengguncang Delapan Domain untuk merebut kembali takdirnya.",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&auto=format&fit=crop&q=80",
            rating = 9.7f,
            status = "Ongoing",
            releaseYear = "2023",
            studio = "Foch Film",
            genres = listOf("Aksi", "Kultivasi", "Dewa", "Supernatural"),
            totalEpisodes = 180,
            latestEpisode = 162,
            releaseDay = "Jumat",
            releaseTime = "10:00 WIB",
            isFeatured = true,
            isPopular = true,
            views = "3.9M"
        ),
        Donghua(
            id = "soul-land-2",
            title = "Soul Land II: Peerless Tang Clan",
            chineseTitle = "斗罗大陆II 绝世唐门 (Douluo Dalu 2)",
            synopsis = "Sepuluh ribu tahun setelah berdirinya Sekte Tang oleh Tang San di Benua Douluo, sekte legendaris itu mulai meredup. Huo Yuhao, seorang pemuda yang lahir dengan Mata Roh Langka dan dipandang rendah, berkelana ke Hutan Bintang Dou dan takdirnya terjalin dengan Ular Sutra Es Berusia Sejuta Tahun. Bersama Akademi Shrek, dia menghidupkan kembali kejayaan Sekte Tang.",
            coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=1200&auto=format&fit=crop&q=80",
            rating = 9.6f,
            status = "Ongoing",
            releaseYear = "2024",
            studio = "Sparkly Key Animation",
            genres = listOf("Fantasi", "Aksi", "Sihir", "Romansa", "Sains"),
            totalEpisodes = 104,
            latestEpisode = 68,
            releaseDay = "Sabtu",
            releaseTime = "10:00 WIB",
            isFeatured = true,
            isPopular = true,
            views = "4.2M"
        ),
        Donghua(
            id = "renegade-immortal",
            title = "Renegade Immortal",
            chineseTitle = "仙逆 (Xian Ni)",
            synopsis = "Wang Lin adalah anak desa biasa yang ingin mengubah nasib keluarganya dengan bergabung ke sekte kultivator. Ditolak karena bakatnya yang buruk, ia secara tidak sengaja mendapatkan Manik Pembangkang Surga misterius. Dengan bimbingan jiwa kuno Situ Nan, Wang Lin memilih jalur kultivasi Pembantaian Dingin demi melindungi apa yang dicintainya.",
            coverUrl = "https://images.unsplash.com/photo-1514539079130-25950c84af65?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200&auto=format&fit=crop&q=80",
            rating = 9.9f,
            status = "Ongoing",
            releaseYear = "2023",
            studio = "Build Dream Animation",
            genres = listOf("Kultivasi", "Aksi Gelap", "Xianxia", "Tragedi"),
            totalEpisodes = 72,
            latestEpisode = 56,
            releaseDay = "Senin",
            releaseTime = "10:00 WIB",
            isFeatured = true,
            isPopular = true,
            views = "5.1M"
        ),
        Donghua(
            id = "swallowed-star",
            title = "Swallowed Star",
            chineseTitle = "吞噬星空 (Tunshi Xingkong)",
            synopsis = "Virus RR mengubah bumi menjadi tempat penuh binatang mutan raksasa. Manusia yang tersisa membangun kota benteng dan melatih para pejuang tempur. Luo Feng berjuang dari seorang siswa miskin menjadi Pejuang Tingkat Planet terkuat di bawah bimbingan AI cerdas Babata, mempersiapkan umat manusia menghadapi ancaman antar galaksi yang mengintai.",
            coverUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200&auto=format&fit=crop&q=80",
            rating = 9.5f,
            status = "Ongoing",
            releaseYear = "2024",
            studio = "Sparkly Key Animation",
            genres = listOf("Sci-Fi", "Mecha", "Kultivasi Modern", "Aksi"),
            totalEpisodes = 140,
            latestEpisode = 124,
            releaseDay = "Rabu",
            releaseTime = "10:00 WIB",
            isFeatured = false,
            isPopular = true,
            views = "3.4M"
        ),
        Donghua(
            id = "a-will-eternal",
            title = "A Will Eternal S2",
            chineseTitle = "一念永恒 (Yi Nian Yong Heng)",
            synopsis = "Bai Xiaochun adalah pemuda penakut yang terobsesi dengan satu hal: hidup abadi dan tidak mati! Demi mencari keabadian, ia bergabung dengan Sekte Spirit Stream. Di balik kepolosannya yang kocak dan sering menyebabkan kekacauan resep alkimia di sekte, Bai Xiaochun memiliki kecerdasan dan kekuatan luar biasa yang melampaui para tetua.",
            coverUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1200&auto=format&fit=crop&q=80",
            rating = 9.4f,
            status = "Ongoing",
            releaseYear = "2024",
            studio = "B.CMAY PICTURES",
            genres = listOf("Komedi", "Kultivasi", "Aksi", "Petualangan"),
            totalEpisodes = 106,
            latestEpisode = 94,
            releaseDay = "Rabu",
            releaseTime = "16:00 WIB",
            isFeatured = false,
            isPopular = true,
            views = "2.8M"
        ),
        Donghua(
            id = "throne-of-seal",
            title = "Throne of Seal",
            chineseTitle = "神印王座 (Shen Yin Wang Zuo)",
            synopsis = "Ketika iblis menyerbu peradaban manusia dengan 72 Pilar Dewa Iblis, enam Kuil Suci manusia bersatu membangun garis pertahanan terakhir. Long Haochen mendedikasikan hidupnya menjadi Ksatria Suci untuk melindungi ibunya dan gadis misterius bernama Sheng Cai'er yang tidak bisa berbicara. Dengan Jiwa Cahaya Murni, ia membidik Singgasana Ilahi.",
            coverUrl = "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            rating = 9.3f,
            status = "Ongoing",
            releaseYear = "2023",
            studio = "Shenman Entertainment",
            genres = listOf("Fantasi", "Ksatria", "Romansa", "Sihir"),
            totalEpisodes = 120,
            latestEpisode = 112,
            releaseDay = "Kamis",
            releaseTime = "10:00 WIB",
            isFeatured = false,
            isPopular = true,
            views = "2.9M"
        ),
        Donghua(
            id = "shrouding-heavens",
            title = "Shrouding the Heavens",
            chineseTitle = "遮天 (Zhe Tian)",
            synopsis = "Dalam kegelapan ruang angkasa yang sunyi dan dingin, sembilan naga raksasa menarik peti mati perunggu kuno yang melintasi miliaran tahun cahaya. Peti mati itu jatuh di Gunung Tai bumi, membawa Ye Fan dan teman-teman sekelasnya ke dunia kuno galaksi Big Dipper yang penuh praktisi misterius dan pertempuran kaisar kuno.",
            coverUrl = "https://images.unsplash.com/photo-1507499739999-097706ad8914?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&auto=format&fit=crop&q=80",
            rating = 9.2f,
            status = "Ongoing",
            releaseYear = "2023",
            studio = "Sparkly Key Animation",
            genres = listOf("Misteri", "Kultivasi Luar Angkasa", "Xianxia"),
            totalEpisodes = 80,
            latestEpisode = 64,
            releaseDay = "Rabu",
            releaseTime = "10:00 WIB",
            isFeatured = false,
            isPopular = false,
            views = "2.1M"
        ),
        Donghua(
            id = "mortal-journey",
            title = "Record of a Mortal's Journey to Immortality",
            chineseTitle = "凡人修仙传 (Fanren Xiu Xian Chuan)",
            synopsis = "Han Li adalah pemuda desa berhati-hati yang masuk ke Sekte Yellow Maple dengan kualifikasi spiritual yang sangat biasa. Mengandalkan botol hijau misterius yang mampu mempercepat pertumbuhan tanaman obat langka dan prinsip kehati-hatian tingkat tinggi untuk selalu menjaga profil rendah, Han Li bertahan di dunia kultivator yang kejam.",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
            rating = 9.9f,
            status = "Ongoing",
            releaseYear = "2024",
            studio = "Original Force Animation",
            genres = listOf("Kultivasi Realistis", "Xianxia", "Petualangan", "Taktik"),
            totalEpisodes = 110,
            latestEpisode = 102,
            releaseDay = "Sabtu",
            releaseTime = "11:00 WIB",
            isFeatured = false,
            isPopular = true,
            views = "4.5M"
        ),
        Donghua(
            id = "against-the-gods",
            title = "Against the Gods",
            chineseTitle = "逆天邪神 (Ni Tian Xie Shen)",
            synopsis = "Dikejar oleh pemburu harta karun karena Mutiara Racun Langit, Yun Che melompat dari Tebing Keputusasaan Mengakhiri Awan dan bereinkarnasi ke tubuh seorang pemuda lumpuh dengan urat nadi rusak di Benua Langit Biru. Membuka kekuatan Urat Nadi Dewa Jahat, ia bangkit menentang dewa dan langit demi keadilan cintanya.",
            coverUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1514539079130-25950c84af65?w=1200&auto=format&fit=crop&q=80",
            rating = 9.1f,
            status = "Ongoing",
            releaseYear = "2023",
            studio = "Foch Film",
            genres = listOf("Aksi", "Harem", "Balas Dendam", "Kultivasi"),
            totalEpisodes = 60,
            latestEpisode = 42,
            releaseDay = "Selasa",
            releaseTime = "10:00 WIB",
            isFeatured = false,
            isPopular = false,
            views = "1.8M"
        ),
        Donghua(
            id = "the-great-ruler",
            title = "The Great Ruler",
            chineseTitle = "大主宰 (Da Zhu Zai)",
            synopsis = "Dunia Seribu Besar adalah titik pertemuan jutaan dunia dimensi. Mu Chen dari Wilayah Spiritual Utara dikeluarkan dari Jalan Spiritual karena bencana misterius, namun ia tidak menyerah. Bersama gadis misterius Luo Li, ia melangkah melintasi benua untuk membuktikan dirinya sebagai Penguasa Agung sejati.",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=1200&auto=format&fit=crop&q=80",
            rating = 9.0f,
            status = "Ongoing",
            releaseYear = "2023",
            studio = "Foch Film",
            genres = listOf("Fantasi", "Aksi", "Akademi", "Romansa"),
            totalEpisodes = 52,
            latestEpisode = 48,
            releaseDay = "Selasa",
            releaseTime = "10:00 WIB",
            isFeatured = false,
            isPopular = false,
            views = "1.6M"
        ),
        Donghua(
            id = "stellar-transformation",
            title = "Stellar Transformation S5",
            chineseTitle = "星辰变 (Xingchen Bian)",
            synopsis = "Qin Yu terlahir tidak mampu berkultivasi energi internal Dan Tian, namun ia tidak menyerah dan melatih fisiknya melebihi batasan manusia. Menemukan Kristal Air Mata Meteor misterius dan warisan teknik Transformasi Bintang, Qin Yu melompat dari dunia fana menuju alam keabadian kosmik.",
            coverUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=800&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&auto=format&fit=crop&q=80",
            rating = 9.1f,
            status = "Tamat",
            releaseYear = "2023",
            studio = "Shanghai Foch Film",
            genres = listOf("Kultivasi Kosmis", "Aksi", "Petualangan"),
            totalEpisodes = 56,
            latestEpisode = 56,
            releaseDay = "Kamis",
            releaseTime = "10:00 WIB",
            isFeatured = false,
            isPopular = false,
            views = "2.3M"
        )
    )

    // User comments mock state
    private val _commentsState = MutableStateFlow(
        listOf(
            UserComment("c1", "btth", 118, "Rian Kultivator", "R", "Animasi pertarungan Xiao Yan di episode ini gila banget grafisnya! Api Surgawinya epic abis 🔥", "10 menit lalu", 42),
            UserComment("c2", "btth", 118, "DonghuaLover99", "D", "Kualitas 1080p di AnimeDong lancar jaya no buffering, mantap min lanjutkan!", "35 menit lalu", 28),
            UserComment("c3", "btth", 118, "Suhu Wang", "S", "Yao Lao akhirnya mulai pulih, nggak sabar nunggu pertarungan di Sekte Misty Cloud babak selanjutnya.", "2 jam lalu", 15),
            UserComment("c4", "perfect-world", 162, "HuangTian", "H", "Shi Hao makin OP parah! Jurus Kunpengnya cinematic banget.", "1 jam lalu", 31)
        )
    )
    val comments = _commentsState.asStateFlow()

    fun addComment(donghuaId: String, episodeNumber: Int, text: String, userName: String = "Sobat Donghua") {
        val newComment = UserComment(
            id = "c_${System.currentTimeMillis()}",
            donghuaId = donghuaId,
            episodeNumber = episodeNumber,
            userName = userName,
            avatarInitial = userName.take(1).uppercase(),
            commentText = text,
            timeAgo = "Baru saja",
            likes = 1
        )
        _commentsState.value = listOf(newComment) + _commentsState.value
    }

    // Catalog queries
    fun getAllDonghua(): List<Donghua> = donghuaList

    fun getFeaturedDonghua(): List<Donghua> = donghuaList.filter { it.isFeatured }

    fun getPopularDonghua(): List<Donghua> = donghuaList.filter { it.isPopular }

    fun getLatestEpisodes(): List<Donghua> = donghuaList.sortedByDescending { it.latestEpisode }

    fun getDonghuaById(id: String): Donghua? = donghuaList.find { it.id == id } ?: donghuaList.firstOrNull()

    fun searchDonghua(query: String, selectedGenre: String, statusFilter: String): List<Donghua> {
        return donghuaList.filter { donghua ->
            val matchQuery = query.isBlank() ||
                    donghua.title.contains(query, ignoreCase = true) ||
                    donghua.chineseTitle.contains(query, ignoreCase = true) ||
                    donghua.genres.any { it.contains(query, ignoreCase = true) }
            val matchGenre = selectedGenre == "Semua" || donghua.genres.contains(selectedGenre)
            val matchStatus = statusFilter == "Semua" || donghua.status.equals(statusFilter, ignoreCase = true)
            matchQuery && matchGenre && matchStatus
        }
    }

    fun getScheduleForDay(dayName: String): List<Donghua> {
        return donghuaList.filter { it.releaseDay.equals(dayName, ignoreCase = true) }
    }

    fun getEpisodesForDonghua(donghua: Donghua): List<Episode> {
        val list = mutableListOf<Episode>()
        val count = donghua.latestEpisode
        for (i in count downTo 1) {
            val videoUrl = when (i % 3) {
                0 -> sampleVideoUrl1
                1 -> sampleVideoUrl2
                else -> sampleVideoUrl3
            }
            list.add(
                Episode(
                    id = "${donghua.id}_ep_$i",
                    donghuaId = donghua.id,
                    episodeNumber = i,
                    title = "Episode $i : ${donghua.title}",
                    releaseDate = "${(count - i) + 1} hari lalu",
                    durationMinutes = 20,
                    streamServers = listOf(
                        StreamServer("vip_1", "VIP Server 1 (HD 1080p)", "1080p Ultra", videoUrl, isVip = true),
                        StreamServer("fast_2", "Fast CDN Server 2", "720p HD", videoUrl),
                        StreamServer("mirror_3", "Mirror Server 3 (Hemat Kuota)", "480p SD", videoUrl)
                    )
                )
            )
        }
        return list
    }

    // Room DB integration
    val allFavorites: Flow<List<FavoriteEntity>> = dao.getAllFavorites()
    val allHistory: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val allDownloads: Flow<List<DownloadEntity>> = dao.getAllDownloads()

    fun isFavorite(donghuaId: String): Flow<Boolean> = dao.isFavorite(donghuaId)

    suspend fun toggleFavorite(donghua: Donghua, currentIsFav: Boolean) {
        if (currentIsFav) {
            dao.deleteFavoriteById(donghua.id)
        } else {
            val entity = FavoriteEntity(
                donghuaId = donghua.id,
                title = donghua.title,
                chineseTitle = donghua.chineseTitle,
                coverUrl = donghua.coverUrl,
                rating = donghua.rating,
                status = donghua.status,
                latestEpisode = donghua.latestEpisode,
                genres = donghua.genres.joinToString(",")
            )
            dao.insertFavorite(entity)
        }
    }

    suspend fun saveWatchHistory(
        donghua: Donghua,
        episodeNumber: Int,
        progressMs: Long,
        durationMs: Long
    ) {
        val entity = HistoryEntity(
            id = "${donghua.id}_$episodeNumber",
            donghuaId = donghua.id,
            donghuaTitle = donghua.title,
            episodeNumber = episodeNumber,
            coverUrl = donghua.coverUrl,
            progressMs = progressMs,
            durationMs = durationMs
        )
        dao.insertHistory(entity)
    }

    suspend fun removeHistory(id: String) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }

    suspend fun addDownload(donghua: Donghua, episodeNumber: Int, quality: String = "720p HD") {
        val entity = DownloadEntity(
            id = "${donghua.id}_$episodeNumber",
            donghuaId = donghua.id,
            donghuaTitle = donghua.title,
            episodeNumber = episodeNumber,
            coverUrl = donghua.coverUrl,
            quality = quality,
            fileSizeMb = 145.5f,
            downloadStatus = "Selesai",
            progressPercent = 100
        )
        dao.insertDownload(entity)
    }

    suspend fun removeDownload(id: String) {
        dao.deleteDownloadById(id)
    }
}
