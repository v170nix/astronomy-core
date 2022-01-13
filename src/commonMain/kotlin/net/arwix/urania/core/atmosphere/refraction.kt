package net.arwix.urania.core.atmosphere

import net.arwix.urania.core.annotation.ExperimentalUrania
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.angle.times
import net.arwix.urania.core.observer.Observer
import net.arwix.urania.core.toRad
import kotlin.math.*


private const val GAS_CONSTANT = 8.314462618

/**
 *   Atmospheric refraction for radio and optical wavelengths
 *
 * 	Given:
 *    ZOBS    d  observed zenith distance of the source (radian)
 *    HM      d  height of the observer above sea level (metre)
 *    TDK     d  ambient temperature at the observer (deg K)
 *    PMB     d  pressure at the observer (millibar)
 *    RH      d  relative humidity at the observer (range 0-1)
 *    WL      d  effective wavelength of the source (micrometre)
 *    PHI     d  latitude of the observer (radian, astronomical)
 *    TLR     d  temperature lapse rate in the troposphere (degK/metre)
 *    EPS     d  precision required to terminate iteration (radian)
 *
 *  Returned:
 *    REF     d  refraction: in vacuo ZD minus observed ZD (radian)
 *
 *  Typical values for the TLR and EPS arguments are 0.0065D0
 *  and 1D-9 respectively.
 *
 *  This routine computes the refraction for zenith distances up to
 *  and a little beyond 90 deg using the method of Hohenkerk and
 *  Sinclair (NAO Technical Notes 59 and 63).  The code is a
 *  slightly modified form of the optical refraction subroutine AREF
 *  of C.Hohenkerk (HMNAO, September 1984), with extensions to support
 *  radio wavelengths.  Most of the modifications to the HMNAO optical
 *  algorithm are cosmetic;  in addition the angle arguments have been
 *  changed to radians, any value of ZOBS is allowed, and other values
 *  have been limited to safe values.  The radio expressions were
 *  devised by A.T.Sinclair (RGO - private communication), based on
 *  the Essen & Froome refractivity formula adopted in Resolution 1
 *  of the 13th International Geodesy Association General Assembly
 *  (Bulletin Geodesique 1963 p390).
 *
 *  The radio refraction is chosen by specifying WL > 100 micrometres.
 *
 *  Before use, the value of ZOBS is expressed in the range +/- Pi.
 *  If this ranged ZOBS is -ve, the result REF is computed from its
 *  absolute value before being made -ve to match.  In addition, if
 *  it has an absolute value greater than 93 deg, a fixed REF value
 *  equal to the result for ZOBS = 93 deg is returned, appropriately
 *  signed.
 *
 *  Fixed values of the water vapour exponent, height of tropopause, and
 *  height at which refraction is negligible are used.
 *
 *  Original version by C.Hohenkerk, HMNAO, September 1986.
 *  This Starlink version by P.T.Wallace, 4 July 1993.
 *
 *  @param alt Apparent elevation in radians.
 */
