package it.bollofacile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import it.bollofacile.rules.model.Veicolo
import it.bollofacile.util.Format
import it.bollofacile.util.daysUntil

@Composable
fun ScadenzaCard(
    modifier: Modifier = Modifier,
    veicolo: Veicolo,
    bolloResult: it.bollofacile.rules.model.BolloResult,
    revisioneResult: it.bollofacile.rules.model.RevisioneResult,
    onClick: () -> Unit
) {
    val giorniBollo = java.time.LocalDate.now().daysUntil(bolloResult.scadenza).toInt()
    val giorniRevisione = java.time.LocalDate.now().daysUntil(revisioneResult.prossimaScadenza).toInt()
    val isBolloScaduto = giorniBollo < 0
    val isRevisioneScaduta = giorniRevisione < 0
    val isBolloUrgente = giorniBollo >= 0 && giorniBollo <= 30
    val isRevisioneUrgente = giorniRevisione >= 0 && giorniRevisione <= 30

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isBolloScaduto || isRevisioneScaduta -> MaterialTheme.colorScheme.errorContainer
                isBolloUrgente || isRevisioneUrgente -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${veicolo.marca} ${veicolo.modello}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    veicolo.targa?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (isBolloScaduto || isRevisioneScaduta) {
                    Text(
                        text = "SCADUTO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else if (isBolloUrgente || isRevisioneUrgente) {
                    Text(
                        text = "URGENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bollo
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Bollo",
                            tint = if (isBolloScaduto) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Bollo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "€${"%.2f".format(bolloResult.totale)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBolloScaduto) "Scaduto ${-giorniBollo} giorni fa" else "${giorniBollo} giorni",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isBolloScaduto -> MaterialTheme.colorScheme.error
                            isBolloUrgente -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Divider
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                
                // Revisione
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Revisione",
                            tint = if (isRevisioneScaduta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Revisione",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = revisioneResult.prossimaScadenza.format(Format.DATE_FORMATTER),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRevisioneScaduta) "Scaduta ${-giorniRevisione} giorni fa" else "${giorniRevisione} giorni",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isRevisioneScaduta -> MaterialTheme.colorScheme.error
                            isRevisioneUrgente -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}