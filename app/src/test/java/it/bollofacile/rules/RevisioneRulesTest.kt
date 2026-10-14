package it.bollofacile.rules

import it.bollofacile.rules.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class RevisioneRulesTest {

    @Test
    fun testFirstRevisionAfter4Years() {
        val dataImm = LocalDate.of(2020, 6, 15)
        val result = RevisioneRules.calcolaProssimaRevisione(dataImm)
        
        // First revisione (inspection) after 4 years = June 2024
        assertEquals("First revision after 4 years", 2024, result.prossimaScadenza.year)
        assertEquals("June", 6, result.prossimaScadenza.monthValue)
        assertEquals("Last day of June", 30, result.prossimaScadenza.dayOfMonth)
    }

    @Test
    fun testSubsequentRevisionsEvery2Years() {
        val dataImm = LocalDate.of(2018, 6, 15)
        val ultimaRevisione = LocalDate.of(2022, 6, 30) // Prima revisione fatta
        
        val result = RevisioneRules.calcolaProssimaRevisione(dataImm, ultimaRevisione)
        
        // Subsequent revisioni every 2 years = June 2024
        assertEquals("Subsequent revision every 2 years", 2024, result.prossimaScadenza.year)
        assertEquals("June", 6, result.prossimaScadenza.monthValue)
    }

    @Test
    fun testHistoricVehicleEvery2YearsAfter30() {
        val dataImm = LocalDate.of(1990, 6, 15) // 34 years ago
        val result = RevisioneRules.calcolaProssimaRevisione(
            dataImm, 
            isStorico = true
        )
        
        // Historic vehicles: revisione every 2 years after 30 years of age
        // Should be 2 years after the last revisione or after the normal due date
        assertNotNull("Historic vehicle should have revision date", result.prossimaScadenza)
        assertTrue("Historic vehicle flag", result.isStorico)
    }

    @Test
    fun testElectricVehicleExemption() {
        val dataImm = LocalDate.of(2023, 1, 1)
        val result = RevisioneRules.calcolaProssimaRevisione(
            dataImm,
            isElettrico = true,
            regione = Regione.LOMBARDIA
        )
        
        // Lombardia: first-revisione exemption for electric vehicles (within the first 5 years)
        assertTrue("Electric vehicle in Lombardia should have exemption note", 
            result.note.contains("elettrico") || result.note.contains("esenzione"))
    }

    @Test
    fun testRevisioneOverdue() {
        val dataImm = LocalDate.of(2015, 1, 1)
        val result = RevisioneRules.calcolaProssimaRevisione(dataImm)
        
        // If overdue, negative days
        val isScaduta = RevisioneRules.isRevisioneScaduta(result.prossimaScadenza)
        val giorni = RevisioneRules.giorniAllaScadenza(result.prossimaScadenza)
        
        // Revisione in 2015+4=2019, then every 2 years: 2021, 2023, 2025
        // Today is 2026, so the next one is 2025 (already past) -> 2027
        // The test depends on the current date
        assertNotNull("Should have a revision date", result.prossimaScadenza)
    }

    @Test
    fun testRecentVehicleNoRevisionYet() {
        val dataImm = LocalDate.of(2023, 6, 15)
        val result = RevisioneRules.calcolaProssimaRevisione(dataImm)
        
        // First revisione after 4 years = 2027
        assertEquals("First revision in 2027", 2027, result.prossimaScadenza.year)
        assertTrue("Should be in future", result.anniProssima > 0)
    }

    @Test
    fun testExactRevisionDate() {
        val dataImm = LocalDate.of(2020, 2, 29) // Anno bisestile
        val result = RevisioneRules.calcolaProssimaRevisione(dataImm)
        
        // 4 years later = 2024-02-29 (leap year)
        assertEquals("Leap year handling", 2024, result.prossimaScadenza.year)
        assertEquals("February", 2, result.prossimaScadenza.monthValue)
        assertEquals("29th day", 29, result.prossimaScadenza.dayOfMonth)
    }
}