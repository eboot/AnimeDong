package com.animedong.app.di

import android.content.Context
import androidx.room.Room
import com.animedong.app.core.network.AnimeApiService
import com.animedong.app.core.network.DonghuaApiService
import com.animedong.app.core.network.createRetrofit
import com.animedong.app.data.ads.AdManager
import com.animedong.app.data.auth.AuthManager
import com.animedong.app.data.billing.BillingRepositoryImpl
import com.animedong.app.data.local.AppDatabase
import com.animedong.app.data.local.MIGRATION_1_2
import com.animedong.app.data.local.PrefsManager
import com.animedong.app.data.remote.RemoteConfigManager
import com.animedong.app.data.kurama.KuramaStreamExtractor
import com.animedong.app.data.repository.AnimeRepository
import com.animedong.app.data.repository.DonghuaRepository
import com.animedong.app.domain.billing.BillingRepository
import retrofit2.Retrofit

/**
 * Manual DI. Retrofit client bisa di-rebuild saat Remote Config
 * mengubah api_base_url / donghua_api_base_url (tanpa rilis update).
 */
class AppContainer(private val context: Context) {

    val remoteConfig = RemoteConfigManager()
    val prefs = PrefsManager(context.applicationContext)

    @Volatile
    private var retrofit: Retrofit = createRetrofit(remoteConfig.apiBaseUrl)

    val api: AnimeApiService get() = retrofit.create(AnimeApiService::class.java)

    @Volatile
    private var donghuaRetrofit: Retrofit =
        createRetrofit(remoteConfig.donghuaApiBaseUrl)

    val donghuaApi: DonghuaApiService
        get() = donghuaRetrofit.create(DonghuaApiService::class.java)

    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "animedong.db"
    ).addMigrations(MIGRATION_1_2).build()

    val kuramaExtractor = KuramaStreamExtractor(context.applicationContext)

    val repository = AnimeRepository(api, database, kuramaExtractor)
    val donghuaRepository = DonghuaRepository(donghuaApi)

    val billingRepository: BillingRepository = BillingRepositoryImpl(remoteConfig)
    val authManager = AuthManager(context.applicationContext)
    val adManager = AdManager(
        context.applicationContext, remoteConfig, billingRepository, prefs
    )

    /** Rebuild Retrofit + repository API setelah Remote Config fetch. */
    fun refreshApiClient() {
        retrofit = createRetrofit(remoteConfig.apiBaseUrl)
        repository.updateApi(api)
        donghuaRetrofit = createRetrofit(remoteConfig.donghuaApiBaseUrl)
        donghuaRepository.updateApi(donghuaApi)
    }
}
