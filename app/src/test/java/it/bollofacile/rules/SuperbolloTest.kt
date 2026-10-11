package it.bollofacile.rules

import it.bollofacile.rules.model.*
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Test

class SuperbolloTest {

    @Test
    fun testSuperbolloThreshold() {
        // Esattamente 185 kW = no superbollo
        val veicolo185 = Veicolo(
            marca = "Test", modello = "Test", kw = 185, cv = 251,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.BENZINA,
            dataImmatricolazione = LocalDate(2020, 1, 1), regione = Regione.LOMBARDIA
        )
        val result185 = BolloRules2026.calcolaBollo(veicolo185)
        assertFalse("185 kW should not trigger superbollo", result185.hasSuperbollo)
        
        // 186 kW = superbollo
        val veicolo186 = veicolo185.copy(kw = 186)
        val result186 = BolloRules2026.calcolaBollo(veicolo186)
        assertTrue("186 kW should trigger superbollo", result186.hasSuperbollo)
        assertEquals("1 kW excess × 20€ = 20€", 20.0, result186.superbollo, 0.01)
    }

    @Test
    fun testSuperbolloCalculation() {
        val testCases = listOf(
            186 to 20.0,   // 1 kW × 20€
            190 to 100.0,  // 5 kW × 20€
            200 to 300.0,  // 15 kW × 20€
            250 to 1300.0, // 65 kW × 20€
            300 to 2300.0  // 115 kW × 20€
        )
        
        testCases.forEach { (kw, expectedSuperbollo) ->
            val veicolo = Veicolo(
                marca = "Test", modello = "Test", kw = kw, cv = (kw * 1.36).toInt(),
                classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.BENZINA,
                dataImmatricolazione = LocalDate(2020, 1, 1), regione = Regione.LOMBARDIA
            )
            val result = BolloRules2026.calcolaBollo(veicolo)
            assertEquals("Superbollo for $kw kW", expectedSuperbollo, result.superbollo, 0.01)
        }
    }

    @Test
    fun testSuperbolloWithReductions() {
        // Superbollo non viene ridotto da alimentazione o classe Euro
        val veicoloPotenteElettrico = Veicolo(
            marca = "Test", modello = "Test", kw = 200, cv = 272,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.ELETTRICA,
            dataImmatricolazione = LocalDate(2023, 1, 1), regione = Regione.LOMBARDIA
        )
        val result = BolloRules2026.calcolaBollo(veicoloPotenteElettrico)
        
        // Superbollo intero (300€) + bollo base ridotto
        assertEquals("Superbollo should be full 300€", 300.0, result.superbollo, 0.01)
        assertTrue("Base should be reduced for electric", result.riduzioneAlimentazione > 0)
        assertTrue("Total should include full superbollo", result.totale >= result.superbollo)
    }

    @Test
    fun testIsSuperbolloApplicable() {
        val veicoloNormale = Veicolo(
            marca = "Test", modello = "Test", kw = 150, cv = 204,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.BENZINA,
            dataImmatricolazione = LocalDate(2020, 1, 1), regione = Regione.LOMBARDIA
        )
        assertFalse("150 kW should not be superbollo", veicoloNormale.isSuperbolloApplicable())
        assertEquals("0 kW excess", 0, veicoloNormale.superbolloKwExcess())
        
        val veicoloPotente = veicoloNormale.copy(kw = 200)
        assertTrue("200 kW should be superbollo", veicoloPotente.isSuperbolloApplicable())
        assertEquals("15 kW excess", 15, veicoloPotente.superbolloKwExcess())
    }
}