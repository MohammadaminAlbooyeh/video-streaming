package it.bollofacile.rules

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

/**
 * Post-reform rules (D.Lgs. 87/2024, violations from 1 Sept 2024):
 * base penalty 25%; ravvedimento 0.0833%/day within 14 days, then
 * 1.25% / 1.39% / 3.125% / 3.571% / 4.167%; legal interest 1.60% (2026).
 */
class SanzioniRulesTest {

    @Test
    fun testNoPenaltyIfOnTime() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val pagamento = LocalDate.of(2026, 6, 30)

        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)

        assertEquals("0 days delay", 0, result.giorniRitardo)
        assertEquals("No penalty", 0.0, result.sanzioneBase, 0.01)
        assertEquals("No interest", 0.0, result.interessi, 0.01)
        assertEquals("No ravvedimento", 0.0, result.ravvedimentoOperoso, 0.01)
        assertEquals("Total = original amount", 150.0, result.totaleDaPagare, 0.01)
        assertEquals("In rule", "In regola", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin14Days() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val pagamento = LocalDate.of(2026, 7, 10) // 10 days later

        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)

        assertEquals("10 days delay", 10, result.giorniRitardo)
        // Full penalty would be 25% of 150 = 37.50€
        assertEquals("Base penalty 25%", 37.50, result.sanzioneBase, 0.01)
        // Ravvedimento sprint: 0.0833%/day × 10 days = 0.833% of 150 ≈ 1.25€
        assertEquals("Ravvedimento 0,0833%/day", 150.0 * 0.000833 * 10, result.ravvedimentoOperoso, 0.01)
        assertTrue("Total > original", result.totaleDaPagare > 150.0)
        assertEquals("Fascia 14 giorni", "Entro 14 giorni (0,0833%/giorno)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin30Days() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val pagamento = LocalDate.of(2026, 7, 20) // 20 days later

        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)

        assertEquals("20 days delay", 20, result.giorniRitardo)
        // 1.25% of 150 = 1.875€
        assertEquals("Ravvedimento 1,25%", 1.875, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia 30 giorni", "Entro 30 giorni (1,25%)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin90Days() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val pagamento = LocalDate.of(2026, 9, 15) // 77 days later

        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)

        assertEquals("77 days delay", 77, result.giorniRitardo)
        // 1.39% of 150 = 2.085€
        assertEquals("Ravvedimento 1,39%", 150.0 * 0.0139, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia 90 giorni", "Entro 90 giorni (1,39%)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin1Year() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val pagamento = LocalDate.of(2027, 3, 15)

        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)

        assertTrue("~258 days delay", result.giorniRitardo in 251..365)
        // 3.125% of 150 = 4.6875€
        assertEquals("Ravvedimento 3,125%", 150.0 * 0.03125, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia 1 anno", "Entro 1 anno (3,125%)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin2Years() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val pagamento = LocalDate.of(2028, 1, 15)

        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)

        assertTrue("~564 days delay", result.giorniRitardo in 366..730)
        // 3.571% of 150 ≈ 5.36€
        assertEquals("Ravvedimento 3,571%", 150.0 * 0.03571, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia 2 anni", "Entro 2 anni (3,571%)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyOver2Years() {
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2027, 1, 15)

        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)

        assertTrue(">2 years delay", result.giorniRitardo > 730)
        // 4.167% of 150 ≈ 6.25€
        assertEquals("Ravvedimento 4,167%", 150.0 * 0.04167, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia oltre 2 anni", "Oltre 2 anni (4,167%)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyProportionalToAmount() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val pagamento = LocalDate.of(2026, 7, 20) // 20 days: 1.25%

        val small = SanzioniRules.calcolaSanzione(10.0, scadenza, pagamento)
        val large = SanzioniRules.calcolaSanzione(10000.0, scadenza, pagamento)

        assertEquals("Base penalty 25% of 10", 2.50, small.sanzioneBase, 0.01)
        assertEquals("Base penalty 25% of 10000", 2500.0, large.sanzioneBase, 0.01)
        assertEquals("Ravvedimento 1,25% of 10", 10.0 * 0.0125, small.ravvedimentoOperoso, 0.01)
        assertEquals("Ravvedimento 1,25% of 10000", 10000.0 * 0.0125, large.ravvedimentoOperoso, 0.01)
    }

    @Test
    fun testInterestCalculation() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val pagamento = LocalDate.of(2026, 7, 30) // 30 days later

        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)

        // Interest: 150 × 1.60% / 365 × 30 ≈ 0.20€
        val expectedInterest = 150.0 * 0.016 / 365 * 30
        assertEquals("Legal interest calculation", expectedInterest, result.interessi, 0.01)
    }

    @Test
    fun testRavvedimentoDeadlines() {
        val scadenza = LocalDate.of(2026, 6, 30)
        val scadenze = SanzioniRules.getProssimeScadenzeRavvedimento(scadenza)

        assertEquals("5 fasce di ravvedimento", 5, scadenze.size)
        assertEquals("14 giorni", scadenza.plusDays(14), scadenze[0].dataLimite)
        assertEquals("30 giorni", scadenza.plusDays(30), scadenze[1].dataLimite)
        assertEquals("90 giorni", scadenza.plusDays(90), scadenze[2].dataLimite)
        assertEquals("365 giorni", scadenza.plusDays(365), scadenze[3].dataLimite)
        assertEquals("730 giorni", scadenza.plusDays(730), scadenze[4].dataLimite)
    }
}
