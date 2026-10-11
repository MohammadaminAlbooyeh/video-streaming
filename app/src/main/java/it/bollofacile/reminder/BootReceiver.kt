package it.bollofacile.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import it.bollofacile.BolloFacileApp
import it.bollofacile.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        CoroutineScope(Dispatchers.IO).launch {
            val app = context.applicationContext as BolloFacileApp
            val scheduler = app.hiltEntryPoint.reminderScheduler()
            scheduler.rescheduleAfterBoot()
        }
    }
}