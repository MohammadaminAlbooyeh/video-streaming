package it.bollofacile.ui.bollo

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
import androidx.lifecycle.viewModelScope
import it.bollofacile.R
import it.bollofacile.domain.CalcolaBolloUseCase
import it.bollofacile.domain.CalcolaRevisioneUseCase
import it.bollofacile.rules.model.Alimentazione
import it.bollofacile.rules.model.ClasseEuro
import it.bollofacile.rules.model.Regione
import it.bollofacile.rules.model.Veicolo
import it.bollofacile.ui.components.DatePickerField
import it.bollofacile.ui.components.DropdownField
import it.bollofacile.ui.components.Panel
import it.bollofacile.ui.theme.Theme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun BolloCalcScreen(
    viewModel: BolloViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var marca by mutableStateOf("")
    var modello by mutableStateOf("")
    var kw by mutableStateOf("")
    var cv by mutableStateOf("")
    var classeEuro by mutableStateOf(ClasseEuro.EURO_6)
    var alimentazione by mutableStateOf(Alimentazione.BENZINA)
    var dataImmatricolazione by mutableStateOf(kotlinx.datetime.LocalDate.now())
    var regione by mutableStateOf(Regione.LOMBARDIA)
    var isStorico by mutableStateOf(false)
    var showResult by mutableStateOf(false)
    var bolloResult by mutableStateOf<it.bollofacile.rules.model.BolloResult?>(null)
    var revisioneResult by mutableStateOf<it.bollofacile.rules.model.RevisioneResult?>(null)
    
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Calcolo rapido bollo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }
                
                // Form
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(androidx.compose.foundation.layout.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Panel(title = "Dati veicolo") {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = marca,
                            onValueChange = { marca = it },
                            label = { Text(text = "Marca") },
                            placeholder = { Text(text = "es. Fiat") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            keyboardActions = KeyboardActions(onNext = { })
                        )
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = modello,
                            onValueChange = { modello = it },
                            label = { Text(text = "Modello") },
                            placeholder = { Text(text = "es. 500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                value = kw,
                                onValueChange = { kw = it.filter { it.isDigit() } },
                                label = { Text(text = "kW") },
                                placeholder = { Text(text = "75") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                keyboardActions = KeyboardActions(onNext = { })
                            )
                            TextField(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                value = cv,
                                onValueChange = { cv = it.filter { it.isDigit() } },
                                label = { Text(text = "CV") },
                                placeholder = { Text(text = "102") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        
                        DropdownField(
                            label = "Classe Euro",
                            options = ClasseEuro.values().map { it.label },
                            selectedOption = classeEuro.label,
                            onOptionSelected = { label -> classeEuro = ClasseEuro.values().first { it.label == label } }
                        )
                        
                        DropdownField(
                            label = "Alimentazione",
                            options = Alimentazione.values().map { it.label },
                            selectedOption = alimentazione.label,
                            onOptionSelected = { label -> alimentazione = Alimentazione.values().first { it.label == label } }
                        )
                        
                        DatePickerField(
                            label = "Data immatricolazione",
                            selectedDate = dataImmatricolazione,
                            onDateSelected = { dataImmatricolazione = it }
                        )
                        
                        DropdownField(
                            label = "Regione",
                            options = Regione.values().map { it.nome },
                            selectedOption = regione.nome,
                            onOptionSelected = { nome -> regione = Regione.values().first { it.nome == nome } }
                        )
                    }
                    
                    Panel(title = "Opzioni") {
                        androidx.compose.material3.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Veicolo storico (30+ anni)")
                            androidx.compose.material3.Switch(
                                checked = isStorico,
                                onCheckedChange = { isStorico = it }
                            )
                        }
                    }
                    
                    // Calculate button
                    Button(
                        onClick = {
                            val kwInt = kw.toIntOrNull() ?: 0
                            val cvInt = cv.toIntOrNull() ?: 0
                            val veicolo = Veicolo(
                                marca = marca,
                                modello = modello,
                                kw = kwInt,
                                cv = cvInt,
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
                        Text(text = "Calcola", fontWeight = FontWeight.Bold)
                    }
                }
                
                // Result
                if (showResult && bolloResult != null && revisioneResult != null) {
                    Panel(title = "Risultato") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Bollo total
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = if (bolloResult!!.totale == 0.0) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                        else MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Bollo totale", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "€${"%.2f".format(bolloResult!!.totale)}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (bolloResult!!.totale == 0.0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Text(
                                        text = "Scadenza: ${bolloResult!!.scadenza.format(it.bollofacile.util.Format.DATE_FORMATTER)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            // Detail
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailRow("Bollo base", "€${"%.2f".format(bolloResult!!.bolloBase)}")
                                if (bolloResult!!.hasSuperbollo) {
                                    DetailRow("Superbollo", "€${"%.2f".format(bolloResult!!.superbollo)}", true)
                                }
                                if (bolloResult!!.riduzioneAlimentazione > 0) {
                                    DetailRow("Riduzione ${alimentazione.label}", "-€${"%.2f".format(bolloResult!!.riduzioneAlimentazione)}", false, true)
                                }
                                if (bolloResult!!.riduzioneRegionale > 0) {
                                    DetailRow("Riduzioni Euro/Regionali", "-€${"%.2f".format(bolloResult!!.riduzioneRegionale)}", false, true)
                                }
                            }
                            
                            // Revisione
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Prossima revisione", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = revisioneResult!!.prossimaScadenza.format(it.bollofacile.util.Format.DATE_FORMATTER),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Text(text = "Tra ${revisioneResult!!.anniProssima} anni", style = MaterialTheme.typography.bodyMedium)
                                    revisioneResult!!.note.ifNotBlank {
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