@file:Suppress("unused")

package net.arwix.urania.core.physic

import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.sin
import kotlin.math.pow

interface Ellipsoid {
    val equatorialRadius: Double
    val inverseFlatteningFactor: Double

    fun getPolarRadius(): Double {
        return equatorialRadius - equatorialRadius / inverseFlatteningFactor
    }

    fun getRadiusAtLatitude(latitude: Radian): Double {
        return equatorialRadius * (1.0 - sin(latitude).pow(2.0) / inverseFlatteningFactor)
    }
}

sealed class EllipsoidObject: Ellipsoid {
    object EarthWGS72: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.135
        override val inverseFlatteningFactor: Double = 298.26
    }

    object EarthWGS84: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.137
        override val inverseFlatteningFactor: Double = 298.257223563
    }

    object EarthIERS1989: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.136
        override val inverseFlatteningFactor: Double = 298.257
    }

    object EarthMERIT1983: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.137
        override val inverseFlatteningFactor: Double = 298.257
    }

    object EarthGRS80: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.137
        override val inverseFlatteningFactor: Double = 298.257222101
    }

    object EarthGRS67: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.160
        override val inverseFlatteningFactor: Double = 298.247167
    }

    object EarthIAU1964: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.160
        override val inverseFlatteningFactor: Double = 298.25
    }

    object EarthIAU1976: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.140
        override val inverseFlatteningFactor: Double = 298.257
    }

    object EarthIERS2003: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.1366
        override val inverseFlatteningFactor: Double = 298.25642
    }

    object Earth: EllipsoidObject() {
        override val equatorialRadius: Double = 6378.1366
        override val inverseFlatteningFactor: Double = 298.25642
    }

    object Mercury: EllipsoidObject() {
        override val equatorialRadius: Double = 2440.53
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 2438.26)
    }

    object Venus: EllipsoidObject() {
        override val equatorialRadius: Double = 6051.8
        override val inverseFlatteningFactor: Double = 1.0
    }

    object Moon: EllipsoidObject() {
        override val equatorialRadius: Double = 1737.4
        override val inverseFlatteningFactor: Double = 1.0
    }

    object Mars: EllipsoidObject() {
        override val equatorialRadius: Double = 3396.19
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 3376.2)
    }

    object Jupiter: EllipsoidObject() {
        override val equatorialRadius: Double = 71492.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 66854.0)
    }

    object Saturn: EllipsoidObject() {
        override val equatorialRadius: Double = 60268.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 54364.0)
    }

    object Uranus: EllipsoidObject() {
        override val equatorialRadius: Double = 25559.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 24973.0)
    }

    object Neptune: EllipsoidObject() {
        override val equatorialRadius: Double = 24764.0
        override val inverseFlatteningFactor: Double = equatorialRadius / (equatorialRadius - 24341.0)
    }

    object Pluto: EllipsoidObject() {
        override val equatorialRadius: Double = 1188.3
        override val inverseFlatteningFactor: Double = 1.0
    }

    data class Custom(
        override val equatorialRadius: Double,
        override val inverseFlatteningFactor: Double) : EllipsoidObject()
}

