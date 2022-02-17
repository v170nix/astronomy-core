package net.arwix.urania.core.physic

import net.arwix.urania.core.annotation.Apparent
import net.arwix.urania.core.annotation.Ecliptic
import net.arwix.urania.core.annotation.Geocentric
import net.arwix.urania.core.ephemeris.EphemerisVector
import net.arwix.urania.core.ephemeris.Orbit
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.spherical
import kotlin.math.acos

data class PhysicAngles(
    val elongation: Radian,
    val relative: Relative,
    val phaseAngle: Radian,
    val phase: Double
) {

    enum class Relative { Trails, Leads }


    companion object {

        fun getAngles(
            @Apparent @Geocentric @Ecliptic sunVector: Vector, // observer-sun vector
            @Apparent @Geocentric @Ecliptic targetObject: Vector, // observer-body vector
        ): PhysicAngles {
            val helioObject = (sunVector - targetObject).spherical // sun-body vector
            val re = sunVector.normalize()
            val rp = helioObject.r
            val ro = targetObject.normalize()
            val lp = helioObject.phi
            val le = Radian.PI + targetObject.spherical.phi

            val cosPhi = (ro * ro + rp * rp - re * re) / (2.0 * ro * rp)
            val sinPhi = re * sin(le - lp) / ro

            return PhysicAngles(
                elongation = acos((ro * ro + re * re - rp * rp) / (2.0 * ro * re)).rad,
                relative = if (sinPhi < 0) Relative.Trails else Relative.Leads,
                phaseAngle = acos(cosPhi).rad,
                phase = (0.5 * (1.0 + cosPhi))
            )
        }

        fun getAngles(
            @Apparent @Geocentric @Ecliptic sunVector: EphemerisVector, // observer-sun vector
            @Apparent @Geocentric @Ecliptic targetObject: EphemerisVector, // observer-body vector
        ): PhysicAngles {

            if (sunVector.metadata.orbit != Orbit.Geocentric ||
                        sunVector.metadata.plane != Plane.Ecliptic ||
                        sunVector.metadata.epoch != targetObject.metadata.epoch ||
                        targetObject.metadata.orbit != Orbit.Geocentric ||
                        targetObject.metadata.plane != Plane.Ecliptic
            ) throw IllegalArgumentException()

            val helioObject = (sunVector.value - targetObject.value).spherical // sun-body vector
            val re = sunVector.value.normalize()
            val rp = helioObject.r
            val ro = targetObject.value.normalize()
            val lp = helioObject.phi
            val le = Radian.PI + targetObject.value.spherical.phi

            val cosPhi = (ro * ro + rp * rp - re * re) / (2.0 * ro * rp)
            val sinPhi = re * sin(le - lp) / ro

            return PhysicAngles(
                elongation = acos((ro * ro + re * re - rp * rp) / (2.0 * ro * re)).rad,
                relative = if (sinPhi < 0) Relative.Trails else Relative.Leads,
                phaseAngle = acos(cosPhi).rad,
                phase = (0.5 * (1.0 + cosPhi))
            )
        }
    }
}