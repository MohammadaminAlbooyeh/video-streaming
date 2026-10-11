package it.bollofacile.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import it.bollofacile.MainActivity
import it.bollofacile.R

object NotificationHelper {

    private const val CHANNEL_BOLLO = "bollo_reminders"
    private const val CHANNEL_REVISIONE = "revisione_reminders"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelBollo = NotificationChannel(
                CHANNEL_BOLLO,
                "Scadenze Bollo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Promemoria per il pagamento del bollo auto"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
            }
            
            val channelRevisione = NotificationChannel(
                CHANNEL_REVISIONE,
                "Scadenze Revisione",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Promemoria per la revisione auto"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
            }
            
            manager.createNotificationChannels(listOf(channelBollo, channelRevisione))
        }
    }

    fun showReminderNotification(
        context: Context,
        veicoloId: Long,
        tipo: String
    ) {
        createNotificationChannels(context)
        
        val (channelId, title, contentText) = when (tipo) {
            "bollo" -> CHANNEL_BOLLO to "Bollo in scadenza" to "Il bollo per il tuo veicolo sta per scadere"
            "revisione" -> CHANNEL_REVISIONE to "Revisione in scadenza" to "La revisione per il tuo veicolo sta per scadere"
            else -> CHANNEL_BOLLO to "Scadenza" to "Hai una scadenza in arrivo"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("veicolo_id", veicoloId)
            putExtra("scroll_to", tipo)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (veicoloId * 31 + tipo.hashCode()).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((veicoloId * 31 + tipo.hashCode()).toInt(), notification)
    }
}