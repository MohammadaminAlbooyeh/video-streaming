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
import androidx.compose.material.icons.filled.Event
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.bollofacile.R
import it.bollofacile.rules.model.Alimentazione
import it.bollofacile.rules.model.BolloResult
import it.bollofacile.rules.model.ClasseEuro
import it.bollofacile.rules.model.Regione
import it.bollofacile.rules.model.RevisioneResult
import it.bollofacile.rules.model.Veicolo
import it.bollofacile.ui.components.CalculationDisclaimer
import it.bollofacile.ui.components.DatePickerField
import it.bollofacile.ui.components.DropdownField
import it.bollofacile.ui.components.Panel
import it.bollofacile.util.Format
import it.bollofacile.util.addDeadlineToCalendar
import it.bollofacile.util.ifNotBlank
import java.time.LocalDate

/** Mutable UI state for one vehicle input form (used twice in compare mode). */
private class VehicleFormState {
    var marca by mutableStateOf("")
    var modello by mutableStateOf("")
    var kw by mutableStateOf("")
    var cv by mutableStateOf("")
    var classeEuro by mutableStateOf(ClasseEuro.EURO_6)
    var alimentazione by mutableStateOf(Alimentazione.BENZINA)
    var dataImmatricolazione by mutableStateOf(LocalDate.now())
    var regione by mutableStateOf(Regione.LOMBARDIA)
    var isStorico by mutableStateOf(false)

    fun toVeicolo() = Veicolo(
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

    fun displayName(fallback: String): String =
        listOf(marca, modello).filter { it.isNotBlank() }.joinToString(" ").ifBlank { fallback }
}

@Composable
fun BolloCalcScreen(
    viewModel: BolloViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val formA = remember { VehicleFormState() }
    val formB = remember { VehicleFormState() }
    var compareMode by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var bolloA by remember { mutableStateOf<BolloResult?>(null) }
    var revisioneA by remember { mutableStateOf<RevisioneResult?>(null) }
    var bolloB by remember { mutableStateOf<BolloResult?>(null) }

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
            Panel(title = stringResource(R.string.options)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionSwitch(
                        label = stringResource(R.string.compare_vehicles),
                        checked = compareMode,
                        onCheckedChange = {
                            compareMode = it
                            showResult = false
                        }
                    )
                }
            }

            Panel(
                title = if (compareMode) stringResource(R.string.vehicle_a)
                else stringResource(R.string.vehicle_data)
            ) {
                VehicleFormFields(formA)
            }

            if (compareMode) {
                Panel(title = stringResource(R.string.vehicle_b)) {
                    VehicleFormFields(formB)
                }
            }

            CalculationDisclaimer()

            Button(
                onClick = {
                    bolloA = viewModel.calcolaBollo(formA.toVeicolo())
                    revisioneA = viewModel.calcolaRevisione(formA.toVeicolo())
                    bolloB = if (compareMode) viewModel.calcolaBollo(formB.toVeicolo()) else null
                    showResult = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.calculate), fontWeight = FontWeight.Bold)
            }

            if (showResult && compareMode && bolloA != null && bolloB != null) {
                ComparisonPanel(
                    nameA = formA.displayName(stringResource(R.string.vehicle_a)),
                    nameB = formB.displayName(stringResource(R.string.vehicle_b)),
                    resultA = bolloA!!,
                    resultB = bolloB!!
                )
            }

            if (showResult && !compareMode && bolloA != null && revisioneA != null) {
                SingleResultPanel(
                    form = formA,
                    bollo = bolloA!!,
                    revisione = revisioneA!!
                )
            }
        }
    }
}

