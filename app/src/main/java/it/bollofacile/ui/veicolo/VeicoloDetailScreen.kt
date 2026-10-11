package it.bollofacile.ui.veicolo

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.bollofacile.R
import it.bollofacile.domain.CalcolaBolloUseCase
import it.bollofacile.domain.CalcolaRevisioneUseCase
import it.bollofacile.domain.CalcolaSanzioneUseCase
import it.bollofacile.rules.model.Veicolo
import it.bollofacile.ui.components.Panel
import it.bollofacile.ui.components.StatCard
import it.bollofacile.ui.theme.Theme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun VeicoloDetailScreen(
    viewModel: VeicoloViewModel = hiltViewModel(),
    veiculoId: Long,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val veicolo by viewModel.getVeicolo(veiculoId).collectAsStateWithLifecycle()
    
    if (veicolo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Veicolo non trovato")
        }
        return
    }
    
    val bolloResult = viewModel.calcolaBollo(veicolo)
    val revisioneResult = viewModel.calcolaRevisione(veicolo)
    
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
                    Column {
                        Text(
                            text = "${veicolo.marca} ${veicolo.modello}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        veicolo.targa?.let {
                            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onNavigateToEdit) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Modifica")
                        }
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Chiudi")
                        }
                    }
                }
                
                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Bollo",
                        value = "€${"%.2f".format(bolloResult.totale)}",
                        subtitle = "Scadenza: ${bolloResult.scadenza.format(it.bollofacile.util.Format.DATE_FORMATTER)}",
                        color = if (bolloResult.totale == 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                    StatCard(
                        title = "Revisione",
                        value = "${revisioneResult.prossimaScadenza.format(it.bollofacile.util.Format.DATE_FORMATTER)}",
                        subtitle = "${revisioneResult.anniProssima} anni",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                // Bollo details
                Panel(title = "Dettaglio Bollo") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Bollo base")
                            Text(text = "€${"%.2f".format(bolloResult.bolloBase)}")
                        }
                        if (bolloResult.hasSuperbollo) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Superbollo (${veicolo.kw - 185} kW × €20)")
                                Text(text = "€${"%.2f".format(bolloResult.superbollo)}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (bolloResult.riduzioneAlimentazione > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Riduzione ${veicolo.alimentazione.label}")
                                Text(text = "-€${"%.2f".format(bolloResult.riduzioneAlimentazione)}", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (bolloResult.riduzioneRegionale > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Riduzioni Euro/Regionali")
                                Text(text = "-€${"%.2f".format(bolloResult.riduzioneRegionale)}", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .padding(vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Totale", fontWeight = FontWeight.Bold)
                            Text(text = "€${"%.2f".format(bolloResult.totale)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // Revisione details
                Panel(title = "Revisione") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Prossima scadenza")
                            Text(text = revisioneResult.prossimaScadenza.format(it.bollofacile.util.Format.DATE_FORMATTER), fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Tra")
                            Text(text = "${revisioneResult.anniProssima} anni")
                        }
                        revisioneResult.note.ifNotBlank { note ->
                            Text(text = note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                // Technical details
                Panel(title = "Dati tecnici") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DetailRow("Potenza", "${veicolo.kw} kW / ${veicolo.cv} CV")
                        DetailRow("Classe Euro", veicolo.classeEuro.label)
                        DetailRow("Alimentazione", veicolo.alimentazione.label)
                        DetailRow("Immatricolazione", veicolo.dataImmatricolazione.format(it.bollofacile.util.Format.DATE_FORMATTER))
                        DetailRow("Regione", veicolo.regione.nome)
                        veicolo.targa?.let { DetailRow("Targa", it) }
                        if (veicolo.isStorico) DetailRow("Storico", "Sì (esente bollo)")
                        veicolo.note.ifNotBlank { DetailRow("Note", it) }
                    }
                }
            }
            
            // FAB for edit
            ExtendedFloatingActionButton(
                onClick = onNavigateToEdit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null) },
                text = { Text(text = "Modifica") }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}