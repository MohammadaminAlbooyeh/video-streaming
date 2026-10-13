package it.bollofacile.rules

import it.bollofacile.rules.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class BolloRulesTest {

    private val veicoloStandard = Veicolo(
        marca = "Fiat",
        modello = "500",
        kw = 75,
        cv = 102,
        classeEuro = ClasseEuro.EURO_6,
        alimentazione = Alimentazione.BENZINA,
        dataImmatricolazione = LocalDate.of(2020, 6, 15),
        regione = Regione.LOMBARDIA
    )

    @Test
    fun testBolloBaseCalculation() {
        val result = BolloRules2026.calcolaBollo(veicoloStandard)
        
        assertTrue("Bollo base should be positive", result.bolloBase > 0)
        assertEquals("No superbollo for 75 kW", 0.0, result.superbollo, 0.01)
        assertEquals("Total should equal base for standard vehicle", result.bolloBase, result.totale, 0.01)
    }

    @Test
    fun testSuperbolloCalculation() {
        val veicoloPotente = veicoloStandard.copy(kw = 200, cv = 272)
        val result = BolloRules2026.calcolaBollo(veicoloPotente)
        
        assertTrue("Superbollo should apply for >185 kW", result.hasSuperbollo)
        assertEquals("Superbollo: 15 kW × 20€ = 300€", 300.0, result.superbollo, 0.01)
        assertTrue("Total should include superbollo", result.totale > result.bolloBase)
    }

    @Test
    fun testHistoricVehicleExempt() {
        val veicoloStorico = veicoloStandard.copy(
            dataImmatricolazione = LocalDate.of(1990, 1, 1),
            isStorico = true
        )
        val result = BolloRules2026.calcolaBollo(veicoloStorico)
        
        assertTrue("Historic vehicle should be exempt", result.isEsente)
        assertEquals("Total should be 0", 0.0, result.totale, 0.01)
    }

    @Test
    fun testElectricVehicleReduction() {
        val veicoloElettrico = veicoloStandard.copy(
            alimentazione = Alimentazione.ELETTRICA,
            dataImmatricolazione = LocalDate.of(2023, 1, 1)
        )
        val result = BolloRules2026.calcolaBollo(veicoloElettrico)
        
        // Lombardia: esenzione 5 anni per elettriche
        assertTrue("Electric vehicle in Lombardia (2023) should have reduction", result.riduzioneAlimentazione > 0)
    }

    @Test
    fun testHybridVehicleReduction() {
        val veicoloIbrido = veicoloStandard.copy(alimentazione = Alimentazione.IBRIDA)
        val result = BolloRules2026.calcolaBollo(veicoloIbrido)
        
        // Lombardia: 50% riduzione per ibride
        assertTrue("Hybrid should have 50% reduction in Lombardia", result.riduzioneAlimentazione > 0)
        val expectedReduction = result.bolloBase * 0.5
        assertEquals("50% reduction", expectedReduction, result.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testGplMethaneReduction() {
        val veicoloGpl = veicoloStandard.copy(alimentazione = Alimentazione.GPL)
        val result = BolloRules2026.calcolaBollo(veicoloGpl)
        
        // 75% riduzione nazionale per GPL/Metano
        assertTrue("GPL should have 75% reduction", result.riduzioneAlimentazione > 0)
        val expectedReduction = result.bolloBase * 0.75
        assertEquals("75% reduction", expectedReduction, result.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testEuroClassReduction() {
        val veicoloEuro6 = veicoloStandard.copy(classeEuro = ClasseEuro.EURO_6)
        val veicoloEuro0 = veicoloStandard.copy(classeEuro = ClasseEuro.EURO_0)
        
        val resultEuro6 = BolloRules2026.calcolaBollo(veicoloEuro6)
        val resultEuro0 = BolloRules2026.calcolaBollo(veicoloEuro0)
        
        assertTrue("Euro 6 should have more reduction than Euro 0", 
            resultEuro6.riduzioneRegionale >= resultEuro0.riduzioneRegionale)
    }

    @Test
    fun testScadenzaCalculation() {
        val result = BolloRules2026.calcolaBollo(veicoloStandard)
        
        // Scadenza dovrebbe essere fine mese di immatricolazione (giugno)
        assertEquals("Scadenza should be end of June", 6, result.scadenza.monthValue)
        assertEquals("Scadenza should be last day of month", 30, result.scadenza.dayOfMonth)
    }
}