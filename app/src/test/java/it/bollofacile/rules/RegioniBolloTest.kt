package it.bollofacile.rules

import it.bollofacile.rules.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class RegioniBolloTest {

    @Test
    fun testLombardiaElectricExemption() {
        val veicoloElettricoRecente = Veicolo(
            marca = "Test", modello = "Test", kw = 100, cv = 136,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.ELETTRICA,
            dataImmatricolazione = LocalDate.of(2023, 1, 1), regione = Regione.LOMBARDIA
        )
        val result = BolloRules2026.calcolaBollo(veicoloElettricoRecente)
        
        // Lombardia: 5-year exemption for electric vehicles
        assertTrue("Electric 2023 in Lombardia should be exempt", result.isEsente)
    }

    @Test
    fun testLombardiaElectricOldNotExempt() {
        val veicoloElettricoVecchio = Veicolo(
            marca = "Test", modello = "Test", kw = 100, cv = 136,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.ELETTRICA,
            dataImmatricolazione = LocalDate.of(2015, 1, 1), regione = Regione.LOMBARDIA
        )
        val result = BolloRules2026.calcolaBollo(veicoloElettricoVecchio)
        
        // More than 5 years = not exempt
        assertFalse("Electric 2015 in Lombardia should not be exempt", result.isEsente)
    }

    @Test
    fun testLombardiaHybridReduction() {
        val veicoloIbrido = Veicolo(
            marca = "Test", modello = "Test", kw = 100, cv = 136,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.IBRIDA,
            dataImmatricolazione = LocalDate.of(2020, 1, 1), regione = Regione.LOMBARDIA
        )
        val result = BolloRules2026.calcolaBollo(veicoloIbrido)
        
        // Lombardia: 50% reduction for hybrids
        val expectedReduction = result.bolloBase * 0.5
        assertEquals("50% hybrid reduction in Lombardia", expectedReduction, result.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testLazioOnlyElectricExemption() {
        val veicoloIbrido = Veicolo(
            marca = "Test", modello = "Test", kw = 100, cv = 136,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.IBRIDA,
            dataImmatricolazione = LocalDate.of(2023, 1, 1), regione = Regione.LAZIO
        )
        val result = BolloRules2026.calcolaBollo(veicoloIbrido)
        
        // Lazio: no reduction for hybrids
        assertEquals("No hybrid reduction in Lazio", 0.0, result.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testLazioElectricExemption3Years() {
        val veicoloElettricoRecente = Veicolo(
            marca = "Test", modello = "Test", kw = 100, cv = 136,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.ELETTRICA,
            dataImmatricolazione = LocalDate.of(2023, 1, 1), regione = Regione.LAZIO
        )
        val result = BolloRules2026.calcolaBollo(veicoloElettricoRecente)
        
        // Lazio: 3-year exemption for electric vehicles
        assertTrue("Electric 2023 in Lazio should be exempt", result.isEsente)
    }

    @Test
    fun testDefaultRegionNoSpecialRules() {
        // Region without special rules (e.g., ABRUZZO)
        val veicoloIbrido = Veicolo(
            marca = "Test", modello = "Test", kw = 100, cv = 136,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.IBRIDA,
            dataImmatricolazione = LocalDate.of(2020, 1, 1), regione = Regione.ABRUZZO
        )
        val result = BolloRules2026.calcolaBollo(veicoloIbrido)
        
        // No special regional reduction for hybrid in Abruzzo
        assertEquals("No special regional reduction for hybrid in default region", 0.0, result.riduzioneRegionale, 0.01)
    }

    @Test
    fun testGplMethanoNationalReduction() {
        val veicoloGpl = Veicolo(
            marca = "Test", modello = "Test", kw = 100, cv = 136,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.GPL,
            dataImmatricolazione = LocalDate.of(2020, 1, 1), regione = Regione.ABRUZZO
        )
        val result = BolloRules2026.calcolaBollo(veicoloGpl)
        
        // 75% national reduction for LPG
        val expectedReduction = result.bolloBase * 0.75
        assertEquals("75% national GPL reduction", expectedReduction, result.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testCoefficienteRegionale() {
        // Test that the regional coefficient is applied
        val veicolo = Veicolo(
            marca = "Test", modello = "Test", kw = 100, cv = 136,
            classeEuro = ClasseEuro.EURO_6, alimentazione = Alimentazione.BENZINA,
            dataImmatricolazione = LocalDate.of(2020, 1, 1), regione = Regione.LOMBARDIA
        )
        val resultLombardia = BolloRules2026.calcolaBollo(veicolo)
        
        val veicoloPiemonte = veicolo.copy(regione = Regione.PIEMONTE)
        val resultPiemonte = BolloRules2026.calcolaBollo(veicoloPiemonte)
        
        // Same coefficient (1.0) = same base bollo
        assertEquals("Same coefficient should give same base", resultLombardia.bolloBase, resultPiemonte.bolloBase, 0.01)
    }
}