package it.bollofacile.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class SettingsStore @Inject constructor(
    private val context: Context
) {

    private val KEY_REGIONE_DEFAULT = stringPreferencesKey("regione_default")
    private val KEY_REMINDER_DAYS = stringPreferencesKey("reminder_days")
    private val KEY_THEME = intPreferencesKey("theme")
    private val KEY_PRO_ENABLED = booleanPreferencesKey("pro_enabled")
    private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    private val KEY_AD_CONSENT = booleanPreferencesKey("ad_consent")
    private val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")

    companion object {
        const val DEFAULT_REMINDER_DAYS = "30,15,7,1"
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }

    val regioneDefault: Flow<String> = context.dataStore.data
        .map { it[KEY_REGIONE_DEFAULT] ?: "LOM" }

    suspend fun setRegioneDefault(regione: String) {
        context.dataStore.edit { it[KEY_REGIONE_DEFAULT] = regione }
    }

    val reminderDays: Flow<List<Int>> = context.dataStore.data
        .map { prefs ->
            (prefs[KEY_REMINDER_DAYS] ?: DEFAULT_REMINDER_DAYS)
                .split(",").mapNotNull { it.trim().toIntOrNull() }
        }

    suspend fun setReminderDays(days: List<Int>) {
        context.dataStore.edit { it[KEY_REMINDER_DAYS] = days.joinToString(",") }
    }

    val theme: Flow<Int> = context.dataStore.data
        .map { it[KEY_THEME] ?: THEME_SYSTEM }

    suspend fun setTheme(theme: Int) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    val isProEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_PRO_ENABLED] ?: false }

    suspend fun setProEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_PRO_ENABLED] = enabled }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    val adConsent: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_AD_CONSENT] ?: false }

    suspend fun setAdConsent(consent: Boolean) {
        context.dataStore.edit { it[KEY_AD_CONSENT] = consent }
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_FIRST_LAUNCH] ?: true }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { it[KEY_FIRST_LAUNCH] = false }
    }

    suspend fun getRegioneDefaultSync(): String = regioneDefault.first()
    suspend fun getReminderDaysSync(): List<Int> = reminderDays.first()
    suspend fun getThemeSync(): Int = theme.first()
    suspend fun isProEnabledSync(): Boolean = isProEnabled.first()
}
