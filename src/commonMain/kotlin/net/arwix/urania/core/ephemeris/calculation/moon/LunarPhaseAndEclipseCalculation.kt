package net.arwix.urania.core.ephemeris.calculation.moon

import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.arwix.urania.core.calendar.getDaysInMonth
import net.arwix.urania.core.calendar.mJD
import net.arwix.urania.core.calendar.toInstant
import net.arwix.urania.core.calendar.toMJD
import net.arwix.urania.core.math.DELTA_JD_MJD
import net.arwix.urania.core.math.JULIAN_DAYS_PER_CENTURY
import net.arwix.urania.core.math.SECONDS_PER_DAY
import net.arwix.urania.core.math.angle.cos
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.math.polynomialSum
import net.arwix.urania.core.toRad
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

@Suppress("unused")
object LunarPhaseAndEclipseCalculation {

    data class Event(val instant: Instant, val phase: Phase, val eclipse: Eclipse? = null)

    enum class Phase(val delta: Double) {
        New(0.0),
        FirstQuarter(0.25),
        Full(0.5),
        LastQuarter(0.75)
    }

    enum class EventType {
        Next, Previous, Closest
    }

    open class Eclipse(val timeOfMaximumEclipseMJD: Double)

    sealed class SolarEclipse(timeOfMaximumEclipseMJD: Double) : Eclipse(timeOfMaximumEclipseMJD) {
        class Total(timeOfMaximumEclipseMJD: Double, val isCentral: Boolean) : SolarEclipse(timeOfMaximumEclipseMJD)
        class Annular(timeOfMaximumEclipseMJD: Double, val isCentral: Boolean) : SolarEclipse(timeOfMaximumEclipseMJD)
        class Hybrid(timeOfMaximumEclipseMJD: Double, val isCentral: Boolean) : SolarEclipse(timeOfMaximumEclipseMJD)
        class Partial(timeOfMaximumEclipseMJD: Double, val magnitude: Double) : SolarEclipse(timeOfMaximumEclipseMJD)
    }

    sealed class LunarEclipse(
        timeOfMaximumEclipseMJD: Double,
        val partialPhasePenumbraSemiDuration: Double
    ) : Eclipse(timeOfMaximumEclipseMJD) {

        class Penumbral(
            timeOfMaximumEclipseMJD: Double,
            val magnitude: Double,
            val radius: Double,
            partialPhasePenumbraSemiDuration: Double
        ) : LunarEclipse(timeOfMaximumEclipseMJD, partialPhasePenumbraSemiDuration)

        class Partial(
            timeOfMaximumEclipseMJD: Double,
            val magnitude: Double,
            val radiusPenumbral: Double,
            val radiusUmbral: Double,
            partialPhasePenumbraSemiDuration: Double,
            val partialPhaseSemiDuration: Double
        ) : LunarEclipse(timeOfMaximumEclipseMJD, partialPhasePenumbraSemiDuration)

        class Total(
            timeOfMaximumEclipseMJD: Double,
            val magnitude: Double,
            val radiusPenumbral: Double,
            val radiusUmbral: Double,
            partialPhasePenumbraSemiDuration: Double,
            val partialPhaseSemiDuration: Double,
            val totalPhaseSemiDuration: Double
        ) : LunarEclipse(timeOfMaximumEclipseMJD, partialPhasePenumbraSemiDuration)

    }

    suspend fun invoke(
        beginInstant: Instant,
        endInstant: Instant,
        addEclipses: Boolean = true,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): List<Event> {
        val initK = Phase.values().minOf { getInitK(beginInstant, it, EventType.Next) }
        val endK = Phase.values().maxOf { getInitK(endInstant, it, EventType.Next) }
        val count = abs(endK - initK).toInt()
        return calculate(beginInstant, EventType.Next, count, addEclipses, dispatcher).filter {
            it.instant.epochSeconds >= beginInstant.epochSeconds &&
                    it.instant.epochSeconds <= endInstant.epochSeconds
        }
    }

    private suspend fun calculate(
        beginInstant: Instant,
        eventType: EventType,
        count: Int = 1,
        addEclipses: Boolean = true,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): List<Event> = coroutineScope {
        val initK = Phase.values().map {
            it to getInitK(beginInstant, it, eventType)
        }
        val d = if (eventType == EventType.Previous) -1 else 1
        val results = mutableListOf<Deferred<Event>>()
        (0 until count).map { index ->
            initK.forEach { (phase, k) ->
                results.add(
                    async(dispatcher) {
                        getTruePhaseJd(k + d * index, phase, addEclipses)
                    }
                )
            }
        }
        results.awaitAll().sortedBy { it.instant.epochSeconds }
    }

