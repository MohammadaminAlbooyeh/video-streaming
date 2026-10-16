package it.bollofacile.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import it.bollofacile.R
import it.bollofacile.data.prefs.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Interstitial frequency policy:
 * - No interstitials during the first [GRACE_SESSIONS] app sessions.
 * - At most one interstitial per session.
 * - At least [MIN_INTERVAL_MS] between two interstitials (persisted across sessions).
 * - One interstitial every [ACTIONS_PER_AD] completed user actions (save vehicle, finish calc).
 * - Never blocks the user: if no ad is preloaded, the action completes silently.
 */
class AdManager(
    private val context: Context,
    private val settingsStore: SettingsStore
) {

    companion object {
        const val GRACE_SESSIONS = 2
        const val ACTIONS_PER_AD = 3
        const val MIN_INTERVAL_MS = 3 * 60 * 1000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var shownThisSession = false
    private var sessionCount = 0

    /** Call once per app process start (e.g. from MainActivity.onCreate). */
    fun onSessionStart() {
        shownThisSession = false
        scope.launch {
            sessionCount = settingsStore.incrementAdSessionCount()
            // Only pay for an ad load when this session could actually show one.
            if (sessionCount > GRACE_SESSIONS) {
                preloadInterstitial()
            }
        }
    }

    /**
     * Call when the user completes a natural end-of-task action.
     * Shows an interstitial only when every policy rule passes, then runs [onDone].
     * [onDone] always runs (immediately when no ad is shown).
     */
    fun onActionCompleted(activity: Activity, onDone: () -> Unit = {}) {
        scope.launch {
            val actions = settingsStore.incrementAdActionCounter()

            val eligible = sessionCount > GRACE_SESSIONS &&
                !shownThisSession &&
                actions >= ACTIONS_PER_AD &&
                System.currentTimeMillis() - settingsStore.getLastInterstitialAt() >= MIN_INTERVAL_MS

            val ad = interstitialAd
            if (!eligible || ad == null) {
                if (eligible && ad == null) preloadInterstitial()
                onDone()
                return@launch
            }

            interstitialAd = null
            shownThisSession = true
            settingsStore.resetAdActionCounter()
            settingsStore.setLastInterstitialAt(System.currentTimeMillis())

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    preloadInterstitial()
                    onDone()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    preloadInterstitial()
                    onDone()
                }
            }
            ad.show(activity)
        }
    }

    private fun preloadInterstitial() {
        if (isLoading || interstitialAd != null) return
        isLoading = true
        InterstitialAd.load(
            context,
            context.getString(R.string.admob_interstitial_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    // No retry loop: the next completed action triggers a fresh load.
                }
            }
        )
    }
}
