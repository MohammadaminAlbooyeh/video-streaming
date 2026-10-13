package it.bollofacile.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

object Format {
    val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.ITALY).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
    val CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.ITALY)
    
    fun formatEuro(value: Double): String = CURRENCY_FORMAT.format(value)
    fun formatNumber(value: Double): String = NUMBER_FORMAT.format(value)
    fun formatDate(date: LocalDate): String = date.format(DATE_FORMATTER)
    fun formatDateTime(date: LocalDate): String = date.format(DATE_TIME_FORMATTER)
}

fun String.ifBlank(): String? = if (isBlank()) null else this
inline fun String.ifNotBlank(block: (String) -> Unit): String? {
    if (isNotBlank()) block(this)
    return this
}

inline fun <T> List<T>.ifNotEmpty(block: (List<T>) -> Unit): List<T>? {
    if (isNotEmpty()) block(this)
    return this
}