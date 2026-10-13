package it.bollofacile.rules

import it.bollofacile.util.daysUntil
import it.bollofacile.rules.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class SanzioniRulesTest {

    @Test
    fun testNoPenaltyIfOnTime() {
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2024, 6, 30)
        
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
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2024, 7, 10) // 10 giorni dopo
        
        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)
        
        assertEquals("10 days delay", 10, result.giorniRitardo)
        // Sanzione base: 30% di 150 = 45€ (min 25€)
        assertEquals("Base penalty 30%", 45.0, result.sanzioneBase, 0.01)
        // Ravvedimento 1/10 = 4.5€
        assertEquals("Ravvedimento 1/10", 4.5, result.ravvedimentoOperoso, 0.01)
        assertTrue("Total > original", result.totaleDaPagare > 150.0)
        assertEquals("Fascia 14 giorni", "Entro 14 giorni (1/10)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin30Days() {
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2024, 7, 20) // 20 giorni dopo
        
        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)
        
        assertEquals("20 days delay", 20, result.giorniRitardo)
        // Ravvedimento 1/8 = 5.625€
        assertEquals("Ravvedimento 1/8", 5.625, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia 30 giorni", "Entro 30 giorni (1/8)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin90Days() {
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2024, 9, 15) // ~77 giorni dopo
        
        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)
        
        assertEquals("~77 days delay", 77, result.giorniRitardo)
        // Ravvedimento 1/7 ≈ 6.43€
        assertEquals("Ravvedimento 1/7", 45.0 / 7.0, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia 90 giorni", "Entro 90 giorni (1/7)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin1Year() {
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2025, 3, 15) // ~258 giorni dopo
        
        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)
        
        assertTrue("~258 days delay", result.giorniRitardo > 250)
        // Ravvedimento 1/6 = 7.5€
        assertEquals("Ravvedimento 1/6", 7.5, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia 1 anno", "Entro 1 anno (1/6)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyWithin2Years() {
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2026, 1, 15) // ~564 giorni dopo
        
        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)
        
        assertTrue("~564 days delay", result.giorniRitardo > 500)
        // Ravvedimento 1/5 = 9€
        assertEquals("Ravvedimento 1/5", 9.0, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia 2 anni", "Entro 2 anni (1/5)", result.fasciaRavvedimento)
    }

    @Test
    fun testPenaltyOver2Years() {
        val scadenza = LocalDate.of(2022, 6, 30)
        val pagamento = LocalDate.of(2025, 1, 15) // ~929 giorni dopo
        
        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)
        
        assertTrue(">2 years delay", result.giorniRitardo > 730)
        // Nessun ravvedimento = sanzione piena (45€)
        assertEquals("Full penalty", 45.0, result.ravvedimentoOperoso, 0.01)
        assertEquals("Fascia oltre 2 anni", "Oltre 2 anni (sanzione piena)", result.fasciaRavvedimento)
    }

    @Test
    fun testMinimumPenalty() {
        // Importo molto basso: sanzione minima €25
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2024, 7, 10)
        
        val result = SanzioniRules.calcolaSanzione(10.0, scadenza, pagamento)
        
        // 30% di 10 = 3€, ma minimo €25
        assertEquals("Minimum penalty €25", 25.0, result.sanzioneBase, 0.01)
    }

    @Test
    fun testMaximumPenalty() {
        // Importo alto: sanzione max = importo dovuto
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2024, 7, 10)
        
        val result = SanzioniRules.calcolaSanzione(10000.0, scadenza, pagamento)
        
        // 30% di 10000 = 3000€, max = importo dovuto = 10000€
        assertEquals("Max penalty = importo dovuto", 3000.0, result.sanzioneBase, 0.01)
    }

    @Test
    fun testInterestCalculation() {
        val scadenza = LocalDate.of(2024, 6, 30)
        val pagamento = LocalDate.of(2024, 7, 30) // 30 giorni dopo
        
        val result = SanzioniRules.calcolaSanzione(150.0, scadenza, pagamento)
        
        // Interessi: 150 * 2.5% / 365 * 30 ≈ 0.31€
        val expectedInterest = 150.0 * 0.025 / 365 * 30
        assertEquals("Legal interest calculation", expectedInterest, result.interessi, 0.01)
    }

    @Test
    fun testRavvedimentoDeadlines() {
        val scadenza = LocalDate.of(2024, 6, 30)
        val scadenze = SanzioniRules.getProssimeScadenzeRavvedimento(scadenza)
        
        assertEquals("5 fasce di ravvedimento", 5, scadenze.size)
        assertEquals("14 giorni", 14, scadenze[0].dataLimite.daysUntil(scadenza.plusDays(14)).toInt())
        assertEquals("30 giorni", 30, scadenze[1].dataLimite.daysUntil(scadenza.plusDays(30)).toInt())
        assertEquals("90 giorni", 90, scadenze[2].dataLimite.daysUntil(scadenza.plusDays(90)).toInt())
        assertEquals("365 giorni", 365, scadenze[3].dataLimite.daysUntil(scadenza.plusDays(365)).toInt())
        assertEquals("730 giorni", 730, scadenze[4].dataLimite.daysUntil(scadenza.plusDays(730)).toInt())
    }
}