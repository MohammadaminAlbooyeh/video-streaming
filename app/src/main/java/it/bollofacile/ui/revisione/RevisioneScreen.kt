package it.bollofacile.ui.revisione

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import it.bollofacile.R
import it.bollofacile.domain.CalcolaRevisioneUseCase
import it.bollofacile.rules.model.Alimentazione
import it.bollofacile.rules.model.ClasseEuro
import it.bollofacile.rules.model.Regione
import it.bollofacile.rules.model.Veicolo
import it.bollofacile.ui.components.CalculationDisclaimer
import it.bollofacile.ui.components.DatePickerField
import it.bollofacile.ui.components.DropdownField
import it.bollofacile.ui.components.Panel
import it.bollofacile.util.Format
import it.bollofacile.util.ifNotBlank
import java.time.LocalDate

@Composable
fun RevisioneScreen(
    onNavigateBack: () -> Unit
) {
    var marca by remember { mutableStateOf("") }
    var modello by remember { mutableStateOf("") }
    var dataImmatricolazione by remember { mutableStateOf(LocalDate.now()) }
    var ultimaRevisione by remember { mutableStateOf<LocalDate?>(null) }
    var classeEuro by remember { mutableStateOf(ClasseEuro.EURO_6) }
    var alimentazione by remember { mutableStateOf(Alimentazione.BENZINA) }
    var regione by remember { mutableStateOf(Regione.LOMBARDIA) }
    var isStorico by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var revisioneResult by remember { mutableStateOf<it.bollofacile.rules.model.RevisioneResult?>(null) }
    val calcolaRevisioneUseCase = remember { CalcolaRevisioneUseCase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.revisione_title)) },
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

                    DatePickerField(
                        label = stringResource(R.string.last_revision),
                        selectedDate = ultimaRevisione ?: LocalDate.now(),
                        onDateSelected = { ultimaRevisione = it },
                        placeholder = stringResource(R.string.not_done)
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

            CalculationDisclaimer()

            Button(
                onClick = {
                    val veicolo = Veicolo(
                        marca = marca,
                        modello = modello,
                        kw = 0,
                        cv = 0,
                        classeEuro = classeEuro,
                        alimentazione = alimentazione,
                        dataImmatricolazione = dataImmatricolazione,
                        regione = regione,
                        isStorico = isStorico
                    )
                    revisioneResult = calcolaRevisioneUseCase(veicolo, ultimaRevisione)
                    showResult = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.calculate), fontWeight = FontWeight.Bold)
            }

            if (showResult && revisioneResult != null) {
                val result = revisioneResult!!
                Panel(title = stringResource(R.string.result)) {
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
                                Text(text = stringResource(R.string.next_revision), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = result.prossimaScadenza.format(Format.DATE_FORMATTER),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = stringResource(R.string.in_years_fmt, result.anniProssima),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            result.note.ifNotBlank {
                                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
