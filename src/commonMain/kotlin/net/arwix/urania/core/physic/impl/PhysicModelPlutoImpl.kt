package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10

internal object PhysicModelPlutoImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        return when (this) {
            Physic.Model.IAU2000 -> PhysicEphemeris.NorthPole(
                rightAscension = 313.02.deg.toRad(),
                declination = 9.09.deg.toRad()
            )
            Physic.Model.IAU2006 -> PhysicEphemeris.NorthPole(
                rightAscension = 312.993.deg.toRad(),
                declination = 6.163.deg.toRad()
            )
            Physic.Model.IAU2009,
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> PhysicEphemeris.NorthPole(
                rightAscension = 132.993.deg.toRad(),
                declination = (-6.163).deg.toRad()
            )
        }
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return -1.01 + 5.0 * log10(distance * distanceFromSun)
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        if (this == Physic.Model.IAU2000) return -56.3623195
        return 56.3625225
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        if (this == Physic.Model.IAU2000) return 236.77.deg
        return 302.695.deg
    }
}