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
        dataImmatricolazione = LocalDate.now().minusYears(2).withMonth(6).withDayOfMonth(15),
        regione = Regione.LOMBARDIA
    )

    @Test
    fun testBolloBaseCalculation() {
        val result = BolloRules2026.calcolaBollo(veicoloStandard)

        // Euro 6, 75 kW: 75 × 2.58 = 193.50€
        assertEquals("Euro 6 tariff 2.58 €/kW", 193.50, result.bolloBase, 0.01)
        assertEquals("No superbollo for 75 kW", 0.0, result.superbollo, 0.01)
        assertEquals("Total should equal base for standard vehicle", result.bolloBase, result.totale, 0.01)
    }

    @Test
    fun testTwoTierTariffAbove100Kw() {
        // The higher rate applies only to the kW exceeding 100
        val veicolo110 = veicoloStandard.copy(kw = 110, cv = 150)
        val result = BolloRules2026.calcolaBollo(veicolo110)

        // Euro 6: 100 × 2.58 + 10 × 3.87 = 296.70€
        assertEquals("Two-tier tariff for 110 kW Euro 6", 296.70, result.bolloBase, 0.01)
    }

    @Test
    fun testEuroClassTariffs() {
        val resultEuro6 = BolloRules2026.calcolaBollo(veicoloStandard.copy(classeEuro = ClasseEuro.EURO_6))
        val resultEuro0 = BolloRules2026.calcolaBollo(veicoloStandard.copy(classeEuro = ClasseEuro.EURO_0))

        // Euro 0, 75 kW: 75 × 3.00 = 225.00€
        assertEquals("Euro 0 tariff 3.00 €/kW", 225.00, resultEuro0.bolloBase, 0.01)
        assertTrue("Euro 0 pays more than Euro 6", resultEuro0.bolloBase > resultEuro6.bolloBase)
    }

    @Test
    fun testSuperbolloCalculation() {
        val veicoloPotente = veicoloStandard.copy(kw = 200, cv = 272)
        val result = BolloRules2026.calcolaBollo(veicoloPotente)

        assertTrue("Superbollo should apply for >185 kW", result.hasSuperbollo)
        // Vehicle is 2 years old → full superbollo: 15 kW × 20€ = 300€
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
    fun testElectricVehicleExemption() {
        val veicoloElettrico = veicoloStandard.copy(alimentazione = Alimentazione.ELETTRICA)
        val result = BolloRules2026.calcolaBollo(veicoloElettrico)

        // National rule: 5-year exemption from first registration (vehicle is 2 years old)
        assertEquals("Recent EV fully exempt", result.bolloBase, result.riduzioneAlimentazione, 0.01)
        assertEquals("Total 0 for recent EV", 0.0, result.totale, 0.01)
    }

    @Test
    fun testHybridVehicleReduction() {
        // Lombardia: 50% for 5 years, hybrids ≤ 100 kW (vehicle is 2 years old, 75 kW)
        val veicoloIbrido = veicoloStandard.copy(alimentazione = Alimentazione.IBRIDA)
        val result = BolloRules2026.calcolaBollo(veicoloIbrido)

        assertEquals("50% hybrid reduction in Lombardia", result.bolloBase * 0.5, result.riduzioneAlimentazione, 0.01)

        // After 5 years no reduction applies
        val ibridoVecchio = veicoloIbrido.copy(dataImmatricolazione = LocalDate.now().minusYears(7))
        val resultVecchio = BolloRules2026.calcolaBollo(ibridoVecchio)
        assertEquals("No hybrid reduction after 5 years", 0.0, resultVecchio.riduzioneAlimentazione, 0.01)
    }

    @Test
    fun testGplMethaneNoNationalReduction() {
        // There is no national GPL/metano reduction
        val veicoloGpl = veicoloStandard.copy(alimentazione = Alimentazione.GPL)
        val result = BolloRules2026.calcolaBollo(veicoloGpl)

        assertEquals("No national GPL reduction", 0.0, result.riduzioneAlimentazione, 0.01)
        assertEquals("GPL pays the full bollo", result.bolloBase, result.totale, 0.01)
    }

    @Test
    fun testScadenzaCalculation() {
        val result = BolloRules2026.calcolaBollo(veicoloStandard)

        // Due date should be the end of the registration month (June)
        assertEquals("Scadenza should be end of June", 6, result.scadenza.monthValue)
        assertEquals("Scadenza should be last day of month", 30, result.scadenza.dayOfMonth)
    }
}
