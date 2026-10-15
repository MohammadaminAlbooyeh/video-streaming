package it.bollofacile.rules

import it.bollofacile.util.daysUntil
import it.bollofacile.util.maxDaysInMonth
import it.bollofacile.rules.model.*
import java.time.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode

object SanzioniRules {

    // Ravvedimento operoso (voluntary correction, Art. 13 D.Lgs. 472/1997)
    // Penalty reduction brackets based on days of delay:
    // - Within 14 days: 1/10 of the minimum (0.1%)
    // - Within 30 days: 1/8 of the minimum (1.5%)
    // - Within 90 days: 1/7 of the minimum (1.67%)
    // - Within 1 year: 1/6 of the minimum (2.5%)
    // - Within 2 years: 1/5 of the minimum (3%)
    // Beyond 2 years: full penalty (30%) + interest

    // Year-dependent values live in Rates.current so they can be updated remotely.
    val SANZIONE_PERCENTUALE: Double get() = Rates.current.sanzionePercentuale
    val SANZIONE_MINIMA: Double get() = Rates.current.sanzioneMinima
    val INTERESSE_LEGALE_ANNUO: Double get() = Rates.current.interesseLegaleAnnuo

    private val FASCE_RAVVEDIMENTO: List<FasciaRavvedimento>
        get() = Rates.current.fasceRavvedimento.map {
            FasciaRavvedimento(it.giorniMax, it.riduzione, it.descrizione)
        }

    data class FasciaRavvedimento(
        val giorniMax: Int,
        val riduzione: Double,
        val descrizione: String
    )

    fun calcolaSanzione(
        importoBollo: Double,
        dataScadenza: LocalDate,
        dataPagamento: LocalDate = LocalDate.now()
    ): SanzioneResult {
        
        val giorniRitardo = dataScadenza.daysUntil(dataPagamento).toInt().coerceAtLeast(0)
        
        if (giorniRitardo == 0) {
            return SanzioneResult(
                importoOriginale = importoBollo,
                giorniRitardo = 0,
                sanzioneBase = 0.0,
                interessi = 0.0,
                ravvedimentoOperoso = 0.0,
                totaleDaPagare = importoBollo,
                fasciaRavvedimento = "In regola"
            )
        }

        // Base penalty (30% with €25 minimum)
        val sanzioneBase = (importoBollo * SANZIONE_PERCENTUALE).coerceAtLeast(SANZIONE_MINIMA)
        
        // Legal interest (daily)
        val interessiGiornalieri = importoBollo * INTERESSE_LEGALE_ANNUO / 365
        val interessi = interessiGiornalieri * giorniRitardo
        
        // Ravvedimento operoso
        val fascia = FASCE_RAVVEDIMENTO.firstOrNull { giorniRitardo <= it.giorniMax }
        val ravvedimento = if (fascia != null) {
            sanzioneBase * fascia.riduzione
        } else {
            sanzioneBase // No reduction beyond 2 years
        }
        
        val fasciaDesc = fascia?.descrizione ?: "Oltre 2 anni (sanzione piena)"
        
        val totale = importoBollo + ravvedimento + interessi

        return SanzioneResult(
            importoOriginale = arrotonda(importoBollo),
            giorniRitardo = giorniRitardo,
            sanzioneBase = arrotonda(sanzioneBase),
            interessi = arrotonda(interessi),
            ravvedimentoOperoso = arrotonda(ravvedimento),
            totaleDaPagare = arrotonda(totale),
            fasciaRavvedimento = fasciaDesc
        )
    }

    fun getProssimeScadenzeRavvedimento(dataScadenza: LocalDate): List<ScadenzaRavvedimento> {
        return FASCE_RAVVEDIMENTO.map { fascia ->
            ScadenzaRavvedimento(
                dataLimite = dataScadenza,
                descrizione = fascia.descrizione,
                riduzione = fascia.riduzione
            )
        }
    }

    data class ScadenzaRavvedimento(
        val dataLimite: LocalDate,
        val descrizione: String,
        val riduzione: Double
    )

    private fun arrotonda(value: Double): Double {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}