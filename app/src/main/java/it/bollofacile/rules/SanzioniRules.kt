package it.bollofacile.rules

import it.bollofacile.util.daysUntil
import it.bollofacile.rules.model.*
import java.time.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode

object SanzioniRules {

    // Post-reform rules (D.Lgs. 87/2024, violations from 1 September 2024):
    // base penalty 25% of the unpaid tax; ravvedimento operoso reduces it to
    // 0.0833%/day within 14 days, 1.25% within 30, 1.39% within 90,
    // 3.125% within 1 year, 3.571% within 2, 4.167% beyond.
    // Legal interest accrues daily (1.60%/year from 1 Jan 2026).

    val SANZIONE_PERCENTUALE: Double get() = Rates.current.sanzionePercentuale
    val INTERESSE_LEGALE_ANNUO: Double get() = Rates.current.interesseLegaleAnnuo

    data class FasciaRavvedimento(
        val giorniMax: Int,
        val riduzione: Double,
        val descrizione: String,
        val perGiorno: Boolean = false
    )

    private val FASCE_RAVVEDIMENTO: List<FasciaRavvedimento>
        get() = Rates.current.fasceRavvedimento.map {
            FasciaRavvedimento(it.giorniMax, it.riduzione, it.descrizione, it.perGiorno)
        }

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

        // Full penalty if no voluntary correction were made
        val sanzioneBase = importoBollo * SANZIONE_PERCENTUALE

        // Legal interest (daily accrual)
        val interessi = importoBollo * INTERESSE_LEGALE_ANNUO / 365.0 * giorniRitardo

        // Ravvedimento operoso: reduced penalty as a fraction of the unpaid tax
        val fascia = FASCE_RAVVEDIMENTO.firstOrNull { giorniRitardo <= it.giorniMax }
        val ravvedimento = when {
            fascia == null -> importoBollo * Rates.current.sanzioneOltreUltimaFascia
            fascia.perGiorno -> importoBollo * fascia.riduzione * giorniRitardo
            else -> importoBollo * fascia.riduzione
        }

        val fasciaDesc = fascia?.descrizione ?: "Oltre 2 anni (4,167%)"

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
                dataLimite = dataScadenza.plusDays(fascia.giorniMax.toLong()),
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