@ExperimentalUrania
fun getAtmosphericRefraction(obs: Observer, alt: Radian, observingWavelength: Double = 0.555): Double {
    // 93 degrees in radians
    val D93: Radian = 93.0.deg.toRad()
    // Universal gas constant
    val GCR: Double = GAS_CONSTANT * 1000.0
    // Molecular weight of dry air
    val DMD = 28.966
    // Molecular weight of water vapour
    val DMW = 18.016
    // Mean Earth radius (metr)
    val S = 6378120.0
    // Exponent of temperature dependence of water vapour pressure
    val DELTA = 18.36
    // Height of tropopause (metr)
    val HT = 11000.0
    // Upper limit for refractive effects (metr)
    val HS = 80000.0

    var IN: Int
    var IS: Int
    var K: Int
    var ISTART: Int
    var I: Int
    var J: Int
    var OPTIC: Boolean
    var LOOP: Boolean
    val ZOBS1: Radian
    val ZOBS2: Double
    val HMOK: Double
    val TDKOK: Double
    val PMBOK: Double
    val RHOK: Double
    val WLOK: Double
    val ALPHA: Double
    val TOL: Double
    val WLSQ: Double
    val GB: Double
    val A: Double
    val GAMAL: Double
    val GAMMA: Double
    val GAMM2: Double
    val DELM2: Double
    val PW0: Double
    val W: Double
    val C1: Double
    val C2: Double
    val C3: Double
    val C4: Double
    val C5: Double
    val C6: Double
    val R0: Double
    val DN0: Double
    val DNDR0: Double
    val SK0: Double
    val F0: Double
    val RT: Double
    val TT: Double
    val DNT: Double
    val DNDRT: Double
    val ZT: Double
    val FT: Double
    val DNTS: Double
    val DNDRTS: Double
    var SINE: Double
    val ZTS: Double
    val FTS: Double
    val RS: Double
    val DNS: Double
    val DNDRS: Double
    val ZS: Double
    val FS: Double
    var REFO: Double
    var FE: Double
    var FO: Double
    var H: Double
    var FB: Double
    var FF: Double
    var STEP: Double
    var Z = 0.0
    var R = 0.0
    var RG: Double
    var DN: Double
    var DNDR: Double
    var F: Double
    var REFP = 0.0
    var REFT = 0.0 //,TG,T,TEMPO;


    val ZOBS: Radian = (PI / 2.0).rad - alt
    val HM: Double = obs.position.altitude
    val TDK: Double = obs.weather.temperature + 273.15
    val PMB: Double = obs.weather.pressure.toDouble()
    val RH: Double = obs.weather.humidity * 0.01
    val WL: Double = observingWavelength
    val PHI: Radian = obs.position.latitude
    val TLR = 0.0065
    val EPS = 1.0E-9

    // Transform ZOBS into the normal range
    ZOBS1 = ZOBS
    ZOBS2 = min(abs(ZOBS1.value), D93.value)

    // Keep other arguments within safe bounds
    HMOK = min(max(HM, -1000.0), 10000.0)
    TDKOK = min(max(TDK, 100.0), 500.0)
    PMBOK = min(max(PMB, 0.0), 10000.0)
    RHOK = min(max(RH, 0.0), 1.0)
    WLOK = max(WL, 0.1)
    ALPHA = min(max(abs(TLR), 0.001), 0.01)

    // Tolerance for iteration
    TOL = min(abs(EPS), 0.1) / 2.0

    // Decide whether optical or radio case - switch at 100 micron
    OPTIC = true
    if (WLOK > 100.0) OPTIC = false

    // Set up model atmosphere parameters defined at the observer
    WLSQ = WLOK * WLOK
    GB = 9.784 * (1.0 - 0.0026 * cos(2.0 * PHI) - 0.00000028 * HMOK)
    A = if (OPTIC) {
        (287.604 + 1.6288 / WLSQ + 0.0136 / (WLSQ * WLSQ)) * 273.15 / 1013.25 * 1.0E-6
    } else {
        77.624E-6
    }
    GAMAL = GB * DMD / GCR
    GAMMA = GAMAL / ALPHA
    GAMM2 = GAMMA - 2.0
    DELM2 = DELTA - 2.0
    PW0 = RHOK * (TDKOK / 247.1).pow(DELTA)
    W = PW0 * (1.0 - DMW / DMD) * GAMMA / (DELTA - GAMMA)
    C1 = A * (PMBOK + W) / TDKOK
    C2 = if (OPTIC) {
        (A * W + 11.2684E-6 * PW0) / TDKOK
    } else {
        (A * W + 12.92E-6 * PW0) / TDKOK
    }
    C3 = (GAMMA - 1.0) * ALPHA * C1 / TDKOK
    C4 = (DELTA - 1.0) * ALPHA * C2 / TDKOK
    if (OPTIC) {
        C5 = 0.0
        C6 = 0.0
    } else {
        C5 = 371897E-6 * PW0 / TDKOK
        C6 = C5 * DELM2 * ALPHA / (TDKOK * TDKOK)
    }

    // At the observer
    R0 = S + HMOK
    var out: DoubleArray = ATMT(R0, TDKOK, ALPHA, GAMM2, DELM2, C1, C2, C3, C4, C5, C6, R0)

    //TEMPO = out[0];
    DN0 = out[1]
    DNDR0 = out[2]
    SK0 = DN0 * R0 * sin(ZOBS2)
    F0 = REFI(R0, DN0, DNDR0)

    // At the tropopause in the troposphere
    RT = S + HT
    out = ATMT(R0, TDKOK, ALPHA, GAMM2, DELM2, C1, C2, C3, C4, C5, C6, RT)
    TT = out[0]
    DNT = out[1]
    DNDRT = out[2]
    SINE = SK0 / (RT * DNT)
    ZT = atan2(SINE, sqrt(max(1.0 - SINE * SINE, 0.0)))
    FT = REFI(RT, DNT, DNDRT)

    // At the tropopause in the stratosphere
    out = ATMS(RT, TT, DNT, GAMAL, RT)
    DNTS = out[0]
    DNDRTS = out[1]
    SINE = SK0 / (RT * DNTS)
    ZTS = atan2(SINE, sqrt(max(1.0 - SINE * SINE, 0.0)))
    FTS = REFI(RT, DNTS, DNDRTS)

    // At the stratosphere limit
    RS = S + HS
    out = ATMS(RT, TT, DNT, GAMAL, RS)
    DNS = out[0]
    DNDRS = out[1]
    SINE = SK0 / (RS * DNS)
    ZS = atan2(SINE, sqrt(max(1.0 - SINE * SINE, 0.0)))
    FS = REFI(RS, DNS, DNDRS)

    // Integrate the refraction integral in two parts;  first in the
    // troposphere (K=1), then in the stratosphere (K=2)
    REFO = -999.999
    IS = 16
    K = 1
    while (K <= 2) {
        ISTART = 0
        FE = 0.0
        FO = 0.0
        if (K == 1) {
            H = (ZT - ZOBS2) / IS.toDouble()
            FB = F0
            FF = FT
        } else {
            H = (ZS - ZTS) / IS.toDouble()
            FB = FTS
            FF = FS
        }
        IN = IS - 1
        IS /= 2
        STEP = H

        // Start of iteration loop (terminates at specified precision)
        LOOP = true
        while (LOOP) {
            I = 1
            while (I <= IN) {
                if (I == 1 && K == 1) {
                    Z = ZOBS2 + H
                    R = R0
                } else {
                    if (I == 1 && K == 2) {
                        Z = ZTS + H
                        R = RT
                    } else {
                        Z += STEP
                    }
                }

                // Given the zenith distance (Z) find R
                RG = R
                J = 1
                while (J <= 4) {
                    if (K == 1) {
                        out = ATMT(R0, TDKOK, ALPHA, GAMM2, DELM2, C1, C2, C3, C4, C5, C6, RG)
                        //TG = out[0];
                        DN = out[1]
                        DNDR = out[2]
                    } else {
                        out = ATMS(RT, TT, DNT, GAMAL, RG)
                        DN = out[0]
                        DNDR = out[1]
                    }
                    if (Z > 1E-20) RG -= (RG * DN - SK0 / sin(Z)) / (DN + RG * DNDR)
                    J++
                }
                R = RG

                // Find refractive index and integrand at R
                if (K == 1) {
                    out = ATMT(R0, TDKOK, ALPHA, GAMM2, DELM2, C1, C2, C3, C4, C5, C6, R)
                    //T = out[0];
                    DN = out[1]
                    DNDR = out[2]
                } else {
                    out = ATMS(RT, TT, DNT, GAMAL, R)
                    DN = out[0]
                    DNDR = out[1]
                }
                F = REFI(R, DN, DNDR)
                if (ISTART == 0 && I % 2 == 0) {
                    FE += F
                } else {
                    FO += F
                }
                I++
            }

            // Evaluate the integrand using Simpson's Rule
            REFP = H * (FB + 4.0 * FO + 2.0 * FE + FF) / 3.0

            // Has required precision been reached?
            if (abs(REFP - REFO) > TOL) {
                // No: prepare for next iteration
                IS *= 2
                IN = IS
                STEP = H
                H /= 2.0
                FE += FO
                FO = 0.0
                REFO = REFP
                if (ISTART == 0) ISTART = 1
            } else {
                // Yes: save troposphere component and terminate loop
                if (K == 1) REFT = REFP
                LOOP = false
            }
        }
        K++
    }

    // Result
    var REF = REFT + REFP
    if (ZOBS1.value < 0.0) REF = -REF
    return min(alt.value - REF, PI / 2.0)
}

