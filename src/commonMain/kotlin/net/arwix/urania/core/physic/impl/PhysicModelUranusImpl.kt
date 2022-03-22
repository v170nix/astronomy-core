package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10

internal object PhysicModelUranusImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        return PhysicEphemeris.NorthPole(
                rightAscension = 257.311.deg.toRad(),
                declination = (-15.175).deg.toRad()
        )
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return -7.19 + 5.0 * log10(distance * distanceFromSun)
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return -501.1600928
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return 203.81.deg
    }
}