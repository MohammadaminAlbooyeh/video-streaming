package it.bollofacile.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.bollofacile.rules.model.Alimentazione
import it.bollofacile.rules.model.ClasseEuro
import it.bollofacile.rules.model.Regione
import java.time.LocalDate

@Entity(tableName = "veicoli")
data class VeicoloEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    fun toDomain(): it.bollofacile.rules.model.Veicolo {
        return it.bollofacile.rules.model.Veicolo(
            id = id,
            targa = targa,
            marca = marca,
            modello = modello,
            kw = kw,
            cv = cv,
            classeEuro = classeEuro,
            alimentazione = alimentazione,
            dataImmatricolazione = dataImmatricolazione,
            regione = regione,
            isStorico = isStorico,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

fun it.bollofacile.rules.model.Veicolo.toEntity(): VeicoloEntity {
    return VeicoloEntity(
        id = id,
        targa = targa,
        marca = marca,
        modello = modello,
        kw = kw,
        cv = cv,
        classeEuro = classeEuro,
        alimentazione = alimentazione,
        dataImmatricolazione = dataImmatricolazione,
        regione = regione,
        isStorico = isStorico,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}