/**
 * Refractive index and derivative wrt height for the troposphere
 *
 * Given:
 * R0      d    height of observer from centre of the Earth (metre)
 * T0      d    temperature at the observer (deg K)
 * ALPHA   d    alpha          )
 * GAMM2   d    gamma minus 2  ) see HMNAO paper
 * DELM2   d    delta minus 2  )
 * C1      d    useful term  )
 * C2      d    useful term  )
 * C3      d    useful term  ) see source
 * C4      d    useful term  ) of sla_REFRO
 * C5      d    useful term  )
 * C6      d    useful term  )
 * R       d    current distance from the centre of the Earth (metre)
 *
 * Returned:
 * T       d    temperature at R (deg K)
 * DN      d    refractive index at R
 * DNDR    d    rate the refractive index is changing at R
 *
 * This routine is a version of the ATMOSTRO routine (C.Hohenkerk,
 * HMNAO), with enhancements, specified by A.T.Sinclair (RGO) to
 * handle the radio case.
 *
 * Note that in the optical case C5 and C6 are zero.
 *
 * Original version by C.Hohenkerk, HMNAO, August 1984
 * This Starlink version by P.T.Wallace, 26 July 1993
 */
private fun ATMT(
    R0: Double, T0: Double, ALPHA: Double, GAMM2: Double, DELM2: Double,
    C1: Double, C2: Double, C3: Double, C4: Double, C5: Double, C6: Double, R: Double
): DoubleArray {
    val T = T0 - ALPHA * (R - R0)
    val TT0: Double = max(T / T0, 0.00)
    val TT0GM2: Double = TT0.pow(GAMM2)
    val TT0DM2: Double = TT0.pow(DELM2)
    val DN = 1.0 + (C1 * TT0GM2 - (C2 - C5 / T) * TT0DM2) * TT0
    val DNDR = -C3 * TT0GM2 + (C4 - C6 / TT0) * TT0DM2
    return doubleArrayOf(T, DN, DNDR)
}


