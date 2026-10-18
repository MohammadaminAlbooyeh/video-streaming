package it.bollofacile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import it.bollofacile.ads.AdManager
import it.bollofacile.ads.ConsentManager
import it.bollofacile.ads.LocalAdsAllowed
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.ui.navigation.AppNavHost
import it.bollofacile.ui.theme.Theme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsStore: SettingsStore

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var consentManager: ConsentManager

    private var adsAllowed = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // GDPR/UMP consent must be resolved before any ad SDK call or ad request.
        consentManager.requestConsentAndShowFormIfRequired(this) { canRequestAds ->
            adsAllowed.value = canRequestAds
            adManager.setAdsAllowed(canRequestAds)
            if (canRequestAds) {
                MobileAds.initialize(this) {}
                if (savedInstanceState == null) {
                    adManager.onSessionStart()
                }
            }
        }

        // Default to Italian until the user picks a language explicitly
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("it"))
        }
        setContent {
            val themePref by settingsStore.theme.collectAsStateWithLifecycle(
                initialValue = SettingsStore.THEME_SYSTEM
            )
            val darkTheme = when (themePref) {
                SettingsStore.THEME_LIGHT -> false
                SettingsStore.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }
            Theme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalAdsAllowed provides adsAllowed.value) {
                        val navController = rememberNavController()
                        AppNavHost(navController, adManager = adManager)
                    }
                }
            }
        }
    }
}
