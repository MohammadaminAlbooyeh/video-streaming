package it.bollofacile.rules

import it.bollofacile.rules.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import java.math.BigDecimal
import java.math.RoundingMode

object BolloRules2026 {

    // Tariffe base per kW (valori 2026 - aggiornare annualmente)
    private val TARIFFE_KW = mapOf(
        0 to 2.58,
        1 to 2.58,
        // ... valori intermedi
        100 to 4.50,
        185 to 4.50,
        186 to 6.50,  // Superbollo scatta qui
        200 to 6.50
    )

    // Coefficienti per classe Euro (riduzioni)
    private val COEFFICIENTI_EURO = mapOf(
        ClasseEuro.EURO_0 to 1.0,
        ClasseEuro.EURO_1 to 1.0,
        ClasseEuro.EURO_2 to 1.0,
        ClasseEuro.EURO_3 to 0.9,
        ClasseEuro.EURO_4 to 0.8,
        ClasseEuro.EURO_5 to 0.7,
        ClasseEuro.EURO_6 to 0.6
    )

    // Superbollo: 20€ per ogni kW oltre 185
    const val SUPERBOLLO_PER_KW = 20.0
    const val SOGLIA_SUPERBOLLO_KW = 185

    fun calcolaBollo(veicolo: Veicolo, regione: Regione = veicolo.regione): BolloResult {
        // Veicoli storici (30+ anni) esenti
        val eta = YearMonth.from(veicolo.dataImmatricolazione).yearsUntil(YearMonth.from(LocalDate.now())).years
        if (veicolo.isStorico || eta >= 30) {
            return BolloResult(
                bolloBase = 0.0,
                superbollo = 0.0,
                riduzioneAlimentazione = 0.0,
                riduzioneStorico = calcolaBolloBase(veicolo, regione),
                riduzioneRegionale = 0.0,
                totale = 0.0,
                scadenza = calcolaScadenzaBollo(veicolo.dataImmatricolazione),
                dettaglio = "Veicolo storico (30+ anni) - Esente"
            )
        }

        val bolloBase = calcolaBolloBase(veicolo, regione)
        val superbollo = calcolaSuperbollo(veicolo)
        val riduzioneAlim = calcolaRiduzioneAlimentazione(veicolo, bolloBase)
        val riduzioneRegionale = calcolaRiduzioneRegionale(veicolo, regione, bolloBase)
        val riduzioneEuro = calcolaRiduzioneEuro(veicolo, bolloBase)

        val totale = (bolloBase - riduzioneAlim - riduzioneRegionale - riduzioneEuro + superbollo).coerceAtLeast(0.0)
        val scadenza = calcolaScadenzaBollo(veicolo.dataImmatricolazione)

        return BolloResult(
            bolloBase = arrotonda(bolloBase),
            superbollo = arrotonda(superbollo),
            riduzioneAlimentazione = arrotonda(riduzioneAlim),
            riduzioneStorico = 0.0,
            riduzioneRegionale = arrotonda(riduzioneRegionale + riduzioneEuro),
            totale = arrotonda(totale),
            scadenza = scadenza,
            dettaglio = buildDettaglio(veicolo, bolloBase, superbollo, riduzioneAlim, riduzioneRegionale + riduzioneEuro, totale)
        )
    }

    private fun calcolaBolloBase(veicolo: Veicolo, regione: Regione): Double {
        val kw = veicolo.kw
        val tariffaKw = TARIFFE_KW.entries
            .filter { it.key <= kw }
            .maxByOrNull { it.key }
            ?.value ?: 4.50
        
        return (kw * tariffaKw * regione.coefficiente)
    }

    private fun calcolaSuperbollo(veicolo: Veicolo): Double {
        if (veicolo.kw <= SOGLIA_SUPERBOLLO_KW) return 0.0
        val kwEccedenti = veicolo.kw - SOGLIA_SUPERBOLLO_KW
        return kwEccedenti * SUPERBOLLO_PER_KW
    }

    private fun calcolaRiduzioneAlimentazione(veicolo: Veicolo, base: Double): Double {
        return base * veicolo.alimentazione.bolloReduction
    }

    private fun calcolaRiduzioneEuro(veicolo: Veicolo, base: Double): Double {
        val coeff = COEFFICIENTI_EURO[veicolo.classeEuro] ?: 1.0
        return base * (1.0 - coeff)
    }

    private fun calcolaRiduzioneRegionale(veicolo: Veicolo, regione: Regione, base: Double): Double {
        return RegioniBollo.getRiduzioneRegionale(regione, veicolo, base)
    }

    private fun calcolaScadenzaBollo(dataImm: LocalDate): LocalDate {
        // Scadenza: ultimo giorno del mese di immatricolazione
        val annoCorrente = LocalDate.now().year
        val meseImm = dataImm.month
        return LocalDate(annoCorrente, meseImm, meseImm.maxDaysInMonth(annoCorrente))
    }

    private fun arrotonda(value: Double): Double {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    private fun buildDettaglio(
        veicolo: Veicolo,
        base: Double,
        superbollo: Double,
        ridAlim: Double,
        ridReg: Double,
        totale: Double
    ): String {
        return buildString {
            append("Bollo base: €${arrotonda(base)}")
            if (superbollo > 0) append("\nSuperbollo (${veicolo.kw - SOGLIA_SUPERBOLLO_KW} kW × €$SUPERBOLLO_PER_KW): €${arrotonda(superbollo)}")
            if (ridAlim > 0) append("\nRiduzione ${veicolo.alimentazione.label}: -€${arrotonda(ridAlim)}")
            if (ridReg > 0) append("\nRiduzioni classe Euro/Regionali: -€${arrotonda(ridReg)}")
            append("\nTotale: €${arrotonda(totale)}")
        }
    }
}