/** The refraction integrand  */
private fun REFI(R: Double, DN: Double, DNDR: Double): Double {
    return R * DNDR / (DN + R * DNDR)
}

/**
 * Refractive index and derivative wrt height for the stratosphere
 *
 * Given:
 * RT      d    height of tropopause from centre of the Earth (metre)
 * TT      d    temperature at the tropopause (deg K)
 * DNT     d    refractive index at the tropopause
 * GAMAL   d    constant of the atmospheric model = G*MD/R
 * R       d    current distance from the centre of the Earth (metre)
 *
 * Returned:
 * DN      d    refractive index at R
 * DNDR    d    rate the refractive index is changing at R
 *
 * This routine is a version of the ATMOSSTR routine (C.Hohenkerk,
 * HMNAO), with trivial modifications.
 *
 * Original version by C.Hohenkerk, HMNAO, August 1984
 * This Starlink version by P.T.Wallace, 4 July 1993
 */
private fun ATMS(RT: Double, TT: Double, DNT: Double, GAMAL: Double, R: Double): DoubleArray {
    val B = GAMAL / TT
    val W: Double = (DNT - 1.0) * exp(-B * (R - RT))
    val DN = 1.0 + W
    val DNDR = -B * W
    return doubleArrayOf(DN, DNDR)
}