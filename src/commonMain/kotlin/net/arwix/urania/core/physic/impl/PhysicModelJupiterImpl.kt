package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.times
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10

internal object PhysicModelJupiterImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        return when (this) {
            Physic.Model.IAU2000 -> PhysicEphemeris.NorthPole(
                rightAscension = (268.05 - 0.009 * jT).deg.toRad(),
                declination = (64.49 + 0.003 * jT).deg.toRad()
            )
            Physic.Model.IAU2006,
            Physic.Model.IAU2009,
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> {
                val a = (99.360714 + 4850.4046 * jT).deg.toRad()
                val b = (175.895369 + 1191.9605 * jT).deg.toRad()
                val c = (300.323162 + 262.5475 * jT).deg.toRad()
                val d = (114.012305 + 6070.2476 * jT).deg.toRad()
                val e = (49.511251 + 64.3 * jT).deg.toRad()
                PhysicEphemeris.NorthPole(
                    rightAscension = (268.056595 - 0.006499 * jT).deg.toRad() + (
                            0.000117 * sin(a) +
                                    0.000938 * sin(b) +
                                    0.001432 * sin(c) +
                                    0.00003 * sin(d) +
                                    0.00215 * sin(e)).deg.toRad(),
                    declination = (64.495303 + 0.002413 * jT).deg.toRad() + (
                            0.00005 * cos(a) +
                                    0.000404 * cos(b) +
                                    0.000617 * cos(c) -
                                    0.000013 * cos(d) +
                                    0.000926 * cos(e)).deg.toRad()
                )
            }
        }
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return -9.25 + 5.0 * log10(distance * distanceFromSun) + 0.005 * phaseAngle
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006 -> 870.536642
            Physic.Model.IAU2009,
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> 870.536
        }
    }

    /**
     * for system III
     */
    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2009,
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> 284.95.deg
        }
    }
}