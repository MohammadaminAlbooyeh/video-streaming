package it.bollofacile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.bollofacile.ui.bollo.BolloCalcScreen
import it.bollofacile.ui.bollo.BolloViewModel
import it.bollofacile.ui.home.HomeScreen
import it.bollofacile.ui.home.HomeViewModel
import it.bollofacile.ui.navigation.AppNavHost
import it.bollofacile.ui.pro.ProScreen
import it.bollofacile.ui.revisione.RevisioneScreen
import it.bollofacile.ui.sanzioni.SanzioniScreen
import it.bollofacile.ui.settings.SettingsScreen
import it.bollofacile.ui.theme.Theme
import it.bollofacile.ui.veicolo.VeicoloDetailScreen
import it.bollofacile.ui.veicolo.VeicoloFormScreen
import it.bollofacile.ui.veicolo.VeicoloViewModel
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by hiltViewModel()
    private val veicoloViewModel: VeicoloViewModel by hiltViewModel()
    private val bolloViewModel: BolloViewModel by hiltViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(navController)
                }
            }
        }
    }
}