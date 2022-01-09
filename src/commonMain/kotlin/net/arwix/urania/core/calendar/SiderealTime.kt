package net.arwix.urania.core.calendar

import net.arwix.urania.core.math.ARCSEC_TO_RAD
import net.arwix.urania.core.math.SECONDS_PER_DAY
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.toRad
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

// https://astronomy.stackexchange.com/questions/26315/computation-of-mean-sidereal-time-at-greenwich-using-solar-position-algorithm
// https://astronomy.stackexchange.com/questions/21002/how-to-find-greenwich-mean-sideral-time

enum class SiderealTimeMethod {
    Laskar1986, Williams1994, IAU2000, IAU20xx
}

/**
 * Greenwich Mean Sidereal Time
 * @return time in radian
 */
inline fun MJD.getGMST(method: SiderealTimeMethod): Radian {
    return when (method) {
        SiderealTimeMethod.Laskar1986 -> getGMSTLaskar1986()
        SiderealTimeMethod.Williams1994 -> getGMSTWilliams1994()
        SiderealTimeMethod.IAU2000 -> getGMSTIAU20xx(true)
        SiderealTimeMethod.IAU20xx -> getGMSTIAU20xx(false)
    }
}

inline fun MJD.getGMSTLaskar1986(): Radian {
    val mJD0 = floor(this.value).mJD
    val jT0 = mJD0.toJT()
    val secs = SECONDS_PER_DAY * (this - mJD0) // [сек]
    val h0 = ((-6.2e-6 * jT0 + 9.3104e-2) * jT0 + 8640184.812866) * jT0 + 24110.54841
    val msday = 1.0 + ((-1.86e-5 * jT0 + 0.186208) * jT0 + 8640184.812866) / (86400.0 * 36525.0)
    return Radian.PI2 / SECONDS_PER_DAY * ((h0 + msday * secs) % SECONDS_PER_DAY)
}

inline fun MJD.getGMSTWilliams1994(): Radian {
    val mJD0 = floor(this.value).mJD
    val jT0 = mJD0.toJT()
    val secs = SECONDS_PER_DAY * (this - mJD0) // [сек]
    val h0 = (((-2.0e-6 * jT0 - 3e-7) * jT0 + 9.27695e-2) * jT0 + 8640184.7928613) * jT0 + 24110.54841
    val msday =
        (((-(4.0 * 2.0e-6) * jT0 - (3.0 * 3e-7)) * jT0 + (2.0 * 9.27695e-2)) * jT0 + 8640184.7928613) / (86400.0 * 36525.0) + 1.0
    return Radian.PI2 / SECONDS_PER_DAY * ((h0 + msday * secs) % SECONDS_PER_DAY)
}

inline fun MJD.getGMSTIAU20xx(isIAU2000: Boolean): Radian {
    val mJD0 = floor(this.value).mJD
    val dt0 = this - MJD.J2000
    val secs = SECONDS_PER_DAY * (this - mJD0) // [сек]
    val dayLength = 1.0027378119113546
    val jT = toJT()
    val h0 = 2.0 * PI * (secs / SECONDS_PER_DAY + 0.5 + 0.7790572732640 + (dayLength - 1.0) * dt0)
    val gmst = h0 +
            if (isIAU2000)
                (0.014506 + (4612.156534 + (+1.3915817 + (-0.00000044 + (-0.000029956 + (-0.0000000368) * jT) * jT) * jT) * jT) * jT) * ARCSEC_TO_RAD
            else
                (0.014506 + (4612.15739966 + (+1.39667721 + (-0.00009344 + (+0.00001882) * jT) * jT) * jT) * jT) * ARCSEC_TO_RAD
    return gmst.rad.normalize()
}

