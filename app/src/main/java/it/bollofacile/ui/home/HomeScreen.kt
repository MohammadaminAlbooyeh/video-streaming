package it.bollofacile.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import it.bollofacile.R
import it.bollofacile.domain.ProssimeScadenzeUseCase
import it.bollofacile.domain.ScadenzaVeicolo
import it.bollofacile.ui.components.ScadenzaCard
import it.bollofacile.ui.components.StatCard
import it.bollofacile.ui.navigation.AppNavHost
import it.bollofacile.ui.theme.Theme
import it.bollofacile.util.Format
import javax.inject.Inject

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToVeicolo: (Long) -> Unit,
    onNavigateToAddVeicolo: () -> Unit,
    onNavigateToBolloCalc: () -> Unit,
    onNavigateToRevisione: () -> Unit,
    onNavigateToSanzioni: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPro: () -> Unit
) {
    val scadenze by viewModel.scadenze.collectAsStateWithLifecycle()
    val veicoliCount by viewModel.veicoliCount.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    
    Theme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    if (!isPro) {
                        androidx.compose.material3.TextButton(onClick = onNavigateToPro) {
                            Text(text = "PRO", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                
                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Veicoli",
                        value = veicoliCount.toString(),
                        icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.DirectionsCar, contentDescription = null) }
                    )
                    StatCard(
                        title = "Scadenze",
                        value = scadenze.count { it.isBolloScaduto || it.isRevisioneScaduta || it.giorniBollo <= 30 || it.giorniRevisione <= 30 }.toString(),
                        icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Event, contentDescription = null) }
                    )
                }
                
                // Quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onNavigateToBolloCalc,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Calcola Bollo")
                    }
                    androidx.compose.material3.OutlinedButton(
                        onClick = onNavigateToRevisione,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Revisione")
                    }
                    androidx.compose.material3.OutlinedButton(
                        onClick = onNavigateToSanzioni,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Sanzioni")
                    }
                }
                
                // Scadenze
                Text(
                    text = "Prossime scadenze",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
                                imageVector = androidx.compose.material.icons.Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                            Text(
                                text = "Nessun veicolo aggiunto",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tocca + per aggiungere il tuo primo veicolo",
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
                                onClick = { onNavigateToVeicolo(scadenza.veiculo.id) }
                            )
                        }
                    }
                }
            }
            
            // FAB
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddVeicolo,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = null) },
                text = { Text(text = "Aggiungi veicolo") }
            )
        }
    }
}

class HomeViewModel @Inject constructor(
    private val prossimeScadenzeUseCase: ProssimeScadenzeUseCase,
    private val veicoloRepository: it.bollofacile.data.repository.VeicoloRepository,
    private val settingsStore: it.bollofacile.data.prefs.SettingsStore
) : ViewModel() {
    
    val scadenze = prossimeScadenzeUseCase().distinctUntilChanged()
    
    val veicoliCount = veicoloRepository.getAll()
        .map { it.size }
        .distinctUntilChanged()
    
    val isPro = settingsStore.isProEnabled.distinctUntilChanged()
}