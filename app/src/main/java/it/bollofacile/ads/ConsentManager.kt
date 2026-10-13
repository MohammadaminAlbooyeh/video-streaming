package it.bollofacile.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class ConsentManager(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    fun requestConsentAndShowFormIfRequired(
        activity: Activity,
        onComplete: (canRequestAds: Boolean) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    onComplete(consentInformation.canRequestAds())
                }
            },
            {
                onComplete(consentInformation.canRequestAds())
            }
        )
    }

    fun getConsentStatus(): Int = consentInformation.consentStatus

    fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    fun resetConsent() {
        consentInformation.reset()
    }
}
