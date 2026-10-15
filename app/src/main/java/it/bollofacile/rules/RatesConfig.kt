package it.bollofacile.rules

/**
 * All year-dependent fiscal values used by the rules engine, so they can be
 * updated remotely (see data/rates/RatesRepository) without an app release.
 *
 * Defaults encode the national 2026 rules:
 * - Bollo: €/kW by Euro class, two-tier — the higher rate applies only to the
 *   kW exceeding 100 (e.g. 110 kW Euro 6 = 100×2.58 + 10×3.87 = €296.70).
 * - Superbollo: €20/kW over 185 kW, reduced with vehicle age
 *   (D.L. 138/2011: 60% after 5 years, 30% after 10, 15% after 15, 0 after 20).
 * - Sanzioni: 25% base penalty (D.Lgs. 87/2024, violations from 1 Sept 2024)
 *   with the reformed ravvedimento operoso brackets.
 * - Legal interest: 1.60% per year from 1 Jan 2026.
 */
data class RatesConfig(
    val version: Int = 3,
    /** €/kW per Euro class: [rate up to 100 kW, rate for each kW above 100]. Key = ClasseEuro.name. */
    val tariffeEuro: Map<String, TariffaEuro> = mapOf(
        "EURO_0" to TariffaEuro(3.00, 4.50),
        "EURO_1" to TariffaEuro(2.90, 4.35),
        "EURO_2" to TariffaEuro(2.80, 4.20),
        "EURO_3" to TariffaEuro(2.70, 4.05),
        "EURO_4" to TariffaEuro(2.58, 3.87),
        "EURO_5" to TariffaEuro(2.58, 3.87),
        "EURO_6" to TariffaEuro(2.58, 3.87)
    ),
    val sogliaTariffaKw: Int = 100,
    // Superbollo
    val superbolloPerKw: Double = 20.0,
    val sogliaSuperbolloKw: Int = 185,
    /** Fraction of the superbollo still due, by minimum vehicle age in years (descending). */
    val superbolloRiduzioneEta: List<FasciaEta> = listOf(
        FasciaEta(20, 0.0),
        FasciaEta(15, 0.15),
        FasciaEta(10, 0.30),
        FasciaEta(5, 0.60)
    ),
    // Sanzioni (violations from 1 Sept 2024)
    val sanzionePercentuale: Double = 0.25,
    val interesseLegaleAnnuo: Double = 0.016,
    /**
     * Ravvedimento operoso: reduced penalty as a fraction of the unpaid tax.
     * Within 14 days the penalty is per-day (riduzione × days of delay).
     */
    val fasceRavvedimento: List<FasciaRates> = listOf(
        FasciaRates(14, 0.000833, "Entro 14 giorni (0,0833%/giorno)", perGiorno = true),
        FasciaRates(30, 0.0125, "Entro 30 giorni (1,25%)"),
        FasciaRates(90, 0.0139, "Entro 90 giorni (1,39%)"),
        FasciaRates(365, 0.03125, "Entro 1 anno (3,125%)"),
        FasciaRates(730, 0.03571, "Entro 2 anni (3,571%)")
    ),
    /** Penalty fraction beyond the last bracket (before any formal assessment). */
    val sanzioneOltreUltimaFascia: Double = 0.04167
) {
    data class TariffaEuro(val fino100: Double, val oltre100: Double)
    data class FasciaEta(val etaMinAnni: Int, val quota: Double)
    data class FasciaRates(
        val giorniMax: Int,
        val riduzione: Double,
        val descrizione: String,
        val perGiorno: Boolean = false
    )
}

/** Process-wide holder for the active rates. Replaced at startup by RatesRepository. */
object Rates {
    @Volatile
    var current: RatesConfig = RatesConfig()
}
