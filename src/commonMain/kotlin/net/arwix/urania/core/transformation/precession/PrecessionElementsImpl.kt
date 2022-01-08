@file:Suppress("unused")

package net.arwix.urania.core.transformation.precession

import net.arwix.urania.core.Ecliptic
import net.arwix.urania.core.Equatorial
import net.arwix.urania.core.arcToRad
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.jT
import net.arwix.urania.core.calendar.times
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.ARCSEC_TO_RAD
import net.arwix.urania.core.math.angle.Radian.Companion.PI2
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.vector.Matrix
import net.arwix.urania.core.math.vector.Matrix.Companion.AXIS_X
import net.arwix.urania.core.math.vector.Matrix.Companion.AXIS_Z
import net.arwix.urania.core.math.vector.RectangularVector
import kotlin.math.cos
import kotlin.math.sin

fun Precession.createElements(jT: JT): PrecessionElements {
    return when (plane) {
        Plane.Ecliptic -> {
            val matrix = createEclipticPrecessionMatrix(this@createElements, jT)
            val transposeMatrix = createEclipticPrecessionMatrix(this@createElements, jT)
            object : PrecessionElements {
                override val id: Precession = this@createElements
                override val jT: JT = jT
                override val fromJ2000Matrix: Matrix = matrix
                override val toJ2000Matrix: Matrix = transposeMatrix
            }
        }
        Plane.Equatorial -> {
            val matrix = createEquatorialPrecessionMatrix(this@createElements, jT)
            val transposeMatrix = createEquatorialTransposePrecessionMatrix(this@createElements, jT)

            object : PrecessionElements {
                override val id: Precession = this@createElements
                override val jT: JT = jT
                override val fromJ2000Matrix: Matrix = matrix
                override val toJ2000Matrix: Matrix = transposeMatrix
            }
        }
        Plane.Topocentric -> throw IllegalStateException()
    }
}

@Ecliptic
private fun createEclipticPrecessionMatrix(precession: Precession, jT: JT): Matrix {
    return when (precession) {
        Precession.IAU1976 -> createEclipticMatrix(IAU_1976_Matrices, jT, true)
        Precession.Laskar1986 -> createEclipticMatrix(LASKAR_1986_Matrices, jT, true)
        Precession.Williams1994 -> createEclipticMatrix(WILLIAMS_1994_Matrices, jT, true)
        Precession.Simon1994 -> createEclipticMatrix(SIMON_1994_Matrices, jT, true)
        Precession.DE4xxx -> createEclipticMatrix(JPL_DE4xx_Matrices, jT, true)
        else -> throw IndexOutOfBoundsException()
    }
}

@Ecliptic
private fun createEclipticTransposePrecessionMatrix(precession: Precession, jT: JT): Matrix {
    return when (precession) {
        Precession.IAU1976 -> createEclipticMatrix(IAU_1976_Matrices, jT, false)
        Precession.Laskar1986 -> createEclipticMatrix(LASKAR_1986_Matrices, jT, false)
        Precession.Williams1994 -> createEclipticMatrix(WILLIAMS_1994_Matrices, jT, false)
        Precession.Simon1994 -> createEclipticMatrix(SIMON_1994_Matrices, jT, false)
        Precession.DE4xxx -> createEclipticMatrix(JPL_DE4xx_Matrices, jT, false)
        else -> throw IndexOutOfBoundsException()
    }
}

@Equatorial
private fun createEquatorialPrecessionMatrix(precession: Precession, jT: JT): Matrix {
    return when (precession) {
        Precession.IAU2000 -> createPrecessionIAU2000Matrix(jT, true)
        Precession.IAU2006 -> createIAU2006Matrix(jT, true)
        Precession.IAU2009 -> createIAU2006Matrix(jT, true)
        Precession.Vondrak2011 -> createVondrakMatrix(jT, true)
        else -> throw IndexOutOfBoundsException()
    }
}

@Equatorial
private fun createEquatorialTransposePrecessionMatrix(precession: Precession, jT: JT): Matrix {
    return when (precession) {
        Precession.IAU2000 -> createPrecessionIAU2000Matrix(jT, false)
        Precession.IAU2006 -> createIAU2006Matrix(jT, false)
        Precession.IAU2009 -> createIAU2006Matrix(jT, false)
        Precession.Vondrak2011 -> createVondrakMatrix(jT, false)
        else -> throw IndexOutOfBoundsException()
    }
}


@Equatorial
private fun createPrecessionIAU2000Matrix(jT: JT, isFromJ2000ToApparent: Boolean): Matrix {
    val t0: JT = if (isFromJ2000ToApparent) jT else 0.0.jT
    val innerJT = if (!isFromJ2000ToApparent) -jT else jT
    val eps0 = 84381.448
    val psiA = (((-0.001147 * innerJT - 1.07259) * innerJT + 5038.7784) * innerJT - 0.29965 * t0).arcToRad()
    val omegaA = (((-0.007726 * innerJT + 0.05127) * innerJT - 0.0) * innerJT + eps0 - 0.02524 * t0).arcToRad()
    val chiA = (((-0.001125 * innerJT - 2.38064) * innerJT + 10.5526) * innerJT).arcToRad()
    return Matrix(AXIS_Z, chiA) * Matrix(AXIS_X, -omegaA) * Matrix(AXIS_Z, -psiA) * Matrix(AXIS_X, eps0.arcToRad())
}

@Equatorial
private fun createIAU2006Matrix(jT: JT, isFromJ2000ToApparent: Boolean): Matrix {
    val eps0 = 84381.406
    val innerJT = if (!isFromJ2000ToApparent) -jT else jT
    val psiA =
        (((((-0.0000000951 * innerJT + 0.000132851) * innerJT - 0.00114045) * innerJT - 1.0790069) * innerJT + 5038.481507) * innerJT).arcToRad()
    val omegaA =
        (((((+0.0000003337 * innerJT - 0.000000467) * innerJT - 0.00772503) * innerJT + 0.0512623) * innerJT - 0.025754) * innerJT + eps0).arcToRad()
    val chiA =
        (((((-0.0000000560 * innerJT + 0.000170663) * innerJT - 0.00121197) * innerJT - 2.3814292) * innerJT + 10.556403) * innerJT).arcToRad()
    return Matrix(AXIS_Z, chiA) * Matrix(AXIS_X, -omegaA) * Matrix(AXIS_Z, -psiA) * Matrix(AXIS_X, eps0.arcToRad())
}

@Ecliptic
private fun createEclipticMatrix(list: Array<DoubleArray>, jT: JT, isFromJ2000ToApparent: Boolean = true): Matrix {
    val jT10 = jT / 10.0 /* thousands of years */
    val pA = (ARCSEC_TO_RAD * jT10 * list[0].fold(0.0) { acc, d -> acc * jT10 + d }).rad
    val w = (list[1].fold(0.0) { acc, d -> acc * jT10 + d }).rad
    val z = (list[2].fold(0.0) { acc, d -> acc * jT10 + d }.let { if (!isFromJ2000ToApparent) -it else it }).rad

    return Matrix(AXIS_Z, -(w + pA)) * Matrix(AXIS_X, z) * Matrix(AXIS_Z, w)
}

@Equatorial
private fun createVondrakMatrix(T: JT, isFromJ2000ToApparent: Boolean): Matrix {
    val innerJT = if (!isFromJ2000ToApparent) -T else T
    var w = PI2.value * innerJT
    val eps0 = 84381.406.arcToRad()
    val (psiA, omegaA, chiA) = (0..13).fold(RectangularVector(0.0, 0.0, 0.0)) { acc, i ->
        var a = w / xyper[i][0]
        var s = sin(a)
        var c = cos(a)
        acc.x += c * xyper[i][1] + s * xyper[i][3]
        acc.y += c * xyper[i][2] + s * xyper[i][4]

        a = w / zper[i][0]
        s = sin(a)
        c = cos(a)
        acc.z += c * zper[i][1] + s * zper[i][2]
        return@fold acc
    }.let {
        w = 1.0
        for (j in 0..3) {
            it.x += xypol[0][j] * w
            it.y += xypol[1][j] * w
            it.z += xypol[2][j] * w
            w *= innerJT
        }
        return@let it
    } * ARCSEC_TO_RAD

    // COMPUTE ELEMENTS OF PRECESSION ROTATION MATRIX
    // EQUIVALENT TO R3(CHI_A)R1(-OMEGA_A)R3(-PSI_A)R1(EPSILON_0)
    return Matrix(AXIS_Z, chiA.rad) * Matrix(AXIS_X, -omegaA.rad) * Matrix(AXIS_Z, -psiA.rad) * Matrix(AXIS_X, eps0)
}

@Ecliptic
private val WILLIAMS_1994_Matrices by lazy {
    arrayOf(
        doubleArrayOf(
            -8.66e-10,
            -4.759e-8,
            2.424e-7,
            1.3095e-5,
            1.7451e-4,
            -1.8055e-3,
            -0.235316,
            0.076,
            110.5407,
            50287.70000
        ),
        /* Pi from Williams' 1994 paper, in radians. */
        doubleArrayOf(
            6.6402e-16,
            -2.69151e-15,
            -1.547021e-12,
            7.521313e-12,
            1.9e-10,
            -3.54e-9,
            -1.8103e-7,
            1.26e-7,
            7.436169e-5,
            -0.04207794833,
            3.052115282424
        ),
        doubleArrayOf(
            1.2147e-16,
            7.3759e-17,
            -8.26287e-14,
            2.503410e-13,
            2.4650839e-11,
            -5.4000441e-11,
            1.32115526e-9,
            -6.012e-7,
            -1.62442e-5,
            0.00227850649,
            0.0
        )
    )
}

@Ecliptic
private val JPL_DE4xx_Matrices by lazy {
    arrayOf(
        doubleArrayOf(
            -8.66e-10,
            -4.759e-8,
            2.424e-7,
            1.3095e-5,
            1.7451e-4,
            -1.8055e-3,
            -0.235316,
            0.076,
            110.5414,
            50287.91959
        ),
        /* Pi from Williams' 1994 paper, in radians. No change in DE403. */
        doubleArrayOf(
            6.6402e-16,
            -2.69151e-15,
            -1.547021e-12,
            7.521313e-12,
            1.9e-10,
            -3.54e-9,
            -1.8103e-7,
            1.26e-7,
            7.436169e-5,
            -0.04207794833,
            3.052115282424
        ),
        doubleArrayOf(
            1.2147e-16,
            7.3759e-17,
            -8.26287e-14,
            2.503410e-13,
            2.4650839e-11,
            -5.4000441e-11,
            1.32115526e-9,
            -6.012e-7,
            -1.62442e-5,
            0.00227850649,
            0.0
        )
    )
}

@Ecliptic
private val SIMON_1994_Matrices by lazy {
    arrayOf(
        doubleArrayOf(
            -8.66e-10,
            -4.759e-8,
            2.424e-7,
            1.3095e-5,
            1.7451e-4,
            -1.8055e-3,
            -0.235316,
            0.07732,
            111.2022,
            50288.200
        ),
        doubleArrayOf(
            6.6402e-16,
            -2.69151e-15,
            -1.547021e-12,
            7.521313e-12,
            1.9e-10,
            -3.54e-9,
            -1.8103e-7,
            2.579e-8,
            7.4379679e-5,
            -0.0420782900,
            3.0521126906
        ),
        doubleArrayOf(
            1.2147e-16,
            7.3759e-17,
            -8.26287e-14,
            2.503410e-13,
            2.4650839e-11,
            -5.4000441e-11,
            1.32115526e-9,
            -5.99908e-7,
            -1.624383e-5,
            0.002278492868,
            0.0
        )
    )
}

@Ecliptic
private val LASKAR_1986_Matrices by lazy {
    arrayOf(
        doubleArrayOf(
            -8.66e-10,
            -4.759e-8,
            2.424e-7,
            1.3095e-5,
            1.7451e-4,
            -1.8055e-3,
            -0.235316,
            0.07732,
            111.1971,
            50290.966
        ),
        doubleArrayOf(
            6.6402e-16,
            -2.69151e-15,
            -1.547021e-12,
            7.521313e-12,
            6.3190131e-10,
            -3.48388152e-9,
            -1.813065896e-7,
            2.75036225e-8,
            7.4394531426e-5,
            -0.042078604317,
            3.052112654975
        ),
        doubleArrayOf(
            1.2147e-16,
            7.3759e-17,
            -8.26287e-14,
            2.503410e-13,
            2.4650839e-11,
            -5.4000441e-11,
            1.32115526e-9,
            -5.998737027e-7,
            -1.6242797091e-5,
            0.002278495537,
            0.0
        )
    )
}

@Ecliptic
private val IAU_1976_Matrices by lazy {
    arrayOf(
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.006, 111.113, 50290.966),
        doubleArrayOf(
            6.6402e-16,
            -2.69151e-15,
            -1.547021e-12,
            7.521313e-12,
            6.3190131e-10,
            -3.48388152e-9,
            -1.813065896e-7,
            2.75036225e-8,
            7.4394531426e-5,
            -0.042078604317,
            3.052112654975
        ),
        doubleArrayOf(
            1.2147e-16,
            7.3759e-17,
            -8.26287e-14,
            2.503410e-13,
            2.4650839e-11,
            -5.4000441e-11,
            1.32115526e-9,
            -5.998737027e-7,
            -1.6242797091e-5,
            0.002278495537,
            0.0
        )
    )
}

private val xypol by lazy {
    arrayOf(
        doubleArrayOf(8473.343527, 5042.7980307, -0.00740913, 289E-9),
        doubleArrayOf(84283.175915, -0.4436568, 0.00000146, 151E-9),
        doubleArrayOf(-19.657270, 0.0790159, 0.00001472, -61E-9)
    )
}

private val xyper by lazy {
    arrayOf(
        doubleArrayOf(402.90, -22206.325946, 1267.727824, -3243.236469, -8571.476251),
        doubleArrayOf(256.75, 12236.649447, 1702.324248, -3969.723769, 5309.796459),
        doubleArrayOf(292.00, -1589.008343, -2970.553839, 7099.207893, -610.393953),
        doubleArrayOf(537.22, 2482.103195, 693.790312, -1903.696711, 923.201931),
        doubleArrayOf(241.45, 150.322920, -14.724451, 146.435014, 3.759055),
        doubleArrayOf(375.22, -13.632066, -516.649401, 1300.630106, -40.691114),
        doubleArrayOf(157.87, 389.437420, -356.794454, 1727.498039, 80.437484),
        doubleArrayOf(274.20, 2031.433792, -129.552058, 299.854055, 807.300668),
        doubleArrayOf(203.00, 363.748303, 256.129314, -1217.125982, 83.712326),
        doubleArrayOf(440.00, -896.747562, 190.266114, -471.367487, -368.654854),
        doubleArrayOf(170.72, -926.995700, 95.103991, -441.682145, -191.881064),
        doubleArrayOf(713.37, 37.070667, -332.907067, -86.169171, -4.263770),
        doubleArrayOf(313.00, -597.682468, 131.337633, -308.320429, -270.353691),
        doubleArrayOf(128.38, 66.282812, 82.731919, -422.815629, 11.602861)
    )
}

private val zper by lazy {
    arrayOf(
        doubleArrayOf(402.90, -13765.924050, -2206.967126),
        doubleArrayOf(256.75, 13511.858383, -4186.752711),
        doubleArrayOf(292.00, -1455.229106, 6737.949677),
        doubleArrayOf(537.22, 1054.394467, -856.922846),
        doubleArrayOf(375.22, -112.300144, 957.149088),
        doubleArrayOf(157.87, 202.769908, 1709.440735),
        doubleArrayOf(274.20, 1936.050095, 154.425505),
        doubleArrayOf(202.00, 327.517465, -1049.071786),
        doubleArrayOf(440.00, -655.484214, -243.520976),
        doubleArrayOf(170.72, -891.898637, -406.539008),
        doubleArrayOf(315.00, -494.780332, -301.504189),
        doubleArrayOf(136.32, 585.492621, 41.348740),
        doubleArrayOf(128.38, -333.322021, -446.656435),
        doubleArrayOf(490.00, 110.512834, 142.525186)
    )
}
