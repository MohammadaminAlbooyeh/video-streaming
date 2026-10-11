package it.bollofacile.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await

class ConsentManager(private val context: Context) {

    private val consentInformation = ConsentInformation.getInstance(context)

    suspend fun requestConsentInfoUpdate(): Boolean = coroutineScope {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        try {
            consentInformation.requestConsentInfoUpdate(context, params).await()
            return@coroutineScope true
        } catch (e: Exception) {
            return@coroutineScope false
        }
    }

    suspend fun loadAndShowConsentFormIfRequired(activity: Activity): Boolean = coroutineScope {
        if (!consentInformation.isConsentFormAvailable) {
            return@coroutineScope false
        }

        try {
            val form = UserMessagingPlatform.loadConsentForm(context).await()
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                form.show(activity).await()
            }
            return@coroutineScope true
        } catch (e: Exception) {
            return@coroutineScope false
        }
    }

    fun getConsentStatus(): ConsentInformation.ConsentStatus {
        return consentInformation.consentStatus
    }

    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds
    }

    fun resetConsent() {
        consentInformation.reset()
    }
}