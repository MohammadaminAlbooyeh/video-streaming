package it.bollofacile.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.domain.ProssimeScadenzeUseCase
import it.bollofacile.domain.ScadenzaVeicolo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    private val context: Context,
    private val settingsStore: SettingsStore,
    private val scadenzeUseCase: ProssimeScadenzeUseCase
) {

    companion object {
        const val ACTION_CHECK_REMINDERS = "it.bollofacile.ACTION_CHECK_REMINDERS"
        const val EXTRA_VEICOLO_ID = "veicolo_id"
        const val EXTRA_TIPO = "tipo" // "bollo" o "revisione"
    }

    fun scheduleAllReminders() {
        CoroutineScope(Dispatchers.IO).launch {
            val scadenze = scadenzeUseCase().first()
            val reminderDays = settingsStore.getReminderDaysSync()
            val notificationsEnabled = settingsStore.notificationsEnabled.first()
            
            if (!notificationsEnabled) {
                cancelAllReminders()
                return@launch
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            for (scadenza in scadenze) {
                scheduleVeicoloReminders(alarmManager, scadenza, reminderDays)
            }
        }
    }

    private fun scheduleVeicoloReminders(
        alarmManager: AlarmManager,
        scadenza: ScadenzaVeicolo,
        reminderDays: List<Int>
    ) {
        val veicoloId = scadenza.veicolo.id
        
        // Bollo reminders
        if (!scadenza.isBolloScaduto) {
            for (days in reminderDays) {
                val triggerDate = scadenza.bolloResult.scadenza.minusDays(days.toLong())
                if (triggerDate >= java.time.LocalDate.now()) {
                    setAlarm(alarmManager, veicoloId, "bollo", triggerDate)
                }
            }
        }
        
        // Revisione reminders
        if (!scadenza.isRevisioneScaduta) {
            for (days in reminderDays) {
                val triggerDate = scadenza.revisioneResult.prossimaScadenza.minusDays(days.toLong())
                if (triggerDate >= java.time.LocalDate.now()) {
                    setAlarm(alarmManager, veicoloId, "revisione", triggerDate)
                }
            }
        }
    }

    private fun setAlarm(
        alarmManager: AlarmManager,
        veicoloId: Long,
        tipo: String,
        triggerDate: java.time.LocalDate
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_CHECK_REMINDERS
            putExtra(EXTRA_VEICOLO_ID, veicoloId)
            putExtra(EXTRA_TIPO, tipo)
        }

        val requestCode = (veicoloId * 31 + tipo.hashCode()).toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        val triggerMillis = triggerDate.atTime(9, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    fun cancelAllReminders() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAfterBoot() {
        scheduleAllReminders()
    }
}