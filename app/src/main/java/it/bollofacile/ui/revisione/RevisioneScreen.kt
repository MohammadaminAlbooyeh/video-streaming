package it.bollofacile.ui.revisione

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
import it.bollofacile.domain.CalcolaRevisioneUseCase
import it.bollofacile.rules.model.Alimentazione
import it.bollofacile.rules.model.ClasseEuro
import it.bollofacile.rules.model.Regione
import it.bollofacile.rules.model.Veicolo
import it.bollofacile.ui.components.DatePickerField
import it.bollofacile.ui.components.DropdownField
import it.bollofacile.ui.components.Panel
import it.bollofacile.ui.theme.Theme
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@Composable
fun RevisioneScreen(
    onNavigateBack: () -> Unit
) {
    var marca by mutableStateOf("")
    var modello by mutableStateOf("")
    var dataImmatricolazione by mutableStateOf(LocalDate.now())
    var ultimaRevisione by mutableStateOf<LocalDate?>(null)
    var classeEuro by mutableStateOf(ClasseEuro.EURO_6)
    var alimentazione by mutableStateOf(Alimentazione.BENZINA)
    var regione by mutableStateOf(Regione.LOMBARDIA)
    var isStorico by mutableStateOf(false)
    var showResult by mutableStateOf(false)
    var revisioneResult by mutableStateOf<it.bollofacile.rules.model.RevisioneResult?>(null)
    
    val calcolaRevisioneUseCase = CalcolaRevisioneUseCase()
    
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
                    Text(text = "Calcolo Revisione", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
                    Panel(title = "Dati veicolo") {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = marca,
                            onValueChange = { marca = it },
                            label = { Text(text = "Marca") },
                            placeholder = { Text(text = "es. Fiat") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = modello,
                            onValueChange = { modello = it },
                            label = { Text(text = "Modello") },
                            placeholder = { Text(text = "es. 500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        
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
                        
                        DatePickerField(
                            label = "Ultima revisione (opzionale)",
                            selectedDate = ultimaRevisione ?: LocalDate.now(),
                            onDateSelected = { ultimaRevisione = it },
                            placeholder = "Non effettuata"
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
                        Text(text = "Calcola", fontWeight = FontWeight.Bold)
                    }
                }
                
                if (showResult && revisioneResult != null) {
                    Panel(title = "Risultato") {
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
                                    Text(text = "Prossima scadenza", style = MaterialTheme.typography.titleMedium)
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