fun getEquationOfEquinoxes(jT: JT): Radian {
    // abs (year) < 3000
    // See Henning Umland's page at http://www2.arnes.si/~gljsentvid10/longterm.htm
    val jT2 = jT * jT
    val jT3 = jT * jT2

    // Mean elongation of the moon
    val D = (297.85036 + 445267.111480 * jT - 0.0019142 * jT2 + jT3 / 189474).deg.normalize().toRad().value
    // Mean anomaly of the sun
    val M = (357.52772 + 35999.050340 * jT - 0.0001603 * jT2 - jT3 / 300000).deg.normalize().toRad().value
    // Mean anomaly of the moon
    val Mm = (134.96298 + 477198.867398 * jT + 0.0086972 * jT2 + jT3 / 56250).deg.normalize().toRad().value
    // Mean distance of the moon from ascending node
    val F = (93.27191 + 483202.017538 * jT - 0.0036825 * jT2 + jT3 / 327270).deg.normalize().toRad().value
    // Longitude of the ascending node of the moon
    val omega = (125.04452 - 1934.136261 * jT + 0.0020708 * jT2 + jT3 / 450000).deg.normalize().toRad().value

    // Periodic terms for nutation
    var dp = (-171996 - 174.2 * jT) * sin(omega)
    var de = (92025 + 8.9 * jT) * cos(omega)

    dp += (-13187 - 1.6 * jT) * sin(-2 * D + 2 * F + 2 * omega)
    de += (5736 - 3.1 * jT) * cos(-2 * D + 2 * F + 2 * omega)
    dp += (-2274 - 0.2 * jT) * sin(2 * F + 2 * omega)
    de += (977 - 0.5 * jT) * cos(2 * F + 2 * omega)
    dp += (2062 + 0.2 * jT) * sin(2 * omega)
    de += (-895 + 0.5 * jT) * cos(2 * omega)
    dp += (1426 - 3.4 * jT) * sin(M)
    de += (54 - 0.1 * jT) * cos(M)
    dp += (712 + 0.1 * jT) * sin(Mm)
    de += -7 * cos(Mm)
    dp += (-517 + 1.2 * jT)	* sin(-2 * D + M + 2 * F + 2 * omega)
    de += (224 - 0.6 * jT) * cos(-2 * D + M + 2 * F + 2 * omega)
    dp += (-386 - 0.4 * jT) * sin(2 * F + omega)
    de += 200 * cos(2 * F + omega)
    dp += -301 * sin(Mm + 2 * F + 2 * omega)
    de += (129 - 0.1 * jT) * cos(Mm + 2 * F + 2 * omega)
    dp += (217 - 0.5 * jT) * sin(-2 * D - M + 2 * F + 2 * omega)
    de += (-95 + 0.3 * jT) * cos(-2 * D - M + 2 * F + 2 * omega)
    dp += -158 * sin(-2 * D + Mm)
    dp += (129 + 0.1 * jT) * sin(-2 * D + 2 * F + omega)
    de += -70 * cos(-2 * D + 2 * F + omega)
    dp += 123 * sin(-Mm + 2 * F + 2 * omega)
    de += -53 * cos(-Mm + 2 * F + 2 * omega)
    dp += 63 * sin(2 * D)
    dp += (63 + 0.1 * jT) * sin(Mm + omega)
    de += -33 * cos(Mm + omega)
    dp += -59 * sin(2 * D - Mm + 2 * F + 2 * omega)
    de += 26 * cos(2 * D - Mm + 2 * F + 2 * omega)
    dp += (-58 - 0.1 * jT) * sin(-Mm + omega)
    de += 32 * cos(-Mm + omega)
    dp += -51 * sin(Mm + 2 * F + omega)
    de += 27 * cos(Mm + 2 * F + omega)
    dp += 48 * sin(-2 * D + 2 * Mm)
    dp += 46 * sin(-2 * Mm + 2 * F + omega)
    de += -24 * cos(-2 * Mm + 2 * F + omega)
    dp += -38 * sin(2 * D + 2 * F + 2 * omega)
    de += 16 * cos(2 * D + 2 * F + 2 * omega)
    dp += -31 * sin(2 * Mm + 2 * F + 2 * omega)
    de += 13 * cos(2 * Mm + 2 * F + 2 * omega)
    dp += 29 * sin(2 * Mm)
    dp += 29 * sin(-2 * D + Mm + 2 * F + 2 * omega)
    de += -12 * cos(-2 * D + Mm + 2 * F + 2 * omega)
    dp += 26 * sin(2 * F)
    dp += -22 * sin(-2 * D + 2 * F)
    dp += 21 * sin(-Mm + 2 * F + omega)
    de += -10 * cos(-Mm + 2 * F + omega)
    dp += (17 - 0.1 * jT) * sin(2 * M)
    dp += 16 * sin(2 * D - Mm + omega)
    de += -8 * cos(2 * D - Mm + omega)
    dp += (-16 + 0.1 * jT) * sin(-2 * D + 2 * M + 2 * F + 2 * omega)
    de += 7 * cos(-2 * D + 2 * M + 2 * F + 2 * omega)
    dp += -15 * sin(M + omega)
    de += 9 * cos(M + omega)
    dp += -13 * sin(-2 * D + Mm + omega)
    de += 7 * cos(-2 * D + Mm + omega)
    dp += -12 * sin(-M + omega)
    de += 6 * cos(-M + omega)
    dp += 11 * sin(2 * Mm - 2 * F)
    dp += -10 * sin(2 * D - Mm + 2 * F + omega)
    de += 5 * cos(2 * D - Mm + 2 * F + omega)
    dp += -8 * sin(2 * D + Mm + 2 * F + 2 * omega)
    de += 3 * cos(2 * D + Mm + 2 * F + 2 * omega)
    dp += 7 * sin(M + 2 * F + 2 * omega)
    de += -3 * cos(M + 2 * F + 2 * omega)
    dp += -7 * sin(-2 * D + M + Mm)
    dp += -7 * sin(-M + 2 * F + 2 * omega)
    de += 3 * cos(-M + 2 * F + 2 * omega)
    dp += -7 * sin(2 * D + 2 * F + omega)
    de += 3 * cos(2 * D + 2 * F + omega)
    dp += 6 * sin(2 * D + Mm)
    dp += 6 * sin(-2 * D + 2 * Mm + 2 * F + 2 * omega)
    de += -3 * cos(-2 * D + 2 * Mm + 2 * F + 2 * omega)
    dp += 6 * sin(-2 * D + Mm + 2 * F + omega)
    de += -3 * cos(-2 * D + Mm + 2 * F + omega)
    dp += -6 * sin(2 * D - 2 * Mm + omega)
    de += 3 * cos(2 * D - 2 * Mm + omega)
    dp += -6 * sin(2 * D + omega)
    de += 3 * cos(2 * D + omega)
    dp += 5 * sin(-M + Mm)
    dp += -5 * sin(-2 * D - M + 2 * F + omega)
    de += 3 * cos(-2 * D - M + 2 * F + omega)
    dp += -5 * sin(-2 * D + omega)
    de += 3 * cos(-2 * D + omega)
    dp += -5 * sin(2 * Mm + 2 * F + omega)

    // Nutation in longitude
    val deltaPsi = (dp / 36000000.0).deg

    // Nutation in obliquity
    val deltaEps = (de / 36000000.0).deg

    // Mean obliquity of the ecliptic
    val eps0: Double = (84381.448 - 46.815 * jT - 0.00059 * jT2 + 0.001813 * jT3) / 3600.0

    // True obliquity of the ecliptic
    val eps = (eps0 + deltaEps).deg

    // Equation of the equinoxes
    val eoe: Double = 240.0 * deltaPsi * cos(eps.toRad())

    return (eoe * 15.0 * ARCSEC_TO_RAD).rad

}