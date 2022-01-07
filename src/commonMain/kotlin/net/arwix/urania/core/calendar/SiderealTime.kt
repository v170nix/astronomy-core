package net.arwix.urania.core.calendar

import net.arwix.urania.core.math.ARCSEC_TO_RAD
import net.arwix.urania.core.math.SECONDS_PER_DAY
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.rad
import kotlin.math.PI
import kotlin.math.floor

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