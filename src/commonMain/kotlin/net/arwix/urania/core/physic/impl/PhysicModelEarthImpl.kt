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

internal object PhysicModelEarthImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        return PhysicEphemeris.NorthPole(
            rightAscension = (0.0 - 0.641 * jT).deg.toRad(),
            declination = (90.0 - 0.557 * jT).deg.toRad()
        )
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return if (this == Physic.Model.IAU2018 && phaseAngle <= 170.deg) {
            -3.99 + 5.0 * log10(distance * distanceFromSun) -1.060e-03 * phaseAngle + 2.054e-04 * phaseAngle * phaseAngle
        } else -3.86 + 5.0 * log10(distance * distanceFromSun)
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return 360.9856235
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return 190.147.deg
    }
}