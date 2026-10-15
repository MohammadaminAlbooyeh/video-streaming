package it.bollofacile

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import it.bollofacile.data.rates.RatesRepository
import it.bollofacile.reminder.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class BolloFacileApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        NotificationHelper.createNotificationChannels(this)
        appScope.launch { RatesRepository.loadAndRefresh(this@BolloFacileApp) }
    }
}
