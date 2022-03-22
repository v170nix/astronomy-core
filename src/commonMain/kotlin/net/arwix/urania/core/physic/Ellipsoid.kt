@file:Suppress("unused")

package net.arwix.urania.core.physic

import net.arwix.urania.core.math.AU
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.angle.sin
import kotlin.math.atan
import kotlin.math.pow

interface Ellipsoid {
    val equatorialRadius: Double
    val inverseFlatteningFactor: Double
    val relativeMass: Double

    fun getPolarRadius(): Double {
        return equatorialRadius - equatorialRadius / inverseFlatteningFactor
    }

    fun getRadiusAtLatitude(latitude: Radian): Double {
        return equatorialRadius * (1.0 - sin(latitude).pow(2.0) / inverseFlatteningFactor)
    }

    fun getAngularRadius(distanceInKm: Double): Radian {
        return atan(equatorialRadius / distanceInKm / AU).rad
    }
}

sealed class EllipsoidObject: Ellipsoid {

    object Sun: EllipsoidObject() {
        override val equatorialRadius: Double = 696000.0
        override val inverseFlatteningFactor: Double = 1.0
        override val relativeMass: Double = 1.0
    }

    object Mercury: EllipsoidObject() {
        override val equatorialRadius: Double = 2440.53
        // 2439.7
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 2438.26)
        override val relativeMass: Double = 6023682.0
    }

    object Venus: EllipsoidObject() {
        override val equatorialRadius: Double = 6051.8
        override val inverseFlatteningFactor: Double = 1.0
        override val relativeMass: Double = 408523.72
    }

    object EarthWGS72: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.135
        override val inverseFlatteningFactor: Double = 298.26
        override val relativeMass: Double = 332946.050895
    }

    object EarthWGS84: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.137
        override val inverseFlatteningFactor: Double = 298.257223563
        override val relativeMass: Double = 332946.050895
    }

    object EarthIERS1989: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.136
        override val inverseFlatteningFactor: Double = 298.257
        override val relativeMass: Double = 332946.050895
    }

    object EarthMERIT1983: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.137
        override val inverseFlatteningFactor: Double = 298.257
        override val relativeMass: Double = 332946.050895
    }

    object EarthGRS80: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.137
        override val inverseFlatteningFactor: Double = 298.257222101
        override val relativeMass: Double = 332946.050895
    }

    object EarthGRS67: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.160
        override val inverseFlatteningFactor: Double = 298.247167
        override val relativeMass: Double = 332946.050895
    }

    object EarthIAU1964: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.160
        override val inverseFlatteningFactor: Double = 298.25
        override val relativeMass: Double = 332946.050895
    }

    object EarthIAU1976: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.140
        override val inverseFlatteningFactor: Double = 298.257
        override val relativeMass: Double = 332946.050895
    }

    object EarthIERS2003: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.1366
        override val inverseFlatteningFactor: Double = 298.25642
        override val relativeMass: Double = 332946.050895
    }

    object Earth: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.1366
        override val inverseFlatteningFactor: Double = 298.25642
        override val relativeMass: Double = 332946.050895
    }

    object Moon: EllipsoidObject() {
        override val equatorialRadius: Double = 1737.4
        override val inverseFlatteningFactor: Double = 1.0
        override val relativeMass: Double = 2.7068700387534E7

    }

    object Mars: EllipsoidObject() {
        override val equatorialRadius: Double = 3396.19
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 3376.2)
        override val relativeMass: Double = 3098703.59
    }

    object Phobos: EllipsoidObject() {
        override val equatorialRadius: Double = 13.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 9.1)
        override val relativeMass: Double = 0.0
    }

    object Deimos: EllipsoidObject() {
        override val equatorialRadius: Double = 7.8
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 5.1)
        override val relativeMass: Double = 0.0
    }

    object Jupiter: EllipsoidObject() {
        override val equatorialRadius: Double = 71492.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 66854.0)
        override val relativeMass: Double = 1047.3486
    }

    object Saturn: EllipsoidObject() {
        override val equatorialRadius: Double = 60268.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 54364.0)
        override val relativeMass: Double = 3497.898
    }

    object Uranus: EllipsoidObject() {
        override val equatorialRadius: Double = 25559.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 24973.0)
        override val relativeMass: Double = 22902.98
    }

    object Neptune: EllipsoidObject() {
        override val equatorialRadius: Double = 24764.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 24341.0)
        override val relativeMass: Double = 19412.24
    }

    object Pluto: EllipsoidObject() {
        override val equatorialRadius: Double = 1188.3
        override val inverseFlatteningFactor: Double = 1.0
        override val relativeMass: Double = 1.352E8
    }

    object None: EllipsoidObject() {
        override val equatorialRadius: Double = Double.NaN
        override val inverseFlatteningFactor = Double.NaN
        override val relativeMass = Double.NaN

    }

    data class Custom(
        override val equatorialRadius: Double,
        override val inverseFlatteningFactor: Double,
        override val relativeMass: Double
    ) : EllipsoidObject()
}

