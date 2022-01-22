package net.arwix.urania.core.transformation.nutation

import net.arwix.urania.core.annotation.Ecliptic
import net.arwix.urania.core.annotation.Equatorial
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.times
import net.arwix.urania.core.ephemeris.EphemerisVector
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.vector.Matrix
import net.arwix.urania.core.math.vector.Matrix.Companion.AXIS_X
import net.arwix.urania.core.math.vector.Matrix.Companion.AXIS_Z
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.math.vector.times
import net.arwix.urania.core.rectangular
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.getEps

interface NutationElements {

    data class NutationAngles(val jT: JT, val deltaLongitude: Radian, val deltaObliquity: Radian)

    val id: Nutation
    val jT: JT
    val angles: NutationAngles

    @Ecliptic
    val eclipticMatrix: Matrix
    @Equatorial
    val equatorialMatrix: Matrix?

    fun apply(vector: Vector, currentPlane: Plane): Vector
    fun remove(vector: Vector, currentPlane: Plane): Vector

    fun apply(ephemerisVector: EphemerisVector): EphemerisVector {
        return ephemerisVector.copy(
            value = apply(ephemerisVector.value, ephemerisVector.metadata.plane)
        )
    }
    fun remove(ephemerisVector: EphemerisVector): EphemerisVector {
        return ephemerisVector.copy(
            value = remove(ephemerisVector.value, ephemerisVector.metadata.plane)
        )
    }

    fun Vector.applyNutation(currentPlane: Plane): Vector {
        return apply(this, currentPlane)
    }

    fun Vector.removeNutation(currentPlane: Plane): Vector {
        return remove(this, currentPlane)
    }

    fun EphemerisVector.applyNutation(): EphemerisVector {
        return apply(this)
    }

    fun EphemerisVector.removeNutation(): EphemerisVector {
        return remove(this)
    }
}

sealed class Nutation {
    object IAU1980 : Nutation()
    object IAU2000 : Nutation()
    object IAU2006 : Nutation()
}

fun Nutation.getNutationAngles(jT: JT): NutationElements.NutationAngles {
    return when (this) {
        Nutation.IAU1980 -> getNutationAnglesIAU1980(jT)
        Nutation.IAU2000 -> getNutationAnglesIAU2000(jT)
        Nutation.IAU2006 -> getNutationAnglesIAU2000(jT).let {
            NutationElements.NutationAngles(
                jT,
                it.deltaLongitude * (1.0 + (0.4697E-6 - 2.7774E-6 * jT)),
                it.deltaObliquity * (1.0 + (2.7774E-6 * jT))
            )
        }
        else -> throw IndexOutOfBoundsException()
    }
}

fun getNutationMatrix(angles: NutationElements.NutationAngles, plane: Plane, obliquity: Obliquity?): Matrix {
    return when (plane) {
        Plane.Ecliptic -> getEclipticNutationMatrix(angles)
        Plane.Equatorial -> {
            if (obliquity == null) throw IllegalArgumentException()
            getEquatorialNutationMatrix(angles, obliquity)
        }
        else -> throw IllegalArgumentException()
    }
}

@Ecliptic
fun getEclipticNutationMatrix(angles: NutationElements.NutationAngles): Matrix {
    return Matrix(AXIS_X, -angles.deltaObliquity) * Matrix(AXIS_Z, -angles.deltaLongitude)
}

@Equatorial
fun getEquatorialNutationMatrix(angles: NutationElements.NutationAngles, obliquity: Obliquity): Matrix {
    return Matrix(AXIS_X, -obliquity.getEps(angles.jT) - angles.deltaObliquity) *
            Matrix(AXIS_Z, -angles.deltaLongitude) *
            Matrix(AXIS_X, obliquity.getEps(angles.jT))
}

fun Nutation.createElements(jt: JT, obliquity: Obliquity?): NutationElements {
    val angles = getNutationAngles(jt)
    val eclipticMatrix = getEclipticNutationMatrix(angles)
    val equatorialMatrix = obliquity?.let { getEquatorialNutationMatrix(angles, it) }

    return object : NutationElements {
        override val id: Nutation = this@createElements
        override val jT: JT = jt
        override val angles: NutationElements.NutationAngles = angles
        override val eclipticMatrix: Matrix = eclipticMatrix
        override val equatorialMatrix: Matrix? = equatorialMatrix

        override fun apply(vector: Vector, currentPlane: Plane): Vector {
            return when (currentPlane) {
                Plane.Ecliptic -> this.eclipticMatrix * vector
                Plane.Equatorial -> this.equatorialMatrix?.let { it * vector } ?: throw IllegalStateException()
                else -> throw IllegalArgumentException()
            }
        }

        override fun remove(vector: Vector, currentPlane: Plane): Vector {
            return when (currentPlane) {
                Plane.Ecliptic -> vector.rectangular * this.eclipticMatrix
                Plane.Equatorial -> this.equatorialMatrix?.let { vector.rectangular * it } ?: throw IllegalStateException()
                else -> throw IllegalArgumentException()
            }
        }

    }
}