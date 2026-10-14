package it.bollofacile.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import it.bollofacile.data.prefs.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action != ReminderScheduler.ACTION_CHECK_REMINDERS) return

        val veicoloId = intent.getLongExtra(ReminderScheduler.EXTRA_VEICOLO_ID, -1)
        val tipo = intent.getStringExtra(ReminderScheduler.EXTRA_TIPO) ?: "bollo"

        if (veicoloId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val settingsStore = SettingsStore(context)
            val notificationsEnabled = settingsStore.notificationsEnabled.first()
            
            if (!notificationsEnabled) return@launch

            // Here we would fetch the vehicle details from the repository
            // For now we show a generic notification
            NotificationHelper.showReminderNotification(
                context = context,
                veicoloId = veicoloId,
                tipo = tipo
            )
        }
    }
}