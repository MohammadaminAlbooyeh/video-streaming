package it.bollofacile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import it.bollofacile.data.rates.RatesRepository
import it.bollofacile.reminder.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The Google Mobile Ads SDK is NOT initialized here. It is initialized only after the
 * user's GDPR/UMP consent decision is known (see MainActivity), so no ad request or
 * ad SDK activity happens before consent is obtained for EEA/UK users.
 */
@HiltAndroidApp
class BolloFacileApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        appScope.launch { RatesRepository.loadAndRefresh(this@BolloFacileApp) }
    }
}
