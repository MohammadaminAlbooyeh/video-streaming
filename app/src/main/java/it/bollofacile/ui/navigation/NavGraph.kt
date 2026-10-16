package it.bollofacile.ui.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import it.bollofacile.ads.AdManager
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import it.bollofacile.ui.bollo.BolloCalcScreen
import it.bollofacile.ui.bollo.BolloViewModel
import it.bollofacile.ui.home.HomeScreen
import it.bollofacile.ui.home.HomeViewModel
import it.bollofacile.ui.revisione.RevisioneScreen
import it.bollofacile.ui.sanzioni.SanzioniScreen
import it.bollofacile.ui.settings.SettingsScreen
import it.bollofacile.ui.veicolo.VeicoloDetailScreen
import it.bollofacile.ui.veicolo.VeicoloFormScreen
import it.bollofacile.ui.veicolo.VeicoloViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = "home",
    adManager: AdManager? = null
) {
    val activity = LocalContext.current as? Activity

    fun completeAction(navigateBack: () -> Unit) {
        if (adManager != null && activity != null) {
            adManager.onActionCompleted(activity) { navigateBack() }
        } else {
            navigateBack()
        }
    }

    NavHost(navController, startDestination) {
        composable("home") {
            HomeScreen(
                viewModel = hiltViewModel<HomeViewModel>(),
                onNavigateToVeicolo = { id ->
                    navController.navigate("veicolo/$id")
                },
                onNavigateToAddVeicolo = {
                    navController.navigate("veicolo/new")
                },
                onNavigateToBolloCalc = {
                    navController.navigate("bollo/calc")
                },
                onNavigateToRevisione = {
                    navController.navigate("revisione")
                },
                onNavigateToSanzioni = {
                    navController.navigate("sanzioni")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable("veicolo/new") {
            VeicoloFormScreen(
                viewModel = hiltViewModel<VeicoloViewModel>(),
                veiculoId = null,
                onSave = { completeAction { navController.popBackStack() } },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = "veicolo/{veicoloId}",
            arguments = listOf(navArgument("veicoloId") { type = NavType.LongType })
        ) { backStackEntry ->
            val veicoloId = backStackEntry.arguments?.getLong("veicoloId") ?: 0L
            VeicoloDetailScreen(
                viewModel = hiltViewModel<VeicoloViewModel>(),
                veiculoId = veicoloId,
                onNavigateToEdit = {
                    navController.navigate("veicolo/edit/$veicoloId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "veicolo/edit/{veicoloId}",
            arguments = listOf(navArgument("veicoloId") { type = NavType.LongType })
        ) { backStackEntry ->
            val veicoloId = backStackEntry.arguments?.getLong("veicoloId") ?: 0L
            VeicoloFormScreen(
                viewModel = hiltViewModel<VeicoloViewModel>(),
                veiculoId = veicoloId,
                onSave = { completeAction { navController.popBackStack() } },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("bollo/calc") {
            BolloCalcScreen(
                viewModel = hiltViewModel<BolloViewModel>(),
                onNavigateBack = { completeAction { navController.popBackStack() } }
            )
        }
        composable("revisione") {
            RevisioneScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("sanzioni") {
            SanzioniScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
