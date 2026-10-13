package it.bollofacile.ui.pro

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bollofacile.ui.components.Panel

@Composable
fun ProScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProViewModel = hiltViewModel()
) {
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val proPrice by viewModel.proPrice.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BolloFacile Pro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi")
            }
        }

        if (isPro) {
            Panel(title = "Grazie per il supporto!") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Pro",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Hai sbloccato tutte le funzionalità Pro",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• Storico pagamenti illimitato\n• Export dati\n• Nessuna pubblicità\n• Supporto prioritario\n• Aggiornamenti tariffe a vita",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Panel(title = "Sblocca la versione Pro") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Pro",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Funzionalità Pro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    ProFeatureRow(Icons.Default.History, "Storico pagamenti", "Salva e visualizza tutti i pagamenti bollo e revisione")
                    ProFeatureRow(Icons.Default.FileDownload, "Export dati", "Backup completo in CSV")
                    ProFeatureRow(Icons.Default.Block, "Nessuna pubblicità", "Esperienza pulita senza banner o interstitial")
                    ProFeatureRow(Icons.Default.SupportAgent, "Supporto prioritario", "Risposte rapide alle tue domande")
                    ProFeatureRow(Icons.Default.Update, "Aggiornamenti a vita", "Tariffe sempre aggiornate ogni anno")

                    Button(
                        onClick = { activity?.let { viewModel.buyPro(it) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Acquista Pro - ${proPrice ?: "…"} (una tantum)",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.restorePurchases() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Ripristina acquisti")
                    }
                }
            }

            Panel(title = "Perché Pro?") {
                Text(
                    text = "BolloFacile è sviluppato da un piccolo team indipendente. Il tuo acquisto ci aiuta a mantenere l'app aggiornata con le nuove tariffe annuali, aggiungere nuove funzionalità e garantire un'esperienza senza pubblicità. Grazie per il tuo supporto!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ProFeatureRow(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
