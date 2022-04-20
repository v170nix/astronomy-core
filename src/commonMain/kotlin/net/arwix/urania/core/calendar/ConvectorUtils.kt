@file:Suppress("unused")

package net.arwix.urania.core.calendar

import kotlinx.datetime.Instant
import net.arwix.urania.core.math.SECONDS_PER_DAY
import kotlin.math.roundToLong

inline fun Instant.toMJD(useDeltaT: Boolean = true): MJD {
    return MJD(toEpochMilliseconds().toDouble() / SECONDS_PER_DAY / 1000.0 + MJD.J1970.value - 0.5).let {
        if (useDeltaT) it + (it.getDeltaTTUT1() / SECONDS_PER_DAY).mJD else it
    }
}
inline fun Instant.toJT(useDeltaT: Boolean = true): JT = toMJD(useDeltaT).toJT()

inline fun MJD.toInstant(useDeltaT: Boolean = true): Instant {
    val milliseconds = (this.value + 0.5 - MJD.J1970.value) * SECONDS_PER_DAY * 1000.0
    val deltaT = if (useDeltaT) this.getDeltaTTUT1() * 1000.0 else 0.0
    return Instant.fromEpochMilliseconds((milliseconds - deltaT).roundToLong())
}
inline fun MJD.toJT(): JT = JT((this - MJD.J2000) / 36525.0.mJD)

inline fun JT.toMJD(): MJD = MJD(this * 36525.0) + MJD.J2000
inline fun JT.toInstant(useDeltaT: Boolean = true): Instant = toMJD().toInstant(useDeltaT)