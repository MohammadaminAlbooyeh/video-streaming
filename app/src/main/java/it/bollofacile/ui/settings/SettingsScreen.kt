package it.bollofacile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import it.bollofacile.R
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.rules.model.Regione
import it.bollofacile.ui.components.DropdownField
import it.bollofacile.ui.components.Panel
import it.bollofacile.ui.theme.Theme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val settingsStore = androidx.hilt.navigation.compose.hiltViewModel<SettingsViewModel>().settingsStore
    val theme by settingsStore.theme.collectAsStateWithLifecycle()
    val regioneDefault by settingsStore.regioneDefault.collectAsStateWithLifecycle()
    val reminderDays by settingsStore.reminderDays.collectAsStateWithLifecycle()
    val notificationsEnabled by settingsStore.notificationsEnabled.collectAsStateWithLifecycle()
    val isPro by settingsStore.isProEnabled.collectAsStateWithLifecycle()
    
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
                    Text(text = "Impostazioni", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }
                
                // Tema
                Panel(title = "Aspetto") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Tema")
                                Text(text = "Scegli l'aspetto dell'app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            androidx.compose.material3.MenuButton(onClick = { /* show dropdown */ }) {
                                androidx.compose.material3.Text(text = when (theme) {
                                    0 -> "Sistema"
                                    1 -> "Chiaro"
                                    2 -> "Scuro"
                                    else -> "Sistema"
                                })
                            }
                        }
                        
                        // Simplified theme selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOption(text = "Sistema", selected = theme == 0, onClick = { settingsStore.setTheme(0) })
                            ThemeOption(text = "Chiaro", selected = theme == 1, onClick = { settingsStore.setTheme(1) })
                            ThemeOption(text = "Scuro", selected = theme == 2, onClick = { settingsStore.setTheme(2) })
                        }
                    }
                }
                
                // Regione default
                Panel(title = "Regione di default") {
                    DropdownField(
                        label = "Regione",
                        options = Regione.values().map { it.nome },
                        selectedOption = Regione.values().first { it.codice == regioneDefault }.nome,
                        onOptionSelected = { nome -> 
                            val regione = Regione.values().first { it.nome == nome }
                            settingsStore.setRegioneDefault(regione.codice)
                        }
                    )
                }
                
                // Promemoria
                Panel(title = "Promemoria") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Notifiche promemoria")
                                Text(text = "Ricevi avvisi per scadenze bollo e revisione", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { settingsStore.setNotificationsEnabled(it) }
                            )
                        }
                        
                        if (notificationsEnabled) {
                            Text(text = "Giorni prima della scadenza:", style = MaterialTheme.typography.bodyMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(30, 15, 7, 3, 1).forEach { day ->
                                    val isSelected = day in reminderDays
                                    androidx.compose.material3.FilterChip(
                                        selected = isSelected,
                                        onClick = { 
                                            val newDays = if (isSelected) reminderDays.filter { it != day } else reminderDays + day
                                            settingsStore.setReminderDays(newDays.sorted())
                                        },
                                        label = { Text(text = "$day gg") }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Pro status
                Panel(title = "Versione Pro") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isPro) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Pro attivo", fontWeight = FontWeight.Bold)
                                    Text(text = "Tutte le funzionalità sbloccate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle, contentDescription = "Pro", tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Button(onClick = { /* navigate to pro screen */ }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Sblocca versione Pro")
                            }
                        }
                        
                        Button(onClick = { /* restore purchases */ }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Ripristina acquisti")
                        }
                    }
                }
                
                // Info
                Panel(title = "Informazioni") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Versione")
                            Text(text = "1.0.0")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Privacy Policy")
                            androidx.compose.material3.TextButton(onClick = { /* open privacy */ }) {
                                Text(text = "Leggi")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Open Source Licenses")
                            androidx.compose.material3.TextButton(onClick = { /* open licenses */ }) {
                                Text(text = "Visualizza")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text = text)
    }
}

class SettingsViewModel @Inject constructor(
    val settingsStore: SettingsStore
) : ViewModel()