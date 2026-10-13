package it.bollofacile.ui.bollo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.bollofacile.R
import it.bollofacile.rules.model.Alimentazione
import it.bollofacile.rules.model.ClasseEuro
import it.bollofacile.rules.model.Regione
import it.bollofacile.rules.model.Veicolo
import it.bollofacile.ui.components.DatePickerField
import it.bollofacile.ui.components.DropdownField
import it.bollofacile.ui.components.Panel
import it.bollofacile.util.Format
import it.bollofacile.util.ifNotBlank

@Composable
fun BolloCalcScreen(
    viewModel: BolloViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var marca by remember { mutableStateOf("") }
    var modello by remember { mutableStateOf("") }
    var kw by remember { mutableStateOf("") }
    var cv by remember { mutableStateOf("") }
    var classeEuro by remember { mutableStateOf(ClasseEuro.EURO_6) }
    var alimentazione by remember { mutableStateOf(Alimentazione.BENZINA) }
    var dataImmatricolazione by remember { mutableStateOf(java.time.LocalDate.now()) }
    var regione by remember { mutableStateOf(Regione.LOMBARDIA) }
    var isStorico by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var bolloResult by remember { mutableStateOf<it.bollofacile.rules.model.BolloResult?>(null) }
    var revisioneResult by remember { mutableStateOf<it.bollofacile.rules.model.RevisioneResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.quick_calc)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
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
            Panel(title = stringResource(R.string.vehicle_data)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = marca,
                        onValueChange = { marca = it },
                        label = { Text(text = stringResource(R.string.brand)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = modello,
                        onValueChange = { modello = it },
                        label = { Text(text = stringResource(R.string.model)) },
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = kw,
                            onValueChange = { v -> kw = v.filter { it.isDigit() } },
                            label = { Text(text = stringResource(R.string.kw)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = cv,
                            onValueChange = { v -> cv = v.filter { it.isDigit() } },
                            label = { Text(text = stringResource(R.string.cv)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    DropdownField(
                        label = stringResource(R.string.euro_class),
                        options = ClasseEuro.values().map { it.label },
                        selectedOption = classeEuro.label,
                        onOptionSelected = { label -> classeEuro = ClasseEuro.values().first { it.label == label } }
                    )

                    DropdownField(
                        label = stringResource(R.string.fuel_type),
                        options = Alimentazione.values().map { it.label },
                        selectedOption = alimentazione.label,
                        onOptionSelected = { label -> alimentazione = Alimentazione.values().first { it.label == label } }
                    )

                    DatePickerField(
                        label = stringResource(R.string.registration_date),
                        selectedDate = dataImmatricolazione,
                        onDateSelected = { dataImmatricolazione = it }
                    )

                    DropdownField(
                        label = stringResource(R.string.region),
                        options = Regione.values().map { it.nome },
                        selectedOption = regione.nome,
                        onOptionSelected = { nome -> regione = Regione.values().first { it.nome == nome } }
                    )
                }
            }

            Panel(title = stringResource(R.string.options)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.historic_vehicle))
                    Switch(
                        checked = isStorico,
                        onCheckedChange = { isStorico = it }
                    )
                }
            }

            Button(
                onClick = {
                    val veicolo = Veicolo(
                        marca = marca,
                        modello = modello,
                        kw = kw.toIntOrNull() ?: 0,
                        cv = cv.toIntOrNull() ?: 0,
                        classeEuro = classeEuro,
                        alimentazione = alimentazione,
                        dataImmatricolazione = dataImmatricolazione,
                        regione = regione,
                        isStorico = isStorico
                    )
                    bolloResult = viewModel.calcolaBollo(veicolo)
                    revisioneResult = viewModel.calcolaRevisione(veicolo)
                    showResult = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.calculate), fontWeight = FontWeight.Bold)
            }

            if (showResult && bolloResult != null && revisioneResult != null) {
                val bollo = bolloResult!!
                val revisione = revisioneResult!!
                Panel(title = stringResource(R.string.result)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (bollo.totale == 0.0)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = stringResource(R.string.total), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "€${"%.2f".format(bollo.totale)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bollo.totale == 0.0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Text(
                                    text = "${stringResource(R.string.due_date)}: ${bollo.scadenza.format(Format.DATE_FORMATTER)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow(stringResource(R.string.base_bollo), "€${"%.2f".format(bollo.bolloBase)}")
                            if (bollo.hasSuperbollo) {
                                DetailRow(stringResource(R.string.superbollo), "€${"%.2f".format(bollo.superbollo)}", true)
                            }
                            if (bollo.riduzioneAlimentazione > 0) {
                                DetailRow(
                                    "${stringResource(R.string.fuel_reduction)} (${alimentazione.label})",
                                    "-€${"%.2f".format(bollo.riduzioneAlimentazione)}",
                                    false,
                                    true
                                )
                            }
                            if (bollo.riduzioneRegionale > 0) {
                                DetailRow(
                                    stringResource(R.string.euro_regional_reduction),
                                    "-€${"%.2f".format(bollo.riduzioneRegionale)}",
                                    false,
                                    true
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = stringResource(R.string.next_revision_date), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = revisione.prossimaScadenza.format(Format.DATE_FORMATTER),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.in_years_fmt, revisione.anniProssima),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                revisione.note.ifNotBlank {
                                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isError: Boolean = false, isPositive: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                isPositive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
