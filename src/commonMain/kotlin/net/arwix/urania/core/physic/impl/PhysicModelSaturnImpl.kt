package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.times
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.times
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10

internal object PhysicModelSaturnImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        return PhysicEphemeris.NorthPole(
                rightAscension = (40.589 - 0.036 * jT).deg.toRad(),
                declination = (83.537 - 0.004 * jT).deg.toRad()
            )
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return -8.88 + 5.0 * log10(distance * distanceFromSun) + 0.044 * phaseAngle
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return 810.7939024
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return 38.9.deg
    }
}