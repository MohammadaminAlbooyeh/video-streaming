package it.bollofacile.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

fun LocalDate.daysUntil(other: LocalDate): Long = 
    this.periodUntil(other, kotlinx.datetime.DateTimePeriod.Unit.Days).days

fun LocalDate.yearsUntil(other: LocalDate): Int = 
    this.periodUntil(other, kotlinx.datetime.DateTimePeriod.Unit.Years).years

fun LocalDate.isToday(): Boolean = this == LocalDate.now()

fun LocalDate.isPast(): Boolean = this < LocalDate.now()

fun LocalDate.isFuture(): Boolean = this > LocalDate.now()

fun LocalDate.monthName(): String = this.month.toString()

fun LocalDate.yearMonth(): YearMonth = YearMonth.from(this)

fun Int.daysAgo(): LocalDate = LocalDate.now().minusDays(this.toLong())

fun Int.daysFromNow(): LocalDate = LocalDate.now().plusDays(this.toLong())

fun Int.monthsAgo(): LocalDate = LocalDate.now().minusMonths(this.toLong())

fun Int.monthsFromNow(): LocalDate = LocalDate.now().plusMonths(this.toLong())

fun Int.yearsAgo(): LocalDate = LocalDate.now().minusYears(this.toLong())

fun Int.yearsFromNow(): LocalDate = LocalDate.now().plusYears(this.toLong())