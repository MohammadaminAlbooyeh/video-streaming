package it.bollofacile.data.db.converters

import androidx.room.TypeConverter
import it.bollofacile.rules.model.Alimentazione
import it.bollofacile.rules.model.ClasseEuro
import it.bollofacile.rules.model.Regione
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormatter
import kotlinx.datetime.format.DateTimeFormatterComposer

class Converters {

    private val localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(localDateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, localDateFormatter) }
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(localDateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, localDateTimeFormatter) }
    }

    @TypeConverter
    fun fromRegione(value: Regione?): String? {
        return value?.codice
    }

    @TypeConverter
    fun toRegione(value: String?): Regione? {
        return value?.let { Regione.values().firstOrNull { it.codice == it } }
    }

    @TypeConverter
    fun fromAlimentazione(value: Alimentazione?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAlimentazione(value: String?): Alimentazione? {
        return value?.let { Alimentazioni.valueOf(it) }
    }

    @TypeConverter
    fun fromClasseEuro(value: ClasseEuro?): String? {
        return value?.name
    }

    @TypeConverter
    fun toClasseEuro(value: String?): ClasseEuro? {
        return value?.let { ClasseEuro.valueOf(it) }
    }
}