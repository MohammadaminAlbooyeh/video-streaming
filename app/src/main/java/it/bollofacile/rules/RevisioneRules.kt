package it.bollofacile.rules

import it.bollofacile.rules.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

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
        val meseImm = dataImmatricolazione.month
        
        // Se c'è un'ultima revisione, calcola da quella
        val baseDate = ultimaRevisione ?: dataImmatricolazione
        val anniDaBase = YearMonth.from(baseDate).yearsUntil(YearMonth.from(ora)).years
        
        var prossimaScadenza: LocalDate
        var anniProssima: Int
        
        if (ultimaRevisione == null) {
            // Prima revisione
            if (anniDaBase < 4) {
                prossimaScadenza = LocalDate(annoImm + 4, meseImm, meseImm.maxDaysInMonth(annoImm + 4))
                anniProssima = 4 - anniDaBase
            } else {
                // Già passati 4 anni, calcola cicli da 2 anni
                val anniPassati = anniDaBase - 4
                val cicliCompleti = anniPassati / 2
                val prossimoCiclo = cicliCompleti + 1
                prossimaScadenza = LocalDate(annoImm + 4 + prossimoCiclo * 2, meseImm, meseImm.maxDaysInMonth(annoImm + 4 + prossimoCiclo * 2))
                anniProssima = (4 + prossimoCiclo * 2) - anniDaBase
            }
        } else {
            // Revisioni successive: ogni 2 anni
            val anniDaUltima = YearMonth.from(ultimaRevisione).yearsUntil(YearMonth.from(ora)).years
            if (anniDaUltima < 2) {
                prossimaScadenza = LocalDate(ultimaRevisione.year + 2, ultimaRevisione.month, ultimaRevisione.month.maxDaysInMonth(ultimaRevisione.year + 2))
                anniProssima = 2 - anniDaUltima
            } else {
                val anniPassati = anniDaUltima - 2
                val cicliCompleti = anniPassati / 2
                val prossimoCiclo = cicliCompleti + 1
                prossimaScadenza = LocalDate(ultimaRevisione.year + 2 + prossimoCiclo * 2, ultimaRevisione.month, ultimaRevisione.month.maxDaysInMonth(ultimaRevisione.year + 2 + prossimoCiclo * 2))
                anniProssima = (2 + prossimoCiclo * 2) - anniDaUltima
            }
        }

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