package it.bollofacile.rules

import it.bollofacile.util.daysUntil
import it.bollofacile.util.maxDaysInMonth
import it.bollofacile.rules.model.*
import java.time.LocalDate
import java.time.YearMonth

object RevisioneRules {

    // First revisione (inspection): 4 years after registration
    // Subsequent revisioni: every 2 years
    // Historic vehicles (30+ years): every 2 years
    // Electric vehicles: some regions exempt the first revisione

    fun calcolaProssimaRevisione(
        dataImmatricolazione: LocalDate,
        ultimaRevisione: LocalDate? = null,
        isStorico: Boolean = false,
        isElettrico: Boolean = false,
        regione: Regione = Regione.LOMBARDIA
    ): RevisioneResult {
        
        val ora = LocalDate.now()
        val annoImm = dataImmatricolazione.year

        // First revisione: 4 years after registration; subsequent ones: 2 years after the last.
        // The due date is the last day of the reference month, even if already past.
        val prossimaScadenza: LocalDate = if (ultimaRevisione == null) {
            val anno = annoImm + 4
            LocalDate.of(anno, dataImmatricolazione.month, dataImmatricolazione.month.maxDaysInMonth(anno))
        } else {
            val anno = ultimaRevisione.year + 2
            LocalDate.of(anno, ultimaRevisione.month, ultimaRevisione.month.maxDaysInMonth(anno))
        }
        val anniProssima = prossimaScadenza.year - ora.year

        // Historic vehicles: revisione every 2 years after 30 years of age.
        // Note: the revisione is a national obligation — electric vehicles are
        // NOT exempt in any region.
        var note = ""
        if (isStorico) {
            note = "Veicolo storico: revisione ogni 2 anni"
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