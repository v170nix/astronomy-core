@file:Suppress("unused")

package net.arwix.urania.core.calendar

import kotlinx.datetime.Instant
import net.arwix.urania.core.math.SECONDS_PER_DAY
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.roundToLong

@JvmInline
value class MJD(val value: Double) : Comparable<MJD> {

    constructor(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0,
        millisecond: Int = 0,
        isJulianDate: Boolean = false
    ) : this(getMJD(year, month, day, hour, minute, second, millisecond, isJulianDate))

    inline operator fun plus(other: MJD) = MJD(value + other.value)
    inline operator fun minus(other: MJD) = MJD(value - other.value)
    inline operator fun unaryMinus() = MJD(-value)
    inline operator fun div(other: Double): Double = value / other
    inline operator fun div(other: MJD): Double = value / other.value
    inline operator fun times(other: Double): Double = value * other
    inline operator fun times(other: MJD): Double = value * other.value

    override fun compareTo(other: MJD) = value.compareTo(other.value)

    companion object {
        val J2000 = MJD(51544.5)
        val J1970 = MJD(40587.5)

        private fun getMJD(
            year: Int,
            month: Int,
            day: Int,
            hour: Int,
            minute: Int,
            second: Int,
            millisecond: Int,
            isJulianDate: Boolean = false
        ) = run {
            var y = year
            val m = if (month <= 2) {
                --y; month + 12
            } else month
            val b: Long = if (isJulianDate) -2L + (y + 4716L) / 4 - 1179L else (y / 400L - y / 100 + y / 4)

            val mJDN = 365 * y - 679004L + b + (30.6001 * (m + 1)).toInt() + day
            val mJDF = (abs(hour) + abs(minute) / 60.0 + abs(second + millisecond / 1000.0) / 3600.0) / 24.0

            mJDN + mJDF
        }
    }

}

inline val Int.mJD: MJD get() = MJD(this.toDouble())
inline val Double.mJD: MJD get() = MJD(this)


inline operator fun Double.times(mJD: MJD): Double = mJD * this

inline fun Instant.toMJD() = MJD(epochSeconds.toDouble() / SECONDS_PER_DAY + MJD.J1970.value - 0.5)

inline fun MJD.toInstant(): Instant {
    val seconds = (this.value + 0.5 - MJD.J1970.value) * SECONDS_PER_DAY
    return Instant.fromEpochSeconds(seconds.roundToLong(), 0)
}

inline fun MJD.toJT(): JT = JT((this - MJD.J2000) / 36525.0)
inline fun Instant.toJT(): JT = toMJD().toJT()