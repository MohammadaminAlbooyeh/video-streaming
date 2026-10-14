package it.bollofacile.ui.settings
import androidx.compose.material.icons.Icons
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.bollofacile.R
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.rules.model.Regione
import it.bollofacile.ui.components.DropdownField
import it.bollofacile.ui.components.Panel
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
    val activity = LocalContext.current as? android.app.Activity

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
            }

            // Theme
            Panel(title = stringResource(R.string.appearance)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stringResource(R.string.theme))
                            Text(text = stringResource(R.string.theme_choose), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = when (theme) {
                            SettingsStore.THEME_LIGHT -> stringResource(R.string.theme_light)
                            SettingsStore.THEME_DARK -> stringResource(R.string.theme_dark)
                            else -> stringResource(R.string.theme_system)
                        })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOption(modifier = Modifier.weight(1f), text = stringResource(R.string.theme_system), selected = theme == SettingsStore.THEME_SYSTEM, onClick = { viewModel.setTheme(SettingsStore.THEME_SYSTEM) })
                        ThemeOption(modifier = Modifier.weight(1f), text = stringResource(R.string.theme_light), selected = theme == SettingsStore.THEME_LIGHT, onClick = { viewModel.setTheme(SettingsStore.THEME_LIGHT) })
                        ThemeOption(modifier = Modifier.weight(1f), text = stringResource(R.string.theme_dark), selected = theme == SettingsStore.THEME_DARK, onClick = { viewModel.setTheme(SettingsStore.THEME_DARK) })
                    }
                }
            }

            // Default region
            Panel(title = stringResource(R.string.default_region)) {
                DropdownField(
                    label = stringResource(R.string.region),
                    options = Regione.values().map { it.nome },
                    selectedOption = Regione.values().first { it.codice == regioneDefault }.nome,
                    onOptionSelected = { nome ->
                        val regione = Regione.values().first { it.nome == nome }
                        viewModel.setRegioneDefault(regione.codice)
                    }
                )
            }

            // Reminders
            Panel(title = stringResource(R.string.reminders)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stringResource(R.string.reminder_notifications))
                            Text(text = stringResource(R.string.reminder_notifications_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                    }

                    if (notificationsEnabled) {
                        Text(text = stringResource(R.string.reminder_days) + ":", style = MaterialTheme.typography.bodyMedium)
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
                                    label = { Text(text = stringResource(R.string.days_short_fmt, day)) }
                                )
                            }
                        }
                    }
                }
            }

            // Data export
            Panel(title = stringResource(R.string.data_section)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.export_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { activity?.let { viewModel.exportCsv(it) } },
                        enabled = activity != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.export_csv))
                    }
                }
            }

            // About
            Panel(title = stringResource(R.string.about)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = stringResource(R.string.version))
                        Text(text = "1.0.0")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.privacy_policy))
                        TextButton(onClick = { activity?.let { openUrl(it, PRIVACY_POLICY_URL) } }) {
                            Text(text = stringResource(R.string.read))
                        }
                    }
                }
            }
        }
    }
}

private const val PRIVACY_POLICY_URL = "https://github.com/MohammadaminAlbooyeh/bollo-facile/blob/master/PRIVACY_POLICY.md"

private fun openUrl(activity: android.app.Activity, url: String) {
    runCatching {
        activity.startActivity(
            android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        )
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
            activity.getString(it.bollofacile.R.string.export_subject)
        )
    }
}