    private fun getInitK(instant: Instant, event: Phase, eventType: EventType): Double {
        val date = instant.toLocalDateTime(TimeZone.UTC)
        val dayMonthFraction = (date.dayOfMonth + (date.hour + (date.minute / 60.0)) / 24.0)
        val year = date.year +
                (date.monthNumber - 1.0 + dayMonthFraction / (1.0 + getDaysInMonth(date.year, date.monthNumber))) / 12.0
//        println(year)
        val kapprox = (year - 2000.0) * 12.3685
        var k = round(kapprox, event.delta, eventType)
        val jde = getMeanPhaseJd(k)
        val mjde = (jde - DELTA_JD_MJD).mJD
        val mjd = instant.toMJD()
        if (mjde > mjd && eventType == EventType.Previous) k--
        if (mjde < mjd && eventType == EventType.Next) k++
        if (mjde > mjd && eventType != EventType.Previous) {
            val km = k - 1
            val newMJde = (getMeanPhaseJd(km) - DELTA_JD_MJD).mJD
            if ((newMJde > mjd && eventType == EventType.Next) ||
                (abs((mjd - newMJde).value) < abs((mjd - mjde).value) && eventType == EventType.Closest)
            ) k = km
        }
        if (mjde < mjd && eventType != EventType.Next) {
            val km = k + 1
            val newMJde = (getMeanPhaseJd(km) - DELTA_JD_MJD).mJD
            if ((newMJde < mjd && eventType == EventType.Previous) ||
                (abs((mjd - newMJde).value) < abs((mjd - mjde).value) && eventType == EventType.Closest)
            ) k = km
        }
        return k
    }

    private fun round(kapprox: Double, delta: Double, eventType: EventType): Double {
        var k = delta + floor(kapprox)
        if (eventType == EventType.Next && k < kapprox) k++
        if (eventType == EventType.Previous && k > kapprox) k--
        if (eventType == EventType.Closest && k < kapprox - 0.5 && delta == 0.0) k++
        return k
    }

    private fun getMeanPhaseJd(k: Double): Double {
        val t = k / 1236.85
        return doubleArrayOf(
            2451550.09765 + 29.530588853 * k,
            0.0, 0.0001337, -0.000000150, 0.00000000073
        ).polynomialSum(t)
    }

