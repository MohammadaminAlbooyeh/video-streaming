package it.bollofacile.ads

import android.content.Context
import it.bollofacile.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private val adUnitIdBanner = context.getString(R.string.admob_banner_id)
    private val adUnitIdInterstitial = context.getString(R.string.admob_interstitial_id)

    fun loadBanner(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    fun loadInterstitial(onLoaded: (InterstitialAd) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            InterstitialAd.load(
                context,
                adUnitIdInterstitial,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        this@AdManager.interstitialAd = interstitialAd
                        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                loadInterstitial { } // Preload next
                            }
                            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                loadInterstitial { }
                            }
                        }
                        onLoaded(interstitialAd)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Retry logic here
                    }
                }
            )
        }
    }

    fun showInterstitial(activity: android.app.Activity, onDismiss: () -> Unit) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismiss()
                loadInterstitial { }
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                onDismiss()
                loadInterstitial { }
            }
        }
        interstitialAd?.show(activity)
    }

    fun isInterstitialReady(): Boolean = interstitialAd != null
}