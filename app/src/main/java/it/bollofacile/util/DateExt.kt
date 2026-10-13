package it.bollofacile.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

fun LocalDate.daysUntil(other: LocalDate): Long = ChronoUnit.DAYS.between(this, other)

fun LocalDate.yearsUntil(other: LocalDate): Int = ChronoUnit.YEARS.between(this, other).toInt()

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

fun java.time.Month.maxDaysInMonth(year: Int): Int = YearMonth.of(year, this).lengthOfMonth()