    private fun getTruePhaseJd(k: Double, phase: Phase, addEclipses: Boolean = true): Event {
        val jd = getMeanPhaseJd(k)
        val t = k / 1236.85

        val M = doubleArrayOf(2.5534 + 29.10535669 * k, 0.0, -0.0000218, -0.00000011).polynomialSum(t).deg.toRad()
        val Mp = doubleArrayOf(
            201.5643 + 385.81693528 * k,
            0.0,
            0.0107438,
            0.00001239,
            -0.000000058
        ).polynomialSum(t).deg.toRad()
        val F = doubleArrayOf(
            160.7108 + 390.67050274 * k,
            0.0,
            -0.0016341,
            -0.00000227,
            0.000000011
        ).polynomialSum(t).deg.toRad()
        val O = doubleArrayOf(124.7746 - 1.5637558 * k, 0.0, 0.002069, 0.00000215).polynomialSum(t).deg.toRad()

        val E = doubleArrayOf(1.0, -0.002516, -0.0000074).polynomialSum(t)

        val W = 0.00306 -
                0.00038 * E * cos(M) +
                0.00026 * cos(Mp) -
                0.00002 * cos(Mp - M) +
                0.00002 * cos(Mp + M) +
                0.00002 * cos(F * 2.0)

        val F1 by lazy { F - (0.02665 * sin(O)).deg.toRad() }
        val A1 = (299.77 + 0.107408 * k - 0.009173 * t * t).deg.toRad()
        val A2 = (251.88 + 0.016321 * k).deg.toRad()
        val A3 = (251.83 + 26.651886 * k).deg.toRad()
        val A4 = (349.42 + 36.412478 * k).deg.toRad()
        val A5 = (84.66 + 18.206239 * k).deg.toRad()
        val A6 = (141.74 + 53.303771 * k).deg.toRad()
        val A7 = (207.14 + 2.453732 * k).deg.toRad()
        val A8 = (154.84 + 7.306860 * k).deg.toRad()
        val A9 = (34.52 + 27.261239 * k).deg.toRad()
        val A10 = (207.19 + 0.121824 * k).deg.toRad()
        val A11 = (291.34 + 1.844379 * k).deg.toRad()
        val A12 = (161.72 + 24.198154 * k).deg.toRad()
        val A13 = (239.56 + 25.513099 * k).deg.toRad()
        val A14 = (331.55 + 3.592518 * k).deg.toRad()

        val p by lazy {
            -0.0392 * sin(Mp) + 0.2070 * E * sin(M) +
                    0.0024 * E * sin(M * 2.0) + 0.0116 * sin(Mp * 2.0) -
                    0.0073 * E * sin(Mp + M) + 0.0067 * E * sin(Mp - M) +
                    0.0118 * sin(F1 * 2.0)
        }
        val q by lazy {
            5.2207 - 0.3299 * cos(Mp) - 0.0048 * E * cos(M) +
                    0.002 * E * cos(M * 2.0) - 0.006 * E * cos(Mp + M) +
                    0.0041 * E * cos(Mp - M)
        }
        val ww by lazy { abs(cos(F1)) }
        val gamma by lazy { (p * cos(F1) + q * sin(F1)) * (1.0 - 0.0048 * ww) }
        val u by lazy {
            0.0059 + 0.0046 * E * cos(M) - 0.0182 * cos(Mp) +
                    0.0004 * cos(Mp * 2.0) - 0.0005 * cos(M + Mp)
        }
        val absGamma by lazy { abs(gamma) }


//        val phase = MoonPhase.getPhase(k)
        var deltaJd = when (phase) {
            Phase.New -> {
                -0.4072 * sin(Mp) + 0.17241 * E * sin(M) +
                        0.01608 * sin(Mp * 2.0) + 0.01039 * sin(F * 2.0) +
                        0.00739 * E * sin(Mp - M) - 0.00514 * E * sin(Mp + M) +
                        0.00208 * E * E * sin(M * 2.0) - 0.00111 * sin(Mp - F * 2.0) -
                        0.00057 * sin(Mp + F * 2.0) + 0.00056 * E * sin(Mp * 2.0 + M) -
                        0.00042 * sin(Mp * 3.0) + 0.00042 * E * sin(M + F * 2.0) +
                        0.00038 * E * sin(M - F * 2.0) - 0.00024 * E * sin(Mp * 2.0 - M) -
                        0.00007 * sin(Mp + M * 2.0) - 0.00017 * sin(O) +
                        0.00004 * (sin(Mp * 2.0 - F * 2.0) + sin(M * 3.0)) +
                        0.00003 * (sin(Mp + M - F * 2.0) + sin(Mp * 2.0 + F * 2.0) - sin(Mp + M + F * 2.0) + sin(Mp - M + F * 2.0)) +
                        0.00002 * (-sin(Mp - M - F * 2.0) - sin(Mp * 3.0 + M) + sin(Mp * 4.0))
            }

            Phase.Full -> {
                -0.40614 * sin(Mp) + 0.17302 * E * sin(M) +
                        0.01614 * sin(Mp * 2.0) + 0.01043 * sin(F * 2.0) +
                        0.00734 * E * sin(Mp - M) - 0.00515 * E * sin(Mp + M) +
                        0.00209 * E * E * sin(M * 2.0) - 0.00111 * sin(Mp - F * 2.0) -
                        0.00057 * sin(Mp + F * 2.0) + 0.00056 * E * sin(Mp * 2.0 + M) -
                        0.00042 * sin(Mp * 3.0) + 0.00042 * E * sin(M + F * 2.0) +
                        0.00038 * E * sin(M - F * 2.0) - 0.00024 * E * sin(Mp * 2.0 - M) -
                        0.00007 * E * sin(Mp + M * 2.0) - 0.00017 * sin(O) +
                        0.00004 * (sin(Mp * 2.0 - F * 2.0) + sin(M * 3.0)) +
                        0.00003 * (sin(Mp + M - F * 2.0) + sin(Mp * 2.0 + F * 2.0) - sin(Mp + M + F * 2.0) + sin(Mp - M + F * 2.0)) +
                        0.00002 * (-sin(Mp - M - F * 2.0) - sin(Mp * 3.0 + M) + sin(Mp * 4.0))
            }

            Phase.FirstQuarter, Phase.LastQuarter -> {
                (if (phase == Phase.FirstQuarter) 1.0 else -1.0) * W -
                        0.62801 * sin(Mp) + 0.17172 * E * sin(M) +
                        0.00862 * sin(Mp * 2.0) + 0.00804 * sin(F * 2.0) +
                        0.00454 * E * sin(Mp - M) - 0.01183 * E * sin(Mp + M) +
                        0.00204 * E * E * sin(M * 2.0) - 0.00180 * sin(Mp - F * 2.0) -
                        0.0007 * sin(Mp + F * 2.0) - 0.00040 * sin(Mp * 3.0) - 0.00034 * E * sin(Mp * 2.0 - M) + 0.00032 * E * sin(
                    M + F * 2.0
                ) + 0.00032 * E * sin(M - F * 2.0) -
                        0.00028 * E * E * sin(Mp + M * 2.0) + 0.00027 * E * sin(Mp * 2.0 + M) - 0.00017 * sin(O) - 0.00005 * sin(
                    Mp - M - F * 2.0
                ) +
                        0.00004 * sin(Mp * 2.0 + F * 2.0) - 0.00004 * sin(Mp + M + F * 2.0) + 0.00004 * sin(Mp - M * 2.0) + 0.00003 * sin(
                    Mp + M - F * 2.0
                ) +
                        0.00003 * sin(M * 3.0) + 0.00002 * sin(Mp * 2.0 - F * 2.0) + 0.00002 * sin(Mp - M + F * 2.0) - 0.00002 * sin(
                    Mp * 3.0 + M
                )
            }

        }

        val eclipse = if (addEclipses) {

            val sunEclipse = if (phase == Phase.New && absGamma < (1.5433 + u)) {

                val timeOfMaximumEclipse = jd - DELTA_JD_MJD - 0.4075 * sin(Mp) + 0.1721 * E * sin(M) +
                        0.0161 * sin(Mp * 2.0) - 0.0097 * sin(F1 * 2.0) +
                        0.0073 * E * sin(Mp - M) - 0.0050 * E * sin(Mp + M) +
                        0.0021 * E * sin(M * 2.0) - 0.0023 * sin(Mp - F1 * 2.0) +
                        0.0012 * sin(Mp + F1 * 2.0) + 0.0006 * E * sin(Mp * 2.0 + M) -
                        0.0004 * sin(Mp * 3.0) - 0.0003 * E * sin(M + F1 * 2.0) +
                        0.0003 * sin(A1) - 0.0002 * E * sin(M - F1 * 2.0) -
                        0.0002 * E * sin(Mp * 2.0 - M) - 0.0002 * sin(O)

                if (absGamma < 0.9972 || absGamma > 0.9972 && absGamma < 0.9972 + abs(u)) {
                    val isCentral = absGamma < 0.9972

                    if (u < 0) SolarEclipse.Total(timeOfMaximumEclipse, isCentral) else
                        if (u > 0.0047) SolarEclipse.Annular(timeOfMaximumEclipse, isCentral) else {
                            val www = 0.00464 * sqrt(1.0 - gamma * gamma)
                            if (u < www) {
                                SolarEclipse.Hybrid(timeOfMaximumEclipse, isCentral)
                            } else {
                                SolarEclipse.Annular(timeOfMaximumEclipse, isCentral)
                            }
                        }
                } else {
                    val mag = (1.5433 + u - absGamma) / (0.5461 + 2.0 * u)
                    SolarEclipse.Partial(timeOfMaximumEclipse, mag)
                }
            } else null

            (if (phase == Phase.Full) {
                val magnitudePenumbral = (1.5573 + u - absGamma) / 0.545
                val magnitudeUmbral = (1.0128 - u - absGamma) / 0.545
                if (magnitudePenumbral > 0.0 || magnitudeUmbral > 0.0) {

                    val radiusPenumbral = 1.2848 + u
                    val radiusUmbral = 0.7403 - u

                    val timeOfMaximumEclipse = jd - DELTA_JD_MJD - 0.4065 * sin(Mp) + 0.1727 * E * sin(M) +
                            0.0161 * sin(Mp * 2.0) - 0.0097 * sin(F1 * 2.0) +
                            0.0073 * E * sin(Mp - M) - 0.0050 * E * sin(Mp + M) +
                            0.0021 * E * sin(M * 2.0) - 0.0023 * sin(Mp - F1 * 2.0) +
                            0.0012 * sin(Mp + F1 * 2.0) + 0.0006 * E * sin(Mp * 2.0 + M) -
                            0.0004 * sin(Mp * 3.0) - 0.0003 * E * sin(M + F1 * 2.0) +
                            0.0003 * sin(A1) - 0.0002 * E * sin(M - F1 * 2.0) -
                            0.0002 * E * sin(Mp * 2.0 - M) - 0.0002 * sin(O)

                    val pp = 1.0128 - u
                    val tt = 0.4678 - u
                    val n = 0.5458 + 0.04 * cos(Mp)
                    val gamma2 = gamma * gamma

                    fun getTime(it: Double) = if (it > gamma2) 60.0 * sqrt(it - gamma2) / n else null

                    // in min
                    val partialPhaseSemiDuration = (pp * pp).let(::getTime)
                    val totalPhaseSemiDuration = (tt * tt).let(::getTime)
                    val h = 1.5573 + u
                    val partialPhasePenumbraSemiDuration = (h * h).let(::getTime)

                    when {
                        magnitudeUmbral < 0.0 -> LunarEclipse.Penumbral(
                            timeOfMaximumEclipse,
                            magnitudePenumbral,
                            radiusPenumbral,
                            partialPhasePenumbraSemiDuration!!
                        )
                        magnitudeUmbral < 1.0 -> LunarEclipse.Partial(
                            timeOfMaximumEclipse,
                            magnitudeUmbral,
                            radiusPenumbral,
                            radiusUmbral,
                            partialPhasePenumbraSemiDuration!!,
                            partialPhaseSemiDuration!!
                        )
                        else -> LunarEclipse.Total(
                            timeOfMaximumEclipse,
                            magnitudeUmbral,
                            radiusPenumbral,
                            radiusUmbral,
                            partialPhasePenumbraSemiDuration!!,
                            partialPhaseSemiDuration!!,
                            totalPhaseSemiDuration!!
                        )
                    }
                } else null
            } else null) ?: sunEclipse
        } else null

        deltaJd += 0.000325 * sin(A1) +
                0.000165 * sin(A2) +
                0.000164 * sin(A3) +
                0.000126 * sin(A4) +
                0.000110 * sin(A5) +
                0.000062 * sin(A6) +
                0.000060 * sin(A7) +
                0.000056 * sin(A8) +
                0.000047 * sin(A9) +
                0.000042 * sin(A10) +
                0.000040 * sin(A11) +
                0.000037 * sin(A12) +
                0.000035 * sin(A13) +
                0.000023 * sin(A14)

        return Event(
            (timeCorrectionForSecularAcceleration(jd + deltaJd) - DELTA_JD_MJD).mJD.toInstant(),
            phase,
            eclipse
        )
    }

