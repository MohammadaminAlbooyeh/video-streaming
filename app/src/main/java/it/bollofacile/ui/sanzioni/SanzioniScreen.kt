package it.bollofacile.ui.sanzioni

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardType
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import it.bollofacile.R
import it.bollofacile.domain.CalcolaSanzioneUseCase
import it.bollofacile.rules.model.Veicolo
import it.bollofacile.ui.components.DatePickerField
import it.bollofacile.ui.components.Panel
import it.bollofacile.ui.theme.Theme
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@Composable
fun SanzioniScreen(
    onNavigateBack: () -> Unit
) {
    var importoBollo by mutableStateOf("")
    var dataScadenza by mutableStateOf(LocalDate.now())
    var dataPagamento by mutableStateOf(LocalDate.now())
    var showResult by mutableStateOf(false)
    var sanzioneResult by mutableStateOf<it.bollofacile.rules.model.SanzioneResult?>(null)
    var ravvedimentoScadenze by mutableStateOf<List<it.bollofacile.rules.SanzioniRules.ScadenzaRavvedimento>>(emptyList())
    
    val calcolaSanzioneUseCase = CalcolaSanzioneUseCase()
    
    Theme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Calcolo Sanzioni", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }
                
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(androidx.compose.foundation.layout.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Panel(title = "Dati pagamento") {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = importoBollo,
                            onValueChange = { importoBollo = it.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.') },
                            label = { Text(text = "Importo bollo (€)") },
                            placeholder = { Text(text = "es. 150.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            keyboardActions = KeyboardActions(onNext = { })
                        )
                        
                        DatePickerField(
                            label = "Data scadenza bollo",
                            selectedDate = dataScadenza,
                            onDateSelected = { dataScadenza = it }
                        )
                        
                        DatePickerField(
                            label = "Data pagamento prevista",
                            selectedDate = dataPagamento,
                            onDateSelected = { dataPagamento = it }
                        )
                    }
                    
                    Button(
                        onClick = {
                            val importo = importoBollo.toDoubleOrNull() ?: 0.0
                            sanzioneResult = calcolaSanzioneUseCase(importo, dataScadenza, dataPagamento)
                            ravvedimentoScadenze = calcolaSanzioneUseCase.getProssimeScadenzeRavvedimento(dataScadenza)
                            showResult = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Calcola", fontWeight = FontWeight.Bold)
                    }
                }
                
                if (showResult && sanzioneResult != null) {
                    Panel(title = "Risultato") {
                        val r = sanzioneResult!!
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = if (r.giorniRitardo == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = if (r.giorniRitardo == 0) "In regola" : "Ritardo: ${r.giorniRitardo} giorni", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "€${"%.2f".format(r.totaleDaPagare)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (r.giorniRitardo == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (r.giorniRitardo > 0) {
                                    Text(text = "Fascia ravvedimento: ${r.fasciaRavvedimento}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("Importo bollo", "€${"%.2f".format(r.importoOriginale)}")
                            if (r.giorniRitardo > 0) {
                                DetailRow("Sanzione base (30%)", "€${"%.2f".format(r.sanzioneBase)}", true)
                                DetailRow("Interessi legali", "€${"%.2f".format(r.interessi)}", true)
                                DetailRow("Ravvedimento operoso", "-€${"%.2f".format(r.sanzioneBase - r.ravvedimentoOperoso)}", false, true)
                                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant).padding(vertical = 8.dp))
                                DetailRow("Totale da pagare", "€${"%.2f".format(r.totaleDaPagare)}", false, false, true)
                            }
                        }
                        
                        // Ravvedimento scadenze
                        if (ravvedimentoScadenze.isNotEmpty()) {
                            Panel(title = "Scadenze ravvedimento operoso") {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    ravvedimentoScadenze.forEach { scadenza ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = scadenza.descrizione, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                text = scadenza.dataLimite.format(it.bollofacile.util.Format.DATE_FORMATTER),
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
                isTotal -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}