package it.bollofacile.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.rxjava3.RxDataStore
import androidx.datastore.preferences.rxjava3.preferencesDataStore
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsStore @Inject constructor(
    private val context: Context
) {

    private val dataStore = context.preferencesDataStore("settings")

    // Keys
    private val KEY_REGIONE_DEFAULT = PreferencesKeys.stringKey("regione_default")
    private val KEY_REMINDER_DAYS = PreferencesKeys.stringKey("reminder_days") // CSV
    private val KEY_THEME = PreferencesKeys.intKey("theme") // 0=system, 1=light, 2=dark
    private val KEY_PRO_ENABLED = PreferencesKeys.booleanKey("pro_enabled")
    private val KEY_NOTIFICATIONS_ENABLED = PreferencesKeys.booleanKey("notifications_enabled")
    private val KEY_AD_CONSENT = PreferencesKeys.booleanKey("ad_consent")
    private val KEY_FIRST_LAUNCH = PreferencesKeys.booleanKey("first_launch")

    companion object {
        const val DEFAULT_REMINDER_DAYS = "30,15,7,1"
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }

    // Regione default
    val regioneDefault: Flowable<String> = dataStore.data
        .map { it[KEY_REGIONE_DEFAULT] ?: "LOM" }

    suspend fun setRegioneDefault(regione: String) {
        dataStore.edit { it[KEY_REGIONE_DEFAULT] = regione }
    }

    // Reminder days
    val reminderDays: Flowable<List<Int>> = dataStore.data
        .map { 
            it[KEY_REMINDER_DAYS]?.split(",")?.map { it.trim().toIntOrNull() }?.filterNotNull() ?: DEFAULT_REMINDER_DAYS.split(",").map { it.toInt() }
        }

    suspend fun setReminderDays(days: List<Int>) {
        dataStore.edit { it[KEY_REMINDER_DAYS] = days.joinToString(",") }
    }

    // Theme
    val theme: Flowable<Int> = dataStore.data
        .map { it[KEY_THEME] ?: THEME_SYSTEM }

    suspend fun setTheme(theme: Int) {
        dataStore.edit { it[KEY_THEME] = theme }
    }

    // Pro status
    val isProEnabled: Flowable<Boolean> = dataStore.data
        .map { it[KEY_PRO_ENABLED] ?: false }

    suspend fun setProEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_PRO_ENABLED] = enabled }
    }

    // Notifications
    val notificationsEnabled: Flowable<Boolean> = dataStore.data
        .map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    // Ad consent
    val adConsent: Flowable<Boolean> = dataStore.data
        .map { it[KEY_AD_CONSENT] ?: false }

    suspend fun setAdConsent(consent: Boolean) {
        dataStore.edit { it[KEY_AD_CONSENT] = consent }
    }

    // First launch
    val isFirstLaunch: Flowable<Boolean> = dataStore.data
        .map { it[KEY_FIRST_LAUNCH] ?: true }

    suspend fun setFirstLaunchComplete() {
        dataStore.edit { it[KEY_FIRST_LAUNCH] = false }
    }

    // Synchronous getters for immediate access
    suspend fun getRegioneDefaultSync(): String = dataStore.data.first() [KEY_REGIONE_DEFAULT] ?: "LOM"
    suspend fun getReminderDaysSync(): List<Int> = dataStore.data.first() [KEY_REMINDER_DAYS]?.split(",")?.map { it.trim().toIntOrNull() }?.filterNotNull() ?: DEFAULT_REMINDER_DAYS.split(",").map { it.toInt() }
    suspend fun getThemeSync(): Int = dataStore.data.first() [KEY_THEME] ?: THEME_SYSTEM
    suspend fun isProEnabledSync(): Boolean = dataStore.data.first() [KEY_PRO_ENABLED] ?: false
}