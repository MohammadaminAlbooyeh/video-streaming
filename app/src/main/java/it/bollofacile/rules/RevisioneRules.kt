package it.bollofacile.rules

import it.bollofacile.util.daysUntil
import it.bollofacile.util.maxDaysInMonth
import it.bollofacile.rules.model.*
import java.time.LocalDate
import java.time.YearMonth

object RevisioneRules {

    // Prima revisione: 4 anni dall'immatricolazione
    // Revisioni successive: ogni 2 anni
    // Veicoli storici (30+ anni): ogni 2 anni
    // Veicoli elettrici: alcune regioni esentano prima revisione

    fun calcolaProssimaRevisione(
        dataImmatricolazione: LocalDate,
        ultimaRevisione: LocalDate? = null,
        isStorico: Boolean = false,
        isElettrico: Boolean = false,
        regione: Regione = Regione.LOMBARDIA
    ): RevisioneResult {
        
        val ora = LocalDate.now()
        val annoImm = dataImmatricolazione.year

        // Prima revisione: 4 anni dall'immatricolazione; successive: 2 anni dall'ultima.
        // La scadenza è l'ultimo giorno del mese di riferimento, anche se già passata.
        val prossimaScadenza: LocalDate = if (ultimaRevisione == null) {
            val anno = annoImm + 4
            LocalDate.of(anno, dataImmatricolazione.month, dataImmatricolazione.month.maxDaysInMonth(anno))
        } else {
            val anno = ultimaRevisione.year + 2
            LocalDate.of(anno, ultimaRevisione.month, ultimaRevisione.month.maxDaysInMonth(anno))
        }
        val anniProssima = prossimaScadenza.year - ora.year

        // Veicoli storici: revisione ogni 2 anni dopo i 30 anni
        var note = ""
        if (isStorico) {
            note = "Veicolo storico: revisione ogni 2 anni"
        }
        
        // Elettrici: alcune regioni esentano prima revisione
        if (isElettrico && RegioniBollo.isEsenzioneElettricaApplicabile(regione, annoImm)) {
            note = if (note.isBlank()) "Veicolo elettrico: esenzione prima revisione" else "$note; Veicolo elettrico: esenzione prima revisione"
        }

        return RevisioneResult(
            prossimaScadenza = prossimaScadenza,
            anniProssima = anniProssima.coerceAtLeast(0),
            isStorico = isStorico,
            note = note
        )
    }

    fun isRevisioneScaduta(prossimaScadenza: LocalDate): Boolean {
        return LocalDate.now() > prossimaScadenza
    }

    fun giorniAllaScadenza(prossimaScadenza: LocalDate): Int {
        return LocalDate.now().daysUntil(prossimaScadenza).toInt()
    }
}