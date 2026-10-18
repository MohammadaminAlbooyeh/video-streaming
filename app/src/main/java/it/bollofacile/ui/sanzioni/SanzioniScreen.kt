package it.bollofacile.ui.sanzioni

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.bollofacile.R
import it.bollofacile.domain.CalcolaSanzioneUseCase
import it.bollofacile.ui.components.CalculationDisclaimer
import it.bollofacile.ui.components.DatePickerField
import it.bollofacile.ui.components.Panel
import it.bollofacile.util.Format
import java.time.LocalDate

@Composable
fun SanzioniScreen(
    onNavigateBack: () -> Unit
) {
    var importoBollo by remember { mutableStateOf("") }
    var dataScadenza by remember { mutableStateOf(LocalDate.now()) }
    var dataPagamento by remember { mutableStateOf(LocalDate.now()) }
    var showResult by remember { mutableStateOf(false) }
    var sanzioneResult by remember { mutableStateOf<it.bollofacile.rules.model.SanzioneResult?>(null) }
    var ravvedimentoScadenze by remember { mutableStateOf<List<it.bollofacile.rules.SanzioniRules.ScadenzaRavvedimento>>(emptyList()) }
    val calcolaSanzioneUseCase = remember { CalcolaSanzioneUseCase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.sanzioni_title)) },
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
            Panel(title = stringResource(R.string.payment_data)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = importoBollo,
                        onValueChange = { v ->
                            importoBollo = v.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
                        },
                        label = { Text(text = stringResource(R.string.bollo_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    DatePickerField(
                        label = stringResource(R.string.due_date_bollo),
                        selectedDate = dataScadenza,
                        onDateSelected = { dataScadenza = it }
                    )

                    DatePickerField(
                        label = stringResource(R.string.payment_date),
                        selectedDate = dataPagamento,
                        onDateSelected = { dataPagamento = it }
                    )
                }
            }

            CalculationDisclaimer()

            Button(
                onClick = {
                    val importo = importoBollo.toDoubleOrNull() ?: 0.0
                    sanzioneResult = calcolaSanzioneUseCase(importo, dataScadenza, dataPagamento)
                    ravvedimentoScadenze = calcolaSanzioneUseCase.getProssimeScadenzeRavvedimento(dataScadenza)
                    showResult = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.calculate), fontWeight = FontWeight.Bold)
            }

            if (showResult && sanzioneResult != null) {
                val r = sanzioneResult!!
                Panel(title = stringResource(R.string.result)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (r.giorniRitardo == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (r.giorniRitardo == 0)
                                            stringResource(R.string.in_rule)
                                        else
                                            stringResource(R.string.delay_days_fmt, r.giorniRitardo),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "€${"%.2f".format(r.totaleDaPagare)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (r.giorniRitardo == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (r.giorniRitardo > 0) {
                                    Text(
                                        text = stringResource(R.string.ravvedimento_band_fmt, r.fasciaRavvedimento),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow(stringResource(R.string.bollo_amount), "€${"%.2f".format(r.importoOriginale)}")
                            if (r.giorniRitardo > 0) {
                                DetailRow(stringResource(R.string.base_penalty), "€${"%.2f".format(r.sanzioneBase)}", true)
                                DetailRow(stringResource(R.string.legal_interest), "€${"%.2f".format(r.interessi)}", true)
                                DetailRow(stringResource(R.string.ravvedimento), "-€${"%.2f".format(r.sanzioneBase - r.ravvedimentoOperoso)}", false, true)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                DetailRow(stringResource(R.string.total_to_pay), "€${"%.2f".format(r.totaleDaPagare)}", false, false, true)
                            }
                        }

                        if (ravvedimentoScadenze.isNotEmpty()) {
                            Panel(title = stringResource(R.string.penalty_deadlines)) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    ravvedimentoScadenze.forEach { scadenza ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = scadenza.descrizione, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                text = scadenza.dataLimite.format(Format.DATE_FORMATTER),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
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
private fun DetailRow(
    label: String,
    value: String,
    isError: Boolean = false,
    isPositive: Boolean = false,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                isPositive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
