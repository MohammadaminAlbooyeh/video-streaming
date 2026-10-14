package it.bollofacile.rules

import it.bollofacile.util.daysUntil
import it.bollofacile.util.maxDaysInMonth
import it.bollofacile.rules.model.*
import java.time.LocalDate

object RegioniBollo {

    // Region-specific exemptions and reductions (2026)
    private val REGIONAL_RULES = mapOf(
        Regione.LOMBARDIA to RegionalRule(
            esenzioniIbride = true,
            esenzioniElettriche = true,
            anniEsenzioneElettrica = 5,
            riduzioneIbrida = 0.5,
            note = "Lombardia: esenzione 5 anni elettriche, 50% ibride"
        ),
        Regione.PIEMONTE to RegionalRule(
            esenzioniIbride = true,
            esenzioniElettriche = true,
            anniEsenzioneElettrica = 5,
            riduzioneIbrida = 0.5,
            note = "Piemonte: esenzione 5 anni elettriche, 50% ibride"
        ),
        Regione.EMILIA_ROMAGNA to RegionalRule(
            esenzioniIbride = true,
            esenzioniElettriche = true,
            anniEsenzioneElettrica = 5,
            riduzioneIbrida = 0.5,
            note = "Emilia-Romagna: esenzione 5 anni elettriche, 50% ibride"
        ),
        Regione.VENETO to RegionalRule(
            esenzioniIbride = true,
            esenzioniElettriche = true,
            anniEsenzioneElettrica = 5,
            riduzioneIbrida = 0.5,
            note = "Veneto: esenzione 5 anni elettriche, 50% ibride"
        ),
        Regione.TOSCANA to RegionalRule(
            esenzioniIbride = true,
            esenzioniElettriche = true,
            anniEsenzioneElettrica = 5,
            riduzioneIbrida = 0.5,
            note = "Toscana: esenzione 5 anni elettriche, 50% ibride"
        ),
        Regione.LAZIO to RegionalRule(
            esenzioniIbride = false,
            esenzioniElettriche = true,
            anniEsenzioneElettrica = 3,
            riduzioneIbrida = 0.0,
            note = "Lazio: esenzione 3 anni solo elettriche"
        ),
        Regione.CAMPANIA to RegionalRule(
            esenzioniIbride = false,
            esenzioniElettriche = true,
            anniEsenzioneElettrica = 3,
            riduzioneIbrida = 0.0,
            note = "Campania: esenzione 3 anni solo elettriche"
        ),
        Regione.SICILIA to RegionalRule(
            esenzioniIbride = false,
            esenzioniElettriche = true,
            anniEsenzioneElettrica = 3,
            riduzioneIbrida = 0.0,
            note = "Sicilia: esenzione 3 anni solo elettriche"
        )
        // Other regions: national rules by default
    )

    data class RegionalRule(
        val esenzioniIbride: Boolean,
        val esenzioniElettriche: Boolean,
        val anniEsenzioneElettrica: Int,
        val riduzioneIbrida: Double,
        val note: String
    )

    fun getRiduzioneAlimentazione(regione: Regione, veicolo: Veicolo, base: Double): Double {
        val rule = REGIONAL_RULES[regione]
        val eta = LocalDate.now().year - veicolo.dataImmatricolazione.year

        return when (veicolo.alimentazione) {
            Alimentazione.ELETTRICA -> {
                if (rule != null && rule.esenzioniElettriche && eta <= rule.anniEsenzioneElettrica) base else 0.0
            }
            Alimentazione.IBRIDA -> {
                if (rule != null && rule.esenzioniIbride) base * rule.riduzioneIbrida else 0.0
            }
            Alimentazione.GPL, Alimentazione.METANO -> {
                // National 75% reduction for LPG/CNG
                base * 0.75
            }
            else -> 0.0
        }
    }

    fun getNoteRegionali(regione: Regione): String {
        return REGIONAL_RULES[regione]?.note ?: "Nessuna agevolazione regionale specifica"
    }

    fun isEsenzioneElettricaApplicabile(regione: Regione, annoImmatricolazione: Int): Boolean {
        val rule = REGIONAL_RULES[regione] ?: return false
        val eta = LocalDate.now().year - annoImmatricolazione
        return rule.esenzioniElettriche && eta <= rule.anniEsenzioneElettrica
    }

    fun getCoefficienteRegionale(regione: Regione): Double {
        return regione.coefficiente
    }
}