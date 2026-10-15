package it.bollofacile.rules

import it.bollofacile.rules.model.*
import java.time.LocalDate

object RegioniBollo {

    // National rule (L. 449/1997): battery-electric vehicles are exempt for
    // 5 years from first registration in every region; afterwards they pay a
    // reduced amount (25% of the corresponding petrol tariff in most regions).
    private const val ESENZIONE_ELETTRICA_NAZIONALE_ANNI = 5
    private const val QUOTA_ELETTRICA_DOPO_ESENZIONE = 0.25 // pays 25% → 75% reduction

    /**
     * Region-specific deviations from the national rules (2026).
     * Hybrid benefits are entirely regional; there is no national hybrid or
     * GPL/metano reduction. Regions not listed follow the national rules only.
     */
    private val REGIONAL_RULES = mapOf(
        Regione.LOMBARDIA to RegionalRule(
            esenzioneElettricaPermanente = true,
            // 50% for 5 years, only for hybrids with combustion engine ≤ 100 kW
            riduzioneIbrida = 0.5,
            anniRiduzioneIbrida = 5,
            maxKwIbrida = 100,
            note = "Lombardia: elettriche esenti senza limiti; ibride ≤100 kW -50% per 5 anni"
        ),
        Regione.PIEMONTE to RegionalRule(
            esenzioneElettricaPermanente = true,
            // Registrations from 2025: 50% for 5 years (earlier ones had full exemption)
            riduzioneIbrida = 0.5,
            anniRiduzioneIbrida = 5,
            note = "Piemonte: elettriche esenti senza limiti; ibride -50% per 5 anni"
        ),
        Regione.EMILIA_ROMAGNA to RegionalRule(
            // ER applies its own tariff ~10% above the national base
            coefficiente = 1.10,
            note = "Emilia-Romagna: tariffa regionale maggiorata (~+10%)"
        )
    )

    data class RegionalRule(
        val esenzioneElettricaPermanente: Boolean = false,
        val riduzioneIbrida: Double = 0.0,
        val anniRiduzioneIbrida: Int = 0,
        val maxKwIbrida: Int = Int.MAX_VALUE,
        val coefficiente: Double = 1.0,
        val note: String
    )

    fun getRiduzioneAlimentazione(regione: Regione, veicolo: Veicolo, base: Double): Double {
        val rule = REGIONAL_RULES[regione]
        val eta = LocalDate.now().year - veicolo.dataImmatricolazione.year

        return when (veicolo.alimentazione) {
            Alimentazione.ELETTRICA -> when {
                rule?.esenzioneElettricaPermanente == true -> base
                eta < ESENZIONE_ELETTRICA_NAZIONALE_ANNI -> base
                else -> base * (1.0 - QUOTA_ELETTRICA_DOPO_ESENZIONE)
            }
            Alimentazione.IBRIDA -> {
                if (rule != null && rule.riduzioneIbrida > 0 &&
                    eta < rule.anniRiduzioneIbrida && veicolo.kw <= rule.maxKwIbrida
                ) base * rule.riduzioneIbrida else 0.0
            }
            // No national GPL/metano reduction; regional schemes are too
            // heterogeneous to encode safely, so none is applied by default.
            else -> 0.0
        }
    }

    fun getNoteRegionali(regione: Regione): String {
        return REGIONAL_RULES[regione]?.note ?: "Nessuna agevolazione regionale specifica"
    }

    fun isEsenzioneElettricaApplicabile(regione: Regione, annoImmatricolazione: Int): Boolean {
        if (REGIONAL_RULES[regione]?.esenzioneElettricaPermanente == true) return true
        val eta = LocalDate.now().year - annoImmatricolazione
        return eta < ESENZIONE_ELETTRICA_NAZIONALE_ANNI
    }

    fun getCoefficienteRegionale(regione: Regione): Double {
        return REGIONAL_RULES[regione]?.coefficiente ?: regione.coefficiente
    }
}
