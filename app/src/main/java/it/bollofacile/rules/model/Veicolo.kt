package it.bollofacile.rules.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Veicolo(
    val id: Long = 0,
    val targa: String? = null,
    val marca: String,
    val modello: String,
    val kw: Int,
    val cv: Int,
    val classeEuro: ClasseEuro,
    val alimentazione: Alimentazione,
    val dataImmatricolazione: LocalDate,
    val regione: Regione,
    val isStorico: Boolean = false,
    val note: String = "",
    val createdAt: LocalDate = LocalDate.now(),
    val updatedAt: LocalDate = LocalDate.now()
) {
    val potenzaKw: Int get() = kw
    val potenzaCv: Int get() = cv
    
    fun isSuperbolloApplicable(): Boolean = kw > 185
    
    fun superbolloKwExcess(): Int = maxOf(0, kw - 185)
}

@Serializable
enum class ClasseEuro(val label: String, val minYear: Int, val maxYear: Int?) {
    EURO_0("Euro 0", 0, 1992),
    EURO_1("Euro 1", 1993, 1996),
    EURO_2("Euro 2", 1997, 2000),
    EURO_3("Euro 3", 2001, 2005),
    EURO_4("Euro 4", 2006, 2010),
    EURO_5("Euro 5", 2011, 2014),
    EURO_6("Euro 6", 2015, null)
}

@Serializable
enum class Alimentazione(val label: String, val bolloReduction: Double = 0.0) {
    BENZINA("Benzina", 0.0),
    DIESEL("Diesel", 0.0),
    IBRIDA("Ibrida", 0.5),      // 50% riduzione in molte regioni
    ELETTRICA("Elettrica", 1.0), // Esenzione totale (5 anni poi regionale)
    GPL("GPL", 0.75),            // 75% riduzione
    METANO("Metano", 0.75)       // 75% riduzione
}

@Serializable
enum class Regione(val codice: String, val nome: String, val coefficiente: Double = 1.0) {
    ABRUZZO("ABR", "Abruzzo", 1.0),
    BASILICATA("BAS", "Basilicata", 1.0),
    CALABRIA("CAL", "Calabria", 1.0),
    CAMPANIA("CAM", "Campania", 1.0),
    EMILIA_ROMAGNA("EMR", "Emilia-Romagna", 1.0),
    FRIULI_VENEZIA_GIULIA("FVG", "Friuli-Venezia Giulia", 1.0),
    LAZIO("LAZ", "Lazio", 1.0),
    LIGURIA("LIG", "Liguria", 1.0),
    LOMBARDIA("LOM", "Lombardia", 1.0),
    MARCHE("MAR", "Marche", 1.0),
    MOLISE("MOL", "Molise", 1.0),
    PIEMONTE("PIE", "Piemonte", 1.0),
    PUGLIA("PUG", "Puglia", 1.0),
    SARDEGNA("SAR", "Sardegna", 1.0),
    SICILIA("SIC", "Sicilia", 1.0),
    TOSCANA("TOS", "Toscana", 1.0),
    TRENTINO_ALTO_ADIGE("TAA", "Trentino-Alto Adige", 1.0),
    UMBRIA("UMB", "Umbria", 1.0),
    VALLE_D_AOSTA("VDA", "Valle d'Aosta", 1.0),
    VENETO("VEN", "Veneto", 1.0)
}

@Serializable
data class BolloResult(
    val bolloBase: Double,
    val superbollo: Double,
    val riduzioneAlimentazione: Double,
    val riduzioneStorico: Double,
    val riduzioneRegionale: Double,
    val totale: Double,
    val scadenza: LocalDate,
    val dettaglio: String
) {
    val hasSuperbollo: Boolean get() = superbollo > 0
    val isEsente: Boolean get() = totale == 0.0
}

@Serializable
data class RevisioneResult(
    val prossimaScadenza: LocalDate,
    val anniProssima: Int,
    val isStorico: Boolean,
    val note: String
)

@Serializable
data class SanzioneResult(
    val importoOriginale: Double,
    val giorniRitardo: Int,
    val sanzioneBase: Double,
    val interessi: Double,
    val ravvedimentoOperoso: Double,
    val totaleDaPagare: Double,
    val fasciaRavvedimento: String
)