    /**
     * Holds the value of the secular acceleration of the Moon. Currently equal
     * to -25.858 arcsec/cent^2 (Chapront, Chapront-Touze and Francou, 2002).
     */
    private const val MOON_SECULAR_ACCELERATION = -25.858

    /**
     * Value of the Moon secular acceleration ("/cy^2) for DE200.
     */
    private const val MOON_SECULAR_ACCELERATION_DE200 = -23.8946

    /**
     * Corrects Julian day of calculations of ELP2000 theory for secular
     * acceleration of the Moon. This method uses the current value of static
     * variable {@linkplain MOON_SECULAR_ACCELERATION}.
     * <BR>
     * Correction should be performed to standard dynamical time of calculations
     * (Barycentric Dynamical Time), as obtained by using the corresponding methods.
     * {@linkplain elp2000Ephemeris(TimeElement, ObserverElement, EphemerisElement)}
     * accepts any time scale, so it is possible to use the
     * output Julian day of this method with any time scale, unless a very
     * little error (well below the uncertainty in TT-UT correction) could exist
     * if this correction is applied to LT or UT, before the correction to TDB
     * which is performed in {@linkplain Elp2000#elp2000Ephemeris(TimeElement, ObserverElement, EphemerisElement)}.
     * <BR>
     * Correction for different years (using the default value) are as follows:
     *
     * <pre>
     * Year       Correction (seconds)
     * -2000      -2796
     * -1000      -1561
     *     0      -683
     *  1000      -163
     *  1955       0.000
     *  2000      -0.362
     *  3000      -195
     * </pre>
     *
     * @param jd Julian day in TDB.
     * @return Output (corrected) Julian day in TDB.
     */
    private fun timeCorrectionForSecularAcceleration(jd: Double): Double {
        val cent = (jd - 2435109.0) / JULIAN_DAYS_PER_CENTURY
        val deltaT = 0.91072 * (MOON_SECULAR_ACCELERATION - MOON_SECULAR_ACCELERATION_DE200) * cent * cent
        return jd + deltaT / SECONDS_PER_DAY
    }

}