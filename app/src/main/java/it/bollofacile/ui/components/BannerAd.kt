package it.bollofacile.ui.components

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Test ID
) {
    val context = LocalContext.current
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                adUnitId = adUnitId
                adSize = AdSize.BANNER
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Update if needed
        }
    )
}

@Composable
fun InterstitialAdManager(
    adUnitId: String = "ca-app-pub-3940256099942544/1033173712" // Test ID
) {
    val context = LocalContext.current
    
    var interstitialAd by androidx.compose.runtime.mutableStateOf<InterstitialAd?>(null)
    
    LaunchedEffect(Unit) {
        loadInterstitial()
    }
    
    fun loadInterstitial() {
        CoroutineScope(Dispatchers.IO).launch {
            InterstitialAd.load(
                context,
                adUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                loadInterstitial()
                            }
                            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                loadInterstitial()
                            }
                        }
                    }
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle error
                    }
                }
            )
        }
    }
    
    fun show(activity: android.app.Activity, onDismiss: () -> Unit) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismiss()
                loadInterstitial()
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                onDismiss()
                loadInterstitial()
            }
        }
        interstitialAd?.show(activity)
    }
}