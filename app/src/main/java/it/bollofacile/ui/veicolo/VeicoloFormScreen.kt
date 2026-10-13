package it.bollofacile.ui.veicolo

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun VeicoloFormScreen(
    viewModel: VeicoloViewModel = hiltViewModel(),
    veiculoId: Long?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var marca by remember { mutableStateOf("") }
    var modello by remember { mutableStateOf("") }
    var targa by remember { mutableStateOf("") }
    var kw by remember { mutableStateOf("") }
    var cv by remember { mutableStateOf("") }
    var classeEuro by remember { mutableStateOf(ClasseEuro.EURO_6) }
    var alimentazione by remember { mutableStateOf(Alimentazione.BENZINA) }
    var dataImmatricolazione by remember { mutableStateOf(java.time.LocalDate.now()) }
    var regione by remember { mutableStateOf(Regione.LOMBARDIA) }
    var isStorico by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var showPreview by remember { mutableStateOf(false) }
    val isEditing = veiculoId != null

    fun buildVeicolo() = Veicolo(
        id = veiculoId ?: 0,
        targa = targa.ifBlank { null },
        marca = marca,
        modello = modello,
        kw = kw.toIntOrNull() ?: 0,
        cv = cv.toIntOrNull() ?: 0,
        classeEuro = classeEuro,
        alimentazione = alimentazione,
        dataImmatricolazione = dataImmatricolazione,
        regione = regione,
        isStorico = isStorico,
        note = note
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(if (isEditing) R.string.edit_vehicle else R.string.new_vehicle))
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { showPreview = !showPreview }, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(if (showPreview) R.string.hide_preview else R.string.show_preview))
                }
                Button(
                    onClick = {
                        val veicolo = buildVeicolo()
                        if (isEditing) {
                            viewModel.updateVeicolo(veicolo)
                        } else {
                            viewModel.insertVeicolo(veicolo)
                        }
                        onSave()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.save))
                }
            }
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
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = targa,
                        onValueChange = { targa = it },
                        label = { Text(text = stringResource(R.string.plate)) },
                        singleLine = true
                    )
                }
            }

            Panel(title = stringResource(R.string.technical_details)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(text = stringResource(R.string.notes)) },
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            if (showPreview) {
                val veicolo = buildVeicolo()
                val bolloResult = viewModel.calcolaBollo(veicolo)
                val revisioneResult = viewModel.calcolaRevisione(veicolo)

                Panel(title = stringResource(R.string.preview_calc)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${stringResource(R.string.total)}: €${"%.2f".format(bolloResult.totale)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (bolloResult.hasSuperbollo) {
                            Text(
                                text = "${stringResource(R.string.superbollo)}: €${"%.2f".format(bolloResult.superbollo)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${stringResource(R.string.due_date)}: ${bolloResult.scadenza.format(Format.DATE_FORMATTER)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${stringResource(R.string.next_revision_date)}: ${revisioneResult.prossimaScadenza.format(Format.DATE_FORMATTER)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
