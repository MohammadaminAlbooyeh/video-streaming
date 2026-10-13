package it.bollofacile

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import it.bollofacile.reminder.NotificationHelper

@HiltAndroidApp
class BolloFacileApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        NotificationHelper.createNotificationChannels(this)
    }
}
