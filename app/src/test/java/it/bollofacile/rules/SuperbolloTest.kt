package it.bollofacile.rules

import it.bollofacile.rules.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class SuperbolloTest {

    private fun veicolo(kw: Int, anni: Long = 2) = Veicolo(
        marca = "Test", modello = "Test", kw = kw, cv = (kw * 1.36).toInt(),
        classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.BENZINA,
        dataImmatricolazione = LocalDate.now().minusYears(anni), regione = Regione.LOMBARDIA
    )

    @Test
    fun testSuperbolloThreshold() {
        // Exactly 185 kW = no superbollo
        val result185 = BolloRules2026.calcolaBollo(veicolo(185))
        assertFalse("185 kW should not trigger superbollo", result185.hasSuperbollo)

        // 186 kW = superbollo
        val result186 = BolloRules2026.calcolaBollo(veicolo(186))
        assertTrue("186 kW should trigger superbollo", result186.hasSuperbollo)
        assertEquals("1 kW excess × 20€ = 20€", 20.0, result186.superbollo, 0.01)
    }

    @Test
    fun testSuperbolloCalculation() {
        // Vehicles under 5 years of age pay the full superbollo
        val testCases = listOf(
            186 to 20.0,   // 1 kW × 20€
            190 to 100.0,  // 5 kW × 20€
            200 to 300.0,  // 15 kW × 20€
            250 to 1300.0, // 65 kW × 20€
            300 to 2300.0  // 115 kW × 20€
        )

        testCases.forEach { (kw, expectedSuperbollo) ->
            val result = BolloRules2026.calcolaBollo(veicolo(kw))
            assertEquals("Superbollo for $kw kW", expectedSuperbollo, result.superbollo, 0.01)
        }
    }

    @Test
    fun testSuperbolloAgeReduction() {
        // D.L. 138/2011: 60% after 5 years, 30% after 10, 15% after 15, 0 after 20
        val base = 15 * 20.0 // 200 kW → 15 kW excess, full = 300€

        assertEquals("Full under 5 years", base, BolloRules2026.calcolaBollo(veicolo(200, anni = 3)).superbollo, 0.01)
        assertEquals("60% from 5 years", base * 0.60, BolloRules2026.calcolaBollo(veicolo(200, anni = 7)).superbollo, 0.01)
        assertEquals("30% from 10 years", base * 0.30, BolloRules2026.calcolaBollo(veicolo(200, anni = 12)).superbollo, 0.01)
        assertEquals("15% from 15 years", base * 0.15, BolloRules2026.calcolaBollo(veicolo(200, anni = 17)).superbollo, 0.01)
        assertEquals("Zero from 20 years", 0.0, BolloRules2026.calcolaBollo(veicolo(200, anni = 21)).superbollo, 0.01)
    }

    @Test
    fun testSuperbolloWithReductions() {
        // Superbollo is not reduced by fuel type (recent EV: base exempt, superbollo due)
        val elettricoPotente = veicolo(200).copy(alimentazione = Alimentazione.ELETTRICA)
        val result = BolloRules2026.calcolaBollo(elettricoPotente)

        assertEquals("Superbollo should be full 300€", 300.0, result.superbollo, 0.01)
        assertTrue("Base should be reduced for electric", result.riduzioneAlimentazione > 0)
        assertTrue("Total should include full superbollo", result.totale >= result.superbollo)
    }

    @Test
    fun testIsSuperbolloApplicable() {
        val veicoloNormale = veicolo(150)
        assertFalse("150 kW should not be superbollo", veicoloNormale.isSuperbolloApplicable())
        assertEquals("0 kW excess", 0, veicoloNormale.superbolloKwExcess())

        val veicoloPotente = veicoloNormale.copy(kw = 200)
        assertTrue("200 kW should be superbollo", veicoloPotente.isSuperbolloApplicable())
        assertEquals("15 kW excess", 15, veicoloPotente.superbolloKwExcess())
    }
}