@Composable
private fun VehicleFormFields(state: VehicleFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.marca,
            onValueChange = { state.marca = it },
            label = { Text(text = stringResource(R.string.brand)) },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.modello,
            onValueChange = { state.modello = it },
            label = { Text(text = stringResource(R.string.model)) },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.kw,
                onValueChange = { v -> state.kw = v.filter { it.isDigit() } },
                label = { Text(text = stringResource(R.string.kw)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.cv,
                onValueChange = { v -> state.cv = v.filter { it.isDigit() } },
                label = { Text(text = stringResource(R.string.cv)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        DropdownField(
            label = stringResource(R.string.euro_class),
            options = ClasseEuro.values().map { it.label },
            selectedOption = state.classeEuro.label,
            onOptionSelected = { label -> state.classeEuro = ClasseEuro.values().first { it.label == label } }
        )

        DropdownField(
            label = stringResource(R.string.fuel_type),
            options = Alimentazione.values().map { it.label },
            selectedOption = state.alimentazione.label,
            onOptionSelected = { label -> state.alimentazione = Alimentazione.values().first { it.label == label } }
        )

        DatePickerField(
            label = stringResource(R.string.registration_date),
            selectedDate = state.dataImmatricolazione,
            onDateSelected = { state.dataImmatricolazione = it }
        )

        DropdownField(
            label = stringResource(R.string.region),
            options = Regione.values().map { it.nome },
            selectedOption = state.regione.nome,
            onOptionSelected = { nome -> state.regione = Regione.values().first { it.nome == nome } }
        )

        OptionSwitch(
            label = stringResource(R.string.historic_vehicle),
            checked = state.isStorico,
            onCheckedChange = { state.isStorico = it }
        )
    }
}

@Composable
private fun OptionSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ComparisonPanel(
    nameA: String,
    nameB: String,
    resultA: BolloResult,
    resultB: BolloResult
) {
    Panel(title = stringResource(R.string.comparison)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComparisonCard(
                    modifier = Modifier.weight(1f),
                    name = nameA,
                    result = resultA,
                    isCheaper = resultA.totale < resultB.totale
                )
                ComparisonCard(
                    modifier = Modifier.weight(1f),
                    name = nameB,
                    result = resultB,
                    isCheaper = resultB.totale < resultA.totale
                )
            }

            val diff = kotlin.math.abs(resultA.totale - resultB.totale)
            val verdict = when {
                diff < 0.005 -> stringResource(R.string.same_cost)
                resultA.totale < resultB.totale ->
                    stringResource(R.string.cheaper_fmt, nameA, "€${"%.2f".format(diff)}")
                else ->
                    stringResource(R.string.cheaper_fmt, nameB, "€${"%.2f".format(diff)}")
            }
            Text(
                text = verdict,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ComparisonCard(
    modifier: Modifier = Modifier,
    name: String,
    result: BolloResult,
    isCheaper: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isCheaper) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "€${"%.2f".format(result.totale)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCheaper) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.bollo_per_year),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (result.hasSuperbollo) {
                Text(
                    text = "${stringResource(R.string.superbollo)}: €${"%.2f".format(result.superbollo)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SingleResultPanel(
    form: VehicleFormState,
    bollo: BolloResult,
    revisione: RevisioneResult
) {
    val context = LocalContext.current
    val vehicleName = form.displayName(fallback = "").takeIf { it.isNotBlank() }

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
                    AddToCalendarButton(
                        onClick = {
                            addDeadlineToCalendar(
                                context = context,
                                title = vehicleName?.let { context.getString(R.string.calendar_event_bollo, it) }
                                    ?: context.getString(R.string.calendar_event_bollo_generic),
                                description = "€${"%.2f".format(bollo.totale)}",
                                date = bollo.scadenza
                            )
                        }
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
                        "${stringResource(R.string.fuel_reduction)} (${form.alimentazione.label})",
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
                    AddToCalendarButton(
                        onClick = {
                            addDeadlineToCalendar(
                                context = context,
                                title = vehicleName?.let { context.getString(R.string.calendar_event_revisione, it) }
                                    ?: context.getString(R.string.calendar_event_revisione_generic),
                                description = "",
                                date = revisione.prossimaScadenza
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddToCalendarButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(text = stringResource(R.string.add_to_calendar))
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
