package it.bollofacile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.bollofacile.ui.bollo.BolloCalcScreen
import it.bollofacile.ui.bollo.BolloViewModel
import it.bollofacile.ui.home.HomeScreen
import it.bollofacile.ui.home.HomeViewModel
import it.bollofacile.ui.pro.ProScreen
import it.bollofacile.ui.revisione.RevisioneScreen
import it.bollofacile.ui.sanzioni.SanzioniScreen
import it.bollofacile.ui.settings.SettingsScreen
import it.bollofacile.ui.veicolo.VeicoloDetailScreen
import it.bollofacile.ui.veicolo.VeicoloFormScreen
import it.bollofacile.ui.veicolo.VeicoloViewModel

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = "home") {
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
                },
                onNavigateToPro = {
                    navController.navigate("pro")
                }
            )
        }
        composable(
            route = "veiculo/{veiculoId}",
            arguments = listOf(navArgument("veiculoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val veiculoId = backStackEntry.getLong() ?: 0L
            VeiculoDetailScreen(
                viewModel = hiltViewModel<VeiculoViewModel>(),
                veiculoId = veiculoId,
                onNavigateToEdit = {
                    navController.navigate("veiculo/edit/$veiculoId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("veiculo/new") {
            VeiculoFormScreen(
                viewModel = hiltViewModel<VeiculoViewModel>(),
                veiculoId = null,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = "veiculo/edit/{veiculoId}",
            arguments = listOf(navArgument("veiculoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val veiculoId = backStackEntry.getLong() ?: 0L
            VeiculoFormScreen(
                viewModel = hiltViewModel<VeiculoViewModel>(),
                veiculoId = veiculoId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("bollo/calc") {
            BolloCalcScreen(
                viewModel = hiltViewModel<BolloViewModel>(),
                onNavigateBack = { navController.popBackStack() }
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
        composable("pro") {
            ProScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun RememberNavController(): NavHostController = rememberNavController()