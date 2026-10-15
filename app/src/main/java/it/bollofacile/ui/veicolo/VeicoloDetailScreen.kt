package it.bollofacile.ui.veicolo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bollofacile.R
import it.bollofacile.ui.components.Panel
import it.bollofacile.ui.components.StatCard
import it.bollofacile.util.Format
import it.bollofacile.util.ifNotBlank

@Composable
fun VeicoloDetailScreen(
    viewModel: VeicoloViewModel = hiltViewModel(),
    veiculoId: Long,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val veicoloState by viewModel.getVeicolo(veiculoId).collectAsStateWithLifecycle(initialValue = null)
    val veicolo = veicoloState
    if (veicolo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.error_vehicle_not_found))
        }
        return
    }

    val bolloResult = viewModel.calcolaBollo(veicolo)
    val revisioneResult = viewModel.calcolaRevisione(veicolo)
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${veicolo.marca} ${veicolo.modello}",
                            fontWeight = FontWeight.Bold
                        )
                        veicolo.targa?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToEdit,
                icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                text = { Text(text = stringResource(R.string.edit)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.bollo),
                    value = "€${"%.2f".format(bolloResult.totale)}",
                    subtitle = "${stringResource(R.string.due_date)}: ${bolloResult.scadenza.format(Format.DATE_FORMATTER)}",
                    color = if (bolloResult.totale == 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.revisione),
                    value = revisioneResult.prossimaScadenza.format(Format.DATE_FORMATTER),
                    subtitle = stringResource(R.string.in_years_fmt, revisioneResult.anniProssima),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Panel(title = stringResource(R.string.bollo_details)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(stringResource(R.string.base_bollo), "€${"%.2f".format(bolloResult.bolloBase)}")
                    if (bolloResult.hasSuperbollo) {
                        DetailRow(
                            "${stringResource(R.string.superbollo)} (${veicolo.kw - 185} kW × €20)",
                            "€${"%.2f".format(bolloResult.superbollo)}"
                        )
                    }
                    if (bolloResult.riduzioneAlimentazione > 0) {
                        DetailRow(
                            "${stringResource(R.string.fuel_reduction)} (${veicolo.alimentazione.label})",
                            "-€${"%.2f".format(bolloResult.riduzioneAlimentazione)}"
                        )
                    }
                    if (bolloResult.riduzioneRegionale > 0) {
                        DetailRow(
                            stringResource(R.string.euro_regional_reduction),
                            "-€${"%.2f".format(bolloResult.riduzioneRegionale)}"
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringResource(R.string.total), fontWeight = FontWeight.Bold)
                        Text(text = "€${"%.2f".format(bolloResult.totale)}", fontWeight = FontWeight.Bold)
                    }
                    androidx.compose.material3.TextButton(onClick = {
                        it.bollofacile.util.addDeadlineToCalendar(
                            context = context,
                            title = context.getString(R.string.calendar_event_bollo, "${veicolo.marca} ${veicolo.modello}"),
                            description = "€${"%.2f".format(bolloResult.totale)}",
                            date = bolloResult.scadenza
                        )
                    }) {
                        Text(text = stringResource(R.string.add_to_calendar))
                    }
                }
            }

            Panel(title = stringResource(R.string.revisione_details)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(
                        stringResource(R.string.next_revision),
                        revisioneResult.prossimaScadenza.format(Format.DATE_FORMATTER)
                    )
                    DetailRow(
                        stringResource(R.string.in_years),
                        stringResource(R.string.in_years_fmt, revisioneResult.anniProssima)
                    )
                    revisioneResult.note.ifNotBlank { note ->
                        Text(text = note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    androidx.compose.material3.TextButton(onClick = {
                        it.bollofacile.util.addDeadlineToCalendar(
                            context = context,
                            title = context.getString(R.string.calendar_event_revisione, "${veicolo.marca} ${veicolo.modello}"),
                            description = "",
                            date = revisioneResult.prossimaScadenza
                        )
                    }) {
                        Text(text = stringResource(R.string.add_to_calendar))
                    }
                }
            }

            Panel(title = stringResource(R.string.technical_details)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailRow(stringResource(R.string.power), "${veicolo.kw} kW / ${veicolo.cv} CV")
                    DetailRow(stringResource(R.string.euro_class), veicolo.classeEuro.label)
                    DetailRow(stringResource(R.string.fuel_type), veicolo.alimentazione.label)
                    DetailRow(stringResource(R.string.registration_date), veicolo.dataImmatricolazione.format(Format.DATE_FORMATTER))
                    DetailRow(stringResource(R.string.region), veicolo.regione.nome)
                    veicolo.targa?.let { DetailRow(stringResource(R.string.plate_label), it) }
                    if (veicolo.isStorico) DetailRow(stringResource(R.string.historic_label), stringResource(R.string.exempt))
                    veicolo.note.ifNotBlank { DetailRow(stringResource(R.string.notes), it) }
                }
            }
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
