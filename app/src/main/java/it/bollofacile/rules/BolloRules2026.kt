package it.bollofacile.rules

import it.bollofacile.util.daysUntil
import it.bollofacile.util.maxDaysInMonth
import it.bollofacile.rules.model.*
import java.time.LocalDate
import java.time.YearMonth
import java.math.BigDecimal
import java.math.RoundingMode

object BolloRules2026 {

    // Year-dependent values live in Rates.current so they can be updated remotely.
    private val TARIFFE_KW: Map<Int, Double> get() = Rates.current.tariffeKw

    val SUPERBOLLO_PER_KW: Double get() = Rates.current.superbolloPerKw
    val SOGLIA_SUPERBOLLO_KW: Int get() = Rates.current.sogliaSuperbolloKw

    fun calcolaBollo(veicolo: Veicolo, regione: Regione = veicolo.regione): BolloResult {
        // Historic vehicles (30+ years) are exempt
        val eta = YearMonth.from(veicolo.dataImmatricolazione).until(YearMonth.from(LocalDate.now()), java.time.temporal.ChronoUnit.YEARS).toInt()
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
        val riduzioneAlim = RegioniBollo.getRiduzioneAlimentazione(regione, veicolo, bolloBase)

        // The superbollo is not subject to reductions: it is added in full to the reduced bollo
        val totale = (bolloBase - riduzioneAlim).coerceAtLeast(0.0) + superbollo
        val scadenza = calcolaScadenzaBollo(veicolo.dataImmatricolazione)

        return BolloResult(
            bolloBase = arrotonda(bolloBase),
            superbollo = arrotonda(superbollo),
            riduzioneAlimentazione = arrotonda(riduzioneAlim),
            riduzioneStorico = 0.0,
            riduzioneRegionale = 0.0,
            totale = arrotonda(totale),
            scadenza = scadenza,
            dettaglio = buildDettaglio(veicolo, bolloBase, superbollo, riduzioneAlim, 0.0, totale)
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

    private fun calcolaScadenzaBollo(dataImm: LocalDate): LocalDate {
        // Due date: last day of the registration month
        val annoCorrente = LocalDate.now().year
        val meseImm = dataImm.month
        return LocalDate.of(annoCorrente, meseImm, meseImm.maxDaysInMonth(annoCorrente))
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