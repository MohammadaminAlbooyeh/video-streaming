package it.bollofacile.rules

import it.bollofacile.rules.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class RegioniBolloTest {

    private fun veicolo(
        alimentazione: Alimentazione,
        regione: Regione,
        anni: Long,
        kw: Int = 100
    ) = Veicolo(
        marca = "Test", modello = "Test", kw = kw, cv = (kw * 1.36).toInt(),
        classeEuro = ClasseEuro.EURO_6, alimentazione = alimentazione,
        dataImmatricolazione = LocalDate.now().minusYears(anni), regione = regione
    )

    @Test
    fun testNationalElectricExemption5Years() {
        // National rule (L. 449/1997): every region exempts EVs for 5 years
        val result = BolloRules2026.calcolaBollo(veicolo(Alimentazione.ELETTRICA, Regione.ABRUZZO, anni = 2))
        assertTrue("Recent EV exempt in any region", result.isEsente)
    }

    @Test
    fun testElectricAfter5YearsPays25Percent() {
        // After the 5-year exemption, EVs pay 25% of the tariff (75% reduction)
        val result = BolloRules2026.calcolaBollo(veicolo(Alimentazione.ELETTRICA, Regione.LAZIO, anni = 7))
        assertFalse("Old EV not fully exempt", result.isEsente)
        assertEquals("75% reduction after exemption", result.bolloBase * 0.75, result.riduzioneAlimentazione, 0.01)
        assertEquals("Pays 25% of tariff", result.bolloBase * 0.25, result.totale, 0.01)
    }

    @Test
    fun testLombardiaElectricPermanentExemption() {
        // Lombardia extends the EV exemption with no time limit
        val result = BolloRules2026.calcolaBollo(veicolo(Alimentazione.ELETTRICA, Regione.LOMBARDIA, anni = 10))
        assertTrue("EV in Lombardia exempt even after 5 years", result.isEsente)
    }

    @Test
    fun testPiemonteElectricPermanentExemption() {
        val result = BolloRules2026.calcolaBollo(veicolo(Alimentazione.ELETTRICA, Regione.PIEMONTE, anni = 10))
        assertTrue("EV in Piemonte exempt even after 5 years", result.isEsente)
    }

    @Test
    fun testLombardiaHybridReduction() {
        // Lombardia: 50% for 5 years, only hybrids ≤ 100 kW
        val recente = BolloRules2026.calcolaBollo(veicolo(Alimentazione.IBRIDA, Regione.LOMBARDIA, anni = 2))
        assertEquals("50% hybrid reduction in Lombardia", recente.bolloBase * 0.5, recente.riduzioneAlimentazione, 0.01)

        val potente = BolloRules2026.calcolaBollo(veicolo(Alimentazione.IBRIDA, Regione.LOMBARDIA, anni = 2, kw = 120))
        assertEquals("No reduction for hybrids above 100 kW", 0.0, potente.riduzioneAlimentazione, 0.01)

        val vecchio = BolloRules2026.calcolaBollo(veicolo(Alimentazione.IBRIDA, Regione.LOMBARDIA, anni = 7))
        assertEquals("No reduction after 5 years", 0.0, vecchio.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testLazioNoHybridReduction() {
        val result = BolloRules2026.calcolaBollo(veicolo(Alimentazione.IBRIDA, Regione.LAZIO, anni = 2))
        assertEquals("No hybrid reduction in Lazio", 0.0, result.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testGplMethanoNoNationalReduction() {
        val result = BolloRules2026.calcolaBollo(veicolo(Alimentazione.GPL, Regione.ABRUZZO, anni = 2))
        assertEquals("No national GPL reduction", 0.0, result.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testCoefficienteRegionale() {
        val lombardia = BolloRules2026.calcolaBollo(veicolo(Alimentazione.BENZINA, Regione.LOMBARDIA, anni = 2))
        val piemonte = BolloRules2026.calcolaBollo(veicolo(Alimentazione.BENZINA, Regione.PIEMONTE, anni = 2))
        val emiliaRomagna = BolloRules2026.calcolaBollo(veicolo(Alimentazione.BENZINA, Regione.EMILIA_ROMAGNA, anni = 2))

        // Same national base (100 kW Euro 6 = 258€) in regions with coefficient 1.0
        assertEquals("Same base in LOM and PIE", lombardia.bolloBase, piemonte.bolloBase, 0.01)
        // Emilia-Romagna applies ~+10%
        assertEquals("Emilia-Romagna +10%", lombardia.bolloBase * 1.10, emiliaRomagna.bolloBase, 0.01)
    }
}
