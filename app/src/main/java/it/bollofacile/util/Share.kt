package it.bollofacile.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object Share {
    
    fun shareText(activity: Activity, text: String, subject: String = "Condividi") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        activity.startActivity(Intent.createChooser(intent, subject))
    }
    
    fun shareFile(activity: Activity, file: File, authority: String, subject: String = "Condividi file") {
        val uri = FileProvider.getUriForFile(activity, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(Intent.createChooser(intent, subject))
    }
    
    fun shareBolloResult(
        activity: Activity,
        veicolo: it.bollofacile.rules.model.Veicolo,
        bolloResult: it.bollofacile.rules.model.BolloResult,
        revisioneResult: it.bollofacile.rules.model.RevisioneResult
    ) {
        val text = buildString {
            appendLine("BolloFacile - Calcolo bollo")
            appendLine("-------------------")
            appendLine("Veicolo: ${veicolo.marca} ${veicolo.modello}")
            veicolo.targa?.let { appendLine("Targa: $it") }
            appendLine("Potenza: ${veicolo.kw} kW / ${veicolo.cv} CV")
            appendLine("Alimentazione: ${veicolo.alimentazione.label}")
            appendLine("Classe Euro: ${veicolo.classeEuro.label}")
            appendLine("Immatricolazione: ${veicolo.dataImmatricolazione.format(Format.DATE_FORMATTER)}")
            appendLine("Regione: ${veicolo.regione.nome}")
            appendLine("")
            appendLine("Bollo base: €${Format.formatNumber(bolloResult.bolloBase)}")
            if (bolloResult.hasSuperbollo) {
                appendLine("Superbollo: €${Format.formatNumber(bolloResult.superbollo)}")
            }
            if (bolloResult.riduzioneAlimentazione > 0) {
                appendLine("Riduzione ${veicolo.alimentazione.label}: -€${Format.formatNumber(bolloResult.riduzioneAlimentazione)}")
            }
            if (bolloResult.riduzioneRegionale > 0) {
                appendLine("Riduzioni Euro/Regionali: -€${Format.formatNumber(bolloResult.riduzioneRegionale)}")
            }
            appendLine("")
            appendLine("TOTALE: €${Format.formatNumber(bolloResult.totale)}")
            appendLine("Scadenza: ${bolloResult.scadenza.format(Format.DATE_FORMATTER)}")
            appendLine("")
            appendLine("Prossima revisione: ${revisioneResult.prossimaScadenza.format(Format.DATE_FORMATTER)} (tra ${revisioneResult.anniProssima} anni)")
            appendLine("")
            appendLine("Calcolato con BolloFacile")
        }
        shareText(activity, text, "Calcolo bollo ${veicolo.marca} ${veicolo.modello}")
    }
}