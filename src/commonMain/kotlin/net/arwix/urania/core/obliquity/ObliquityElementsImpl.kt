package net.arwix.urania.core.obliquity

import net.arwix.urania.core.arcToRad
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.MINUTES_PER_DEGREE
import net.arwix.urania.core.math.SECONDS_PER_DEGREE
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.Radian.Companion.PI2
import net.arwix.urania.core.math.polynomialSum
import net.arwix.urania.core.math.vector.Matrix
import net.arwix.urania.core.math.vector.Matrix.Companion.AXIS_X
import net.arwix.urania.core.math.vector.Vector
import kotlin.math.cos
import kotlin.math.sin

fun Obliquity.createElements(jT: JT): ObliquityElements {

    val eps = getEps(jT)
    val eclipticMatrix = Matrix(AXIS_X, -eps)
    val equatorialMatrix = eclipticMatrix.transpose()

    return object : ObliquityElements {
        override val id: Obliquity = this@createElements
        override val jT: JT = jT
        override val eps: Radian = eps
        override val eclipticToEquatorialMatrix: Matrix = eclipticMatrix
        override val equatorialToEclipticMatrix: Matrix = equatorialMatrix

        override fun rotate(vector: Vector, currentPlane: Plane): Vector {
            return when (currentPlane) {
                Plane.Ecliptic -> eclipticMatrix * vector
                Plane.Equatorial -> equatorialMatrix * vector
                else -> throw IllegalStateException()
            }
        }
    }
}

fun Obliquity.getEps(jT: JT): Radian {
    return when (this) {
        Obliquity.IAU1976 -> getEps(jT, rStartIAU1976, coefficientsIAU1976)
        Obliquity.IAU2006 -> getEps(jT, rStartIAU2006, coefficientsIAU2006)
        Obliquity.Laskar1986 -> getEps(jT, rStartLaskar1986, coefficientsLaskar1986)
        Obliquity.Simon1994 -> getEps(jT, rStartSimon1994, coefficientsSimon1994)
        Obliquity.Vondrak2011 -> getVondrakEps(jT)
        Obliquity.Williams1994 -> getEps(jT, rStartWilliams1994, coefficientsWilliams1994)
    }
}

private inline fun getEps(jT: JT, rStart: Double, coefficients: DoubleArray): Radian {
    return (rStart + coefficients.polynomialSum(jT / 100.0)).arcToRad()
}

private inline fun getVondrakEps(jT: JT): Radian {
    return (jT.value * PI2.value).let { w ->
        xyper.sumOf { doubles: DoubleArray ->
            (w / doubles[0]).let { a ->
                cos(a) * doubles[1] + sin(a) * doubles[2]
            }
        } + xypol.polynomialSum(jT.value)
    }.arcToRad()
}

private val rStartWilliams1994 by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.406173 }
private val coefficientsWilliams1994 by lazy {
    doubleArrayOf(
        0.0,
        -4683.396,
        -1.75,
        1998.9,
        -51.38,
        -249.67,
        -39.05,
        7.12,
        27.87,
        5.79,
        2.45
    )
}

private val rStartSimon1994 by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.412 }
private val coefficientsSimon1994 by lazy {
    doubleArrayOf(
        0.0,
        -4680.927,
        -1.52,
        1998.9,
        -51.38,
        -249.67,
        -39.05,
        7.12,
        27.87,
        5.79,
        2.45
    )
}


private val rStartLaskar1986 by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.448 }
private val coefficientsLaskar1986 by lazy {
    doubleArrayOf(
        0.0,
        -4680.93,
        -1.55,
        1999.25,
        -51.38,
        -249.67,
        -39.05,
        7.12,
        27.87,
        5.79,
        2.45
    )
}

private val rStartIAU1976 by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.448 }
private val coefficientsIAU1976 by lazy { doubleArrayOf(0.0, -4681.5, -5.9, 1813.0) }

private val rStartIAU2006 by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.406 }
private val coefficientsIAU2006 by lazy { doubleArrayOf(0.0, -4683.6769, -1.831, 2003.400, -57.6, -434.0) }

private val xypol by lazy { doubleArrayOf(84028.206305, 0.3624445, -0.00004039, -110E-9) }
private val xyper by lazy {
    arrayOf(
        doubleArrayOf(409.90, 753.872780, -1704.720302),
        doubleArrayOf(396.15, -247.805823, -862.308358),
        doubleArrayOf(537.22, 379.471484, 447.832178),
        doubleArrayOf(402.90, -53.880558, -889.571909),
        doubleArrayOf(417.15, -90.109153, 190.402846),
        doubleArrayOf(288.92, -353.600190, -56.564991),
        doubleArrayOf(4043.00, -63.115353, -296.222622),
        doubleArrayOf(306.00, -28.248187, -75.859952),
        doubleArrayOf(277.00, 17.703387, 67.473503),
        doubleArrayOf(203.00, 38.911307, 3.014055)
    )
}
