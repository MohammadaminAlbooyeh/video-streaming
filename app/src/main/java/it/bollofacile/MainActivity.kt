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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.ui.navigation.AppNavHost
import it.bollofacile.ui.theme.Theme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    val navController = rememberNavController()
                    AppNavHost(navController)
                }
            }
        }
    }
}
