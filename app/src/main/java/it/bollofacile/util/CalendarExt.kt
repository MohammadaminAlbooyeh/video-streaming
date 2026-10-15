package it.bollofacile.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import it.bollofacile.R
import java.time.LocalDate


/**
 * Opens the device calendar app (Google Calendar or any other) with a prefilled
 * all-day event. No calendar permission is needed for an ACTION_INSERT intent.
 */
fun addDeadlineToCalendar(context: Context, title: String, description: String, date: LocalDate) {
    // All-day events are interpreted in UTC by calendar apps; using the local
    // zone would shift the event to the previous day for positive UTC offsets.
    val startMillis = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        putExtra(CalendarContract.Events.HAS_ALARM, true)
    }
    // try/catch instead of resolveActivity: package-visibility rules (API 30+)
    // hide other apps' activities unless declared in <queries>.
    try {
        context.startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        Toast.makeText(context, R.string.no_calendar_app, Toast.LENGTH_SHORT).show()
    }
}
