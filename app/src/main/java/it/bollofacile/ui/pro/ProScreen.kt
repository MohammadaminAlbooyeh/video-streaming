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
import androidx.compose.ui.res.stringResource
import it.bollofacile.R
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
                text = stringResource(R.string.pro_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }

        if (isPro) {
            Panel(title = stringResource(R.string.pro_thanks)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Pro",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.pro_unlocked_msg),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.pro_benefits_list),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Panel(title = stringResource(R.string.unlock_pro_panel)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Pro",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = stringResource(R.string.pro_features_slogan),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    ProFeatureRow(Icons.Default.History, stringResource(R.string.pro_feature_history), stringResource(R.string.pro_feature_history_desc))
                    ProFeatureRow(Icons.Default.FileDownload, stringResource(R.string.pro_feature_export), stringResource(R.string.pro_feature_export_desc))
                    ProFeatureRow(Icons.Default.Block, stringResource(R.string.pro_feature_no_ads), stringResource(R.string.pro_feature_no_ads_desc))
                    ProFeatureRow(Icons.Default.SupportAgent, stringResource(R.string.pro_feature_support), stringResource(R.string.pro_feature_support_desc))
                    ProFeatureRow(Icons.Default.Update, stringResource(R.string.pro_feature_updates), stringResource(R.string.pro_feature_updates_desc))

                    Button(
                        onClick = { activity?.let { viewModel.buyPro(it) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.buy_pro_fmt, proPrice ?: "…"),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.restorePurchases() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.restore_purchases))
                    }
                }
            }

            Panel(title = stringResource(R.string.why_pro)) {
                Text(
                    text = stringResource(R.string.why_pro_text),
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
