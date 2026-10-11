package it.bollofacile.rules

import it.bollofacile.rules.model.*
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode

object SanzioniRules {

    // Ravvedimento operoso (Art. 13 D.Lgs. 472/1997)
    // Fasce di riduzione sanzione in base ai giorni di ritardo:
    // - Entro 14 giorni: 1/10 del minimo (0.1%)
    // - Entro 30 giorni: 1/8 del minimo (1.5%)
    // - Entro 90 giorni: 1/7 del minimo (1.67%)
    // - Entro 1 anno: 1/6 del minimo (2.5%)
    // - Entro 2 anni: 1/5 del minimo (3%)
    // Oltre 2 anni: sanzione piena (30%) + interessi

    // Sanzione base: 30% dell'importo dovuto (min €25, max importo dovuto)
    const val SANZIONE_PERCENTUALE = 0.30
    const val SANZIONE_MINIMA = 25.0
    
    // Interessi legali 2026 (aggiornare annualmente)
    const val INTERESSE_LEGALE_ANNUO = 0.025 // 2.5%

    // Fasce ravvedimento operoso
    private val FASCE_RAVVEDIMENTO = listOf(
        FasciaRavvedimento(14, 0.10, "Entro 14 giorni (1/10)"),
        FasciaRavvedimento(30, 0.125, "Entro 30 giorni (1/8)"),
        FasciaRavvedimento(90, 0.142857, "Entro 90 giorni (1/7)"),
        FasciaRavvedimento(365, 0.166667, "Entro 1 anno (1/6)"),
        FasciaRavvedimento(730, 0.20, "Entro 2 anni (1/5)")
    )

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

        // Sanzione base (30% con minimo €25)
        val sanzioneBase = (importoBollo * SANZIONE_PERCENTUALE).coerceAtLeast(SANZIONE_MINIMA).coerceAtMost(importoBollo)
        
        // Interessi legali (giornalieri)
        val interessiGiornalieri = importoBollo * INTERESSE_LEGALE_ANNUO / 365
        val interessi = interessiGiornalieri * giorniRitardo
        
        // Ravvedimento operoso
        val fascia = FASCE_RAVVEDIMENTO.firstOrNull { giorniRitardo <= it.giorniMax }
        val ravvedimento = if (fascia != null) {
            sanzioneBase * fascia.riduzione
        } else {
            sanzioneBase // Nessuna riduzione oltre 2 anni
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
                dataLimite = dataScadenza.plusDays(fascia.giorniMax),
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