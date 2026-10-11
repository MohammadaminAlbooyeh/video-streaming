package it.bollofacile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class BolloFacileApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Inizializza AdMob
        MobileAds.initialize(this) {}
        
        // Inizializza UMP Consent
        initUmpConsent()
    }

    private fun initUmpConsent() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = ConsentInformation.getInstance(this)
        
        CoroutineScope(Dispatchers.IO).launch {
            consentInformation.requestConsentInfoUpdate(this@BolloFacileApp, params) {
                if (it != null) {
                    // Errore richiesta consenso
                    return@launch
                }
                
                if (consentInformation.isConsentFormAvailable) {
                    CoroutineScope(Dispatchers.Main).launch {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(this@BolloFacileApp) { error ->
                            // Gestione errore form consenso
                        }
                    }
                }
            }
        }
    }
}