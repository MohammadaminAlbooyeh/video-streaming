package it.bollofacile.data.db

import androidx.room.TypeConverter
import it.bollofacile.rules.model.Alimentazione
import it.bollofacile.rules.model.ClasseEuro
import it.bollofacile.rules.model.Regione
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromRegione(value: Regione?): String? = value?.codice

    @TypeConverter
    fun toRegione(value: String?): Regione? =
        value?.let { code -> Regione.values().firstOrNull { it.codice == code } }

    @TypeConverter
    fun fromAlimentazione(value: Alimentazione?): String? = value?.name

    @TypeConverter
    fun toAlimentazione(value: String?): Alimentazione? = value?.let { Alimentazione.valueOf(it) }

    @TypeConverter
    fun fromClasseEuro(value: ClasseEuro?): String? = value?.name

    @TypeConverter
    fun toClasseEuro(value: String?): ClasseEuro? = value?.let { ClasseEuro.valueOf(it) }
}
