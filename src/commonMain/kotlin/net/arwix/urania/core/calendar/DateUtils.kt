package net.arwix.urania.core.calendar

/**
 * Returns true if the year is a leap year in the Gregorian Calendar.
 *
 * @param year Year value
 * @return true if it is a leap year, false otherwise.
 */
fun isLeapYear(year: Int): Boolean {
    var innerYear = year
    if (innerYear < 0) innerYear++
    val aux1: Int = innerYear % 4
    val aux2: Int = innerYear % 100
    val aux3: Int = innerYear % 400
    return (aux1 == 0 && (aux2 == 0 && aux3 == 0 || aux2 != 0))
}

/**
 * Returns the number of days in a given month in the Gregorian calendar.
 * @param year Year value
 * (-2 = 2 B.C. not the 'astronomical' year).
 * @param month Month first 1
 * @return Days in month.
 */
fun getDaysInMonth(year: Int, month: Int): Int {
    var n = daysPerMonth[month - 1]
    if (month == 2 && isLeapYear(year)) n++
    return n
}


/**
 * Number of days in a month for Gregorian/Julian calendars.
 */
private val daysPerMonth by lazy { intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 0) }