package it.bollofacile.ui.veicolo

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
import it.bollofacile.data.prefs.SettingsStore
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
fun VeicoloFormScreen(
    viewModel: VeicoloViewModel = hiltViewModel(),
    veiculoId: Long?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var marca by mutableStateOf("")
    var modello by mutableStateOf("")
    var targa by mutableStateOf("")
    var kw by mutableStateOf("")
    var cv by mutableStateOf("")
    var classeEuro by mutableStateOf(ClasseEuro.EURO_6)
    var alimentazione by mutableStateOf(Alimentazione.BENZINA)
    var dataImmatricolazione by mutableStateOf(kotlinx.datetime.LocalDate.now())
    var regione by mutableStateOf(Regione.LOMBARDIA)
    var isStorico by mutableStateOf(false)
    var note by mutableStateOf("")
    var showPreview by mutableStateOf(false)
    
    val isEditing = veiculoId != null
    
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
                        text = if (isEditing) "Modifica veicolo" else "Nuovo veicolo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.material3.IconButton(onClick = onCancel) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }
                
                // Form in scrollable column
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
                            keyboardActions = KeyboardActions(onNext = { /* focus next */ })
                        )
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = modello,
                            onValueChange = { modello = it },
                            label = { Text(text = "Modello") },
                            placeholder = { Text(text = "es. 500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = targa,
                            onValueChange = { targa = it },
                            label = { Text(text = "Targa (opzionale)") },
                            placeholder = { Text(text = "AB123CD") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                    }
                    
                    Panel(title = "Caratteristiche tecniche") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                value = kw,
                                onValueChange = { kw = it.filter { it.isDigit() } },
                                label = { Text(text = "kW") },
                                placeholder = { Text(text = "es. 75") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                keyboardActions = KeyboardActions(onNext = { /* focus cv */ })
                            )
                            TextField(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                value = cv,
                                onValueChange = { cv = it.filter { it.isDigit() } },
                                label = { Text(text = "CV") },
                                placeholder = { Text(text = "es. 102") },
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
                    
                    Panel(title = "Altro") {
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
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = note,
                            onValueChange = { note = it },
                            label = { Text(text = "Note") },
                            placeholder = { Text(text = "Note opzionali") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                    
                    // Preview
                    if (showPreview) {
                        val kwInt = kw.toIntOrNull() ?: 0
                        val cvInt = cv.toIntOrNull() ?: 0
                        val veicolo = Veicolo(
                            targa = targa.ifBlank { null },
                            marca = marca,
                            modello = modello,
                            kw = kwInt,
                            cv = cvInt,
                            classeEuro = classeEuro,
                            alimentazione = alimentazione,
                            dataImmatricolazione = dataImmatricolazione,
                            regione = regione,
                            isStorico = isStorico,
                            note = note
                        )
                        
                        val bolloResult = viewModel.calcolaBollo(veicolo)
                        val revisioneResult = viewModel.calcolaRevisione(veicolo)
                        
                        Panel(title = "Anteprima calcolo") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Bollo: €${"%.2f".format(bolloResult.totale)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (bolloResult.hasSuperbollo) {
                                    Text(text = "Include superbollo: €${"%.2f".format(bolloResult.superbollo)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = "Scadenza bollo: ${bolloResult.scadenza.format(it.bollofacile.util.Format.DATE_FORMATTER)}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Prossima revisione: ${revisioneResult.prossimaScadenza.format(it.bollofacile.util.Format.DATE_FORMATTER)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                
                // Bottom buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { showPreview = !showPreview }, modifier = Modifier.weight(1f)) {
                        Text(text = if (showPreview) "Nascondi anteprima" : "Mostra anteprima")
                    }
                    Button(
                        onClick = {
                            val kwInt = kw.toIntOrNull() ?: 0
                            val cvInt = cv.toIntOrNull() ?: 0
                            val veicolo = Veicolo(
                                id = veiculoId ?: 0,
                                targa = targa.ifBlank { null },
                                marca = marca,
                                modello = modello,
                                kw = kwInt,
                                cv = cvInt,
                                classeEuro = classeEuro,
                                alimentazione = alimentazione,
                                dataImmatricolazione = dataImmatricolazione,
                                regione = regione,
                                isStorico = isStorico,
                                note = note
                            )
                            if (isEditing) {
                                viewModel.updateVeicolo(veicolo)
                            } else {
                                viewModel.insertVeicolo(veicolo)
                            }
                            onSave()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (isEditing) "Salva" else "Aggiungi")
                    }
                }
            }
        }
    }
}

class VeicoloViewModel @Inject constructor(
    private val veicoloRepository: it.bollofacile.data.repository.VeicoloRepository,
    private val calcolaBolloUseCase: CalcolaBolloUseCase,
    private val calcolaRevisioneUseCase: CalcolaRevisioneUseCase,
    private val settingsStore: SettingsStore
) : ViewModel() {
    
    fun insertVeicolo(veicolo: Veicolo) {
        viewModelScope.launch {
            veicoloRepository.insert(veicolo)
        }
    }
    
    fun updateVeicolo(veicolo: Veicolo) {
        viewModelScope.launch {
            veicoloRepository.update(veicolo)
        }
    }
    
    fun deleteVeiculo(id: Long) {
        viewModelScope.launch {
            veicoloRepository.delete(id)
        }
    }
    
    fun calcolaBollo(veicolo: Veicolo) = calcolaBolloUseCase(veicolo)
    fun calcolaRevisione(veicolo: Veicolo) = calcolaRevisioneUseCase(veicolo)
}