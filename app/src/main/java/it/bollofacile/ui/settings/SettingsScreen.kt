package it.bollofacile.ui.settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
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
import androidx.lifecycle.viewModelScope
import it.bollofacile.R
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.rules.model.Regione
import it.bollofacile.ui.components.DropdownField
import it.bollofacile.ui.components.Panel
import it.bollofacile.ui.theme.Theme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.ui.platform.LocalContext
import javax.inject.Inject

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel = hiltViewModel<SettingsViewModel>()
    val theme by viewModel.settingsStore.theme.collectAsStateWithLifecycle(initialValue = SettingsStore.THEME_SYSTEM)
    val regioneDefault by viewModel.settingsStore.regioneDefault.collectAsStateWithLifecycle(initialValue = "LOM")
    val reminderDays by viewModel.settingsStore.reminderDays.collectAsStateWithLifecycle(initialValue = emptyList())
    val notificationsEnabled by viewModel.settingsStore.notificationsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val isPro by viewModel.settingsStore.isProEnabled.collectAsStateWithLifecycle(initialValue = false)
    val activity = LocalContext.current as? android.app.Activity
    
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi")
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
                            Text(text = when (theme) {
                                0 -> "Sistema"
                                1 -> "Chiaro"
                                2 -> "Scuro"
                                else -> "Sistema"
                            })
                        }
                        
                        // Simplified theme selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOption(modifier = Modifier.weight(1f), text = "Sistema", selected = theme == 0, onClick = { viewModel.setTheme(0) })
                            ThemeOption(modifier = Modifier.weight(1f), text = "Chiaro", selected = theme == 1, onClick = { viewModel.setTheme(1) })
                            ThemeOption(modifier = Modifier.weight(1f), text = "Scuro", selected = theme == 2, onClick = { viewModel.setTheme(2) })
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
                            viewModel.setRegioneDefault(regione.codice)
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
                                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
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
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { 
                                            val newDays = if (isSelected) reminderDays.filter { it != day } else reminderDays + day
                                            viewModel.setReminderDays(newDays.sorted())
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
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Pro", tint = MaterialTheme.colorScheme.primary)
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
                
                // Export dati (Pro)
                Panel(title = "Dati") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isPro) "Esporta veicoli e pagamenti in formato CSV" else "L'export dati è una funzionalità Pro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { activity?.let { viewModel.exportCsv(it) } },
                            enabled = isPro && activity != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Esporta dati (CSV)")
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
                            TextButton(onClick = { /* open privacy */ }) {
                                Text(text = "Leggi")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Open Source Licenses")
                            TextButton(onClick = { /* open licenses */ }) {
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
private fun ThemeOption(modifier: Modifier = Modifier, text: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text = text)
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsStore: SettingsStore,
    private val veicoloRepository: it.bollofacile.data.repository.VeicoloRepository,
    private val database: it.bollofacile.data.db.AppDatabase
) : ViewModel() {
    fun setTheme(theme: Int) = viewModelScope.launch { settingsStore.setTheme(theme) }
    fun setRegioneDefault(codice: String) = viewModelScope.launch { settingsStore.setRegioneDefault(codice) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { settingsStore.setNotificationsEnabled(enabled) }
    fun setReminderDays(days: List<Int>) = viewModelScope.launch { settingsStore.setReminderDays(days) }

    fun exportCsv(activity: android.app.Activity) = viewModelScope.launch {
        val veicoli = veicoloRepository.getAll().first()
        val pagamenti = database.pagamentoDao().getAllOnce()
        val csv = it.bollofacile.util.Export.buildCsv(veicoli, pagamenti)
        val file = it.bollofacile.util.Export.writeCsvFile(activity.applicationContext, csv)
        it.bollofacile.util.Share.shareFile(
            activity,
            file,
            "${activity.packageName}.fileprovider",
            "Export dati BolloFacile"
        )
    }
}