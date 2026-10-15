package it.bollofacile.rules

/**
 * All year-dependent fiscal values used by the rules engine, so they can be
 * updated remotely (see data/rates/RatesRepository) without an app release.
 * The defaults MUST mirror the values currently baked into the unit tests.
 */
data class RatesConfig(
    val version: Int = 1,
    // €/kW brackets keyed by minimum kW (BolloRules2026.TARIFFE_KW)
    val tariffeKw: Map<Int, Double> = mapOf(
        0 to 2.58,
        1 to 2.58,
        100 to 4.50,
        185 to 4.50,
        186 to 6.50,
        200 to 6.50
    ),
    val superbolloPerKw: Double = 20.0,
    val sogliaSuperbolloKw: Int = 185,
    // Sanzioni
    val sanzionePercentuale: Double = 0.30,
    val sanzioneMinima: Double = 25.0,
    val interesseLegaleAnnuo: Double = 0.025,
    // Ravvedimento operoso brackets: maxDays -> reduction factor applied to the base penalty
    val fasceRavvedimento: List<FasciaRates> = listOf(
        FasciaRates(14, 0.10, "Entro 14 giorni (1/10)"),
        FasciaRates(30, 0.125, "Entro 30 giorni (1/8)"),
        FasciaRates(90, 0.142857, "Entro 90 giorni (1/7)"),
        FasciaRates(365, 0.166667, "Entro 1 anno (1/6)"),
        FasciaRates(730, 0.20, "Entro 2 anni (1/5)")
    )
) {
    data class FasciaRates(
        val giorniMax: Int,
        val riduzione: Double,
        val descrizione: String
    )
}

/** Process-wide holder for the active rates. Replaced at startup by RatesRepository. */
object Rates {
    @Volatile
    var current: RatesConfig = RatesConfig()
}
