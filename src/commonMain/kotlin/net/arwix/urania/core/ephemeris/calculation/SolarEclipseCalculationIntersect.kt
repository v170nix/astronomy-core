package net.arwix.urania.core.ephemeris.calculation

import net.arwix.urania.core.annotation.Apparent
import net.arwix.urania.core.annotation.Equatorial
import net.arwix.urania.core.annotation.ExperimentalUrania
import net.arwix.urania.core.annotation.Geocentric
import net.arwix.urania.core.calendar.*
import net.arwix.urania.core.convert
import net.arwix.urania.core.ephemeris.Ephemeris
import net.arwix.urania.core.math.AU
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.Radian.Companion.PI
import net.arwix.urania.core.math.angle.Radian.Companion.PI2
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.math.modulo
import net.arwix.urania.core.math.vector.Matrix
import net.arwix.urania.core.math.vector.RectangularVector
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.rectangular
import net.arwix.urania.core.toDeg
import net.arwix.urania.core.toRad
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

@ExperimentalUrania
class SolarEclipseCalculationIntersect(
    @Geocentric
    @Apparent
    @Equatorial
    val moonEphemeris: Ephemeris,
    val sunEphemeris: Ephemeris
) {
    suspend fun intersect(jd: MJD) {
        val fac = 0.996647
        val jt = jd.toJT()
//        val obl = createObliquityElements(ID_OBLIQUITY_WILLIAMS_1994, jt)
        val ephemMoon = moonEphemeris(jt).rectangular
//        val ephemMoon = SphericalVector(
//            ((10.0 / 24.0 + 4.0 / 60.0 / 24.0 + 30.6 / 60.0 / 60.0 / 24.0) * 360.0).deg.toRad(),
//            (12.0 + 16.0 / 60.0 + 32.8 / 60.0 / 60.0).deg.toRad(),
//            0.00248733984887
////            moonEphemeris(jt).spherical.r
//        ).rectangular

//        val ephemMoon = SphericalVector(
//            ((6.0 / 24.0 + 46.0 / 60.0 / 24.0 + 17.8 / 60.0 / 60.0 / 24.0) * 360.0).deg.toRad(),
//            (22.0 + 22.0 / 60.0 + 09.7 / 60.0 / 60.0).deg.toRad(),
//            0.00245813486723
////            moonEphemeris(jt).spherical.r
//        ).rectangular

//        val ephemSun = sunEphemeris(jt - (8.32 / 60.0 / 24.0 / 36525.0).jT ).rectangular
        val ephemSun = sunEphemeris(jt).rectangular

//        val ephemSun = SphericalVector(
//            ((6.0 / 24.0 + 46.0 / 60.0 / 24.0 + 14.7 / 60.0 / 60.0 / 24.0) * 360.0).deg.toRad(),
//            (23.0 + 0.0 / 60.0 + 36.5 / 60.0 / 60.0).deg.toRad(),
//            1.01674029618819
//         //   sunEphemeris(jt).spherical.r
//        ).rectangular

        ephemMoon.z = ephemMoon.z / fac
        ephemSun.z = ephemSun.z / fac
        println()

        val r_MS = (ephemMoon - ephemSun).normalize()
//        println("r_MS=${r_MS * AU}")
        val e = (ephemMoon - ephemSun) / r_MS
//        println("e=$e")
        val e_spher: SphericalVector = convert(e)
//        println("e_spher=${e_spher.phi}")

        val s0 = -(ephemMoon dot e)
//        println("s0=${s0 * AU}")

        val R_Sun = 696000.0
        val R_Moon = 1737.4
        //  val R_Moon =  1738.0
//        val R_Earth = 6376.0606
        val R_Earth = 6378.137

        val Delta = s0 * s0 + R_Earth * R_Earth / AU / AU - (ephemMoon dot ephemMoon)
//        println("Delta=${Delta * AU}")
        val r0 = sqrt(R_Earth * R_Earth - Delta * AU * AU)
        print(" ; r0=${r0}")

        val dUmbra = (R_Sun - R_Moon) * (s0 / r_MS) - R_Moon
        val dPenumbra = (R_Sun - R_Moon) * (s0 / r_MS) + R_Moon

        print(" ; umbra=${dUmbra}")
        print(" ; dPenumbra=${dPenumbra}")

        val s = s0 - sqrt(Delta)
//        println("s=$s")
        val r = convert<RectangularVector>(ephemMoon + e * s)
//        println("r=$r")
        val rr = RectangularVector(r.x, r.y, fac * r.z)
//        println("rr=$rr")

        val gmst = jd.getGreenwichApparentSiderealTime(SiderealTimeMethod.IAU20xx, { getEquationOfEquinoxes(jd.toJT()) })
        val r_G: SphericalVector = convert(Matrix.getRotateZ(gmst) * rr)
        val lambda = (r_G.phi + PI).value.modulo(PI2.value).rad - PI
        print(" ; lambda=${lambda.toDeg()}")
        print(" ; long=${printLat(abs(lambda.value).rad)}")
        val phi = r_G.theta
        val phiG = phi + 0.1924.deg.toRad() * sin(phi * 2.0)
        print(" ; phig=${phiG.toDeg()}")
        print(" ; lat=${printLat(phiG)}")



        if (r0 < R_Earth) {
            val s = s0 - sqrt(Delta)
//            println("s=$s")
            val r = convert<RectangularVector>(ephemMoon + e * s)
//            println("r=$r")
            val rr = RectangularVector(r.x, r.y, fac * r.z)
//            println("rr=$rr")

            val D_UmbraS = (R_Sun - R_Moon) * (s / r_MS) - R_Moon
            print("  dUmbraSurface=$D_UmbraS")
            if (D_UmbraS > 0.0) {
                print("  annular")
            } else {
                print("  total")
            }
        } else {
            if (r0 < R_Earth + 0.5 * abs(dUmbra)) {
                if (dUmbra > 0.0) {
                    print("  NonCenAnn")
                } else {
                    print("  NonCenTot")
                }
            } else {
                if (r0 < R_Earth + 0.5 * abs(dPenumbra)) {
                    print("  Partical")
                } else {
                    print("  NoEclipse")
                }
            }
        }



    }

    private fun printLat(theta: Radian): String {

        val g = (theta).toDeg().value.toInt()
        val mm = ((theta).toDeg() - g.deg) * 60.0
        val m = mm.value.toInt()
        val s = (mm.value - m) * 60.0
        return "$g ${abs(m)} ${abs(s)}"
    }

    fun getGMST(mJD: MJD): Radian {
        val mJD0 = floor(mJD.value)
        val uT = 86400.0 * (mJD.value - mJD0) // [сек]
        val jT0 = (mJD0 - 51544.5) / 36525.0
        val jT = (mJD.value - 51544.5) / 36525.0
        val gmst = 24110.54841 + 8640184.812866 * jT0 + 1.0027379093 * uT + (0.093104 - 0.0000062 * jT) * jT * jT // [сек]
//    var gmst0 = 6.697374558 + 0.06570982441908 * jT0 * 36525.0 + 1.00273790935 * uT / 60.0 / 60.0 + 0.000026 * jT * jT
//    gmst0 = gmst0 * 60.0 * 60.0
//    println("gmst=$gmst")
//    println("gmst0=$gmst0")
        return PI2 / 86400.0 * (gmst % 86400.0) // [рад]
    }
}