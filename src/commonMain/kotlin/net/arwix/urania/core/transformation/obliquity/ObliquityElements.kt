package net.arwix.urania.core.transformation.obliquity

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.ephemeris.EphemerisVector
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.vector.Matrix
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.transformation.nutation.NutationElements

@Suppress("unused")
interface ObliquityElements {

    val id: Obliquity
    val jT: JT

    val meanObliquity: Radian
    val eclipticToEquatorialMatrix: Matrix
    val equatorialToEclipticMatrix: Matrix

    fun rotatePlane(vector: Vector, toPlane: Plane): Vector

    fun rotatePlane(ephemerisVector: EphemerisVector): EphemerisVector {
        val toPlane = when (ephemerisVector.metadata.plane) {
            Plane.Ecliptic -> Plane.Equatorial
            Plane.Equatorial -> Plane.Ecliptic
            else -> throw IllegalStateException()
        }
        return ephemerisVector.copy(
            value = rotatePlane(ephemerisVector.value, toPlane),
            metadata = ephemerisVector.metadata.copy(plane = toPlane)
        )
    }

    fun getTrueObliquity(nutationAngles: NutationElements.NutationAngles): Radian {
        return meanObliquity + nutationAngles.deltaObliquity
    }

//    fun Vector.rotatePlane(toPlane: Plane): Vector {
//        return rotatePlane(this, toPlane)
//    }

    fun EphemerisVector.rotatePlane(toPlane: Plane): EphemerisVector {
        return rotatePlane(this)
    }
}

sealed class Obliquity {

    /** Laskar et al.
     *
     * This expansion is from Laskar, cited above. Bretagnon and Simon say,
     * in Planetary Programs and Tables, that it is accurate to 0.1" over a
     * span of 6000 years. Laskar estimates the precision to be 0.01" after
     * 1000 years and a few seconds of arc after 10000 years.
     */
    object Laskar1986 : Obliquity()

    // Williams et al., DE403 Ephemeris
    object Williams1994 : Obliquity()

    // Simon et al., 1994
    object Simon1994 : Obliquity()
    object IAU1976 : Obliquity()

    // Capitaine et al. 2003, Hilton et al. 2006
    object IAU2006 : Obliquity()
    object Vondrak2011 : Obliquity()

}