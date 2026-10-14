package it.bollofacile.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bollofacile.R
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.ui.components.BannerAd
import it.bollofacile.ui.components.ScadenzaCard
import it.bollofacile.ui.components.StatCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToVeicolo: (Long) -> Unit,
    onNavigateToAddVeicolo: () -> Unit,
    onNavigateToBolloCalc: () -> Unit,
    onNavigateToRevisione: () -> Unit,
    onNavigateToSanzioni: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scadenze by viewModel.scadenze.collectAsStateWithLifecycle()
    val veicoliCount by viewModel.veicoliCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    ThemeMenuButton(onThemeSelected = { viewModel.setTheme(it) })
                    LanguageMenuButton()
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BannerAd(adUnitId = stringResource(R.string.admob_banner_id))
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddVeicolo,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text(text = stringResource(R.string.add_vehicle)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stat_vehicles),
                    value = veicoliCount.toString(),
                    icon = { Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null) }
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stat_deadlines),
                    value = scadenze.count {
                        it.isBolloScaduto || it.isRevisioneScaduta || it.giorniBollo <= 30 || it.giorniRevisione <= 30
                    }.toString(),
                    icon = { Icon(imageVector = Icons.Default.Event, contentDescription = null) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onNavigateToBolloCalc, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.calc_bollo), maxLines = 2)
                }
                OutlinedButton(onClick = onNavigateToRevisione, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.revisione))
                }
                OutlinedButton(onClick = onNavigateToSanzioni, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.sanzioni))
                }
            }

            Text(
                text = stringResource(R.string.upcoming_deadlines),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (scadenze.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_vehicles),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.add_first_vehicle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(scadenze) { scadenza ->
                        ScadenzaCard(
                            veicolo = scadenza.veicolo,
                            bolloResult = scadenza.bolloResult,
                            revisioneResult = scadenza.revisioneResult,
                            onClick = { onNavigateToVeicolo(scadenza.veicolo.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeMenuButton(onThemeSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Default.BrightnessMedium,
            contentDescription = stringResource(R.string.theme)
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.theme_system)) },
            onClick = { onThemeSelected(SettingsStore.THEME_SYSTEM); expanded = false }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.theme_light)) },
            onClick = { onThemeSelected(SettingsStore.THEME_LIGHT); expanded = false }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.theme_dark)) },
            onClick = { onThemeSelected(SettingsStore.THEME_DARK); expanded = false }
        )
    }
}

@Composable
private fun LanguageMenuButton() {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = stringResource(R.string.language)
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Italiano") },
            onClick = {
                expanded = false
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("it"))
            }
        )
        DropdownMenuItem(
            text = { Text("English") },
            onClick = {
                expanded = false
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            }
        )
    }
}
