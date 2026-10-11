package it.bollofacile.ui.pro

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
import it.bollofacile.billing.BillingManager
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.ui.components.Panel
import it.bollofacile.ui.theme.Theme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun ProScreen(
    onNavigateBack: () -> Unit
) {
    val billingManager = BillingManager(androidx.compose.ui.platform.LocalContext.current)
    val settingsStore = androidx.hilt.navigation.compose.hiltViewModel<ProViewModel>().settingsStore
    val isPro by settingsStore.isProEnabled.collectAsStateWithLifecycle()
    var loading by mutableStateOf(false)
    
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
                    Text(text = "BolloFacile Pro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }
                
                if (isPro) {
                    Panel(title = "Grazie per il supporto!") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                                contentDescription = "Pro",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(text = "Hai sbloccato tutte le funzionalità Pro", style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.TextAlign.Center)
                            Text(text = "• Storico pagamenti illimitato\n• Export/Import dati\n• Nessuna pubblicità\n• Supporto prioritario\n• Aggiornamenti tariffe a vita", style = MaterialTheme.typography.bodyMedium)
                            
                            Button(onClick = { /* manage subscription */ }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Gestisci abbonamento")
                            }
                        }
                    }
                } else {
                    Panel(title = "Sblocca la versione Pro") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Star,
                                contentDescription = "Pro",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(text = "Funzionalità Pro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.TextAlign.Center)
                            
                            ProFeatureRow(icon = androidx.compose.material.icons.Icons.Default.History, title = "Storico pagamenti", description = "Salva e visualizza tutti i pagamenti bollo e revisione")
                            ProFeatureRow(icon = androidx.compose.material.icons.Icons.Default.FileDownload, title = "Export/Import dati", description = "Backup completo in JSON/CSV")
                            ProFeatureRow(icon = androidx.compose.material.icons.Icons.Default.Block, title = "Nessuna pubblicità", description = "Esperienza pulita senza banner o interstitial")
                            ProFeatureRow(icon = androidx.compose.material.icons.Icons.Default.SupportAgent, title = "Supporto prioritario", description = "Risposte rapide alle tue domande")
                            ProFeatureRow(icon = androidx.compose.material.icons.Icons.Default.Update, title = "Aggiornamenti a vita", description = "Tariffe sempre aggiornate ogni anno")
                            
                            Button(
                                onClick = {
                                    loading = true
                                    val activity = androidx.compose.ui.platform.LocalContext.current as android.app.Activity
                                    billingManager.launchPurchaseFlow(activity)
                                    loading = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !loading
                            ) {
                                if (loading) {
                                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Text(text = "Acquista Pro - €4,99 (una tantum)", fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Button(onClick = { billingManager.restorePurchases() }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Ripristina acquisti")
                            }
                        }
                    }
                    
                    Panel(title = "Perché Pro?") {
                        Text(text = "BolloFacile è sviluppato da un piccolo team indipendente. Il tuo acquisto ci aiuta a mantenere l'app aggiornata con le nuove tariffe annuali, aggiungere nuove funzionalità e garantire un'esperienza senza pubblicità. Grazie per il tuo supporto!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProFeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

class ProViewModel @Inject constructor(
    val settingsStore: SettingsStore
) : ViewModel()