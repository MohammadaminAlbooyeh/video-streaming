package it.bollofacile.data.rates

import android.content.Context
import it.bollofacile.rules.Rates
import it.bollofacile.rules.RatesConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Loads fiscal rates (tariffs, penalty percentages, legal interest) from a JSON
 * file hosted on GitHub, so values can be corrected yearly without an app update.
 *
 * Startup flow: apply the cached copy immediately (if any), then refresh from
 * the network in the background. On any failure the built-in defaults stay active.
 */
object RatesRepository {

    private const val RATES_URL =
        "https://raw.githubusercontent.com/MohammadaminAlbooyeh/bollo-facile/master/rates.json"
    private const val CACHE_FILE = "rates_cache.json"

    suspend fun loadAndRefresh(context: Context) = withContext(Dispatchers.IO) {
        cacheFile(context).takeIf { it.exists() }?.let { file ->
            runCatching { apply(file.readText()) }
        }
        runCatching {
            val json = fetch()
            apply(json) // validate before persisting
            cacheFile(context).writeText(json)
        }
    }

    private fun cacheFile(context: Context) = File(context.filesDir, CACHE_FILE)

    private fun fetch(): String {
        val conn = URL(RATES_URL).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        try {
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                error("HTTP ${conn.responseCode}")
            }
            return conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    private fun apply(jsonText: String) {
        val root = JSONObject(jsonText)
        val defaults = RatesConfig()
        val version = root.optInt("version", defaults.version)
        // Never downgrade: ignore a remote/cached file older than the built-in defaults.
        if (version < defaults.version) return

        val tariffe = root.optJSONObject("tariffeKw")?.let { obj ->
            obj.keys().asSequence().associate { key -> key.toInt() to obj.getDouble(key) }
        } ?: defaults.tariffeKw

        val fasce = root.optJSONArray("fasceRavvedimento")?.let { arr ->
            (0 until arr.length()).map { i ->
                val f = arr.getJSONObject(i)
                RatesConfig.FasciaRates(
                    giorniMax = f.getInt("giorniMax"),
                    riduzione = f.getDouble("riduzione"),
                    descrizione = f.getString("descrizione")
                )
            }
        } ?: defaults.fasceRavvedimento

        Rates.current = RatesConfig(
            version = version,
            tariffeKw = tariffe,
            superbolloPerKw = root.optDouble("superbolloPerKw", defaults.superbolloPerKw),
            sogliaSuperbolloKw = root.optInt("sogliaSuperbolloKw", defaults.sogliaSuperbolloKw),
            sanzionePercentuale = root.optDouble("sanzionePercentuale", defaults.sanzionePercentuale),
            sanzioneMinima = root.optDouble("sanzioneMinima", defaults.sanzioneMinima),
            interesseLegaleAnnuo = root.optDouble("interesseLegaleAnnuo", defaults.interesseLegaleAnnuo),
            fasceRavvedimento = fasce
        )
    }
}
