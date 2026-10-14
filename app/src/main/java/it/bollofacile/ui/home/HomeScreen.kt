package it.bollofacile.ui.home

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bollofacile.R
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.ui.components.BannerAd
import it.bollofacile.ui.components.ScadenzaCard

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
    val urgentCount = scadenze.count {
        it.isBolloScaduto || it.isRevisioneScaduta || it.giorniBollo <= 30 || it.giorniRevisione <= 30
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text(text = stringResource(R.string.add_vehicle)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroHeader(veicoliCount = veicoliCount, urgentCount = urgentCount)
            }

            item {
                Text(
                    text = stringResource(R.string.quick_actions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Calculate,
                        label = stringResource(R.string.calc_bollo),
                        tint = MaterialTheme.colorScheme.primary,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onNavigateToBolloCalc
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CarRepair,
                        label = stringResource(R.string.revisione),
                        tint = MaterialTheme.colorScheme.secondary,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onNavigateToRevisione
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Gavel,
                        label = stringResource(R.string.sanzioni),
                        tint = MaterialTheme.colorScheme.tertiary,
                        container = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = onNavigateToSanzioni
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.upcoming_deadlines),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (scadenze.isEmpty()) {
                item { EmptyState() }
            } else {
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

@Composable
private fun HeroHeader(veicoliCount: Int, urgentCount: Int) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient, RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroStat(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DirectionsCar,
                    value = veicoliCount.toString(),
                    label = stringResource(R.string.stat_vehicles)
                )
                HeroStat(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Event,
                    value = urgentCount.toString(),
                    label = stringResource(R.string.stat_deadlines)
                )
            }
        }
    }
}

@Composable
private fun HeroStat(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    tint: Color,
    container: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(container, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
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
