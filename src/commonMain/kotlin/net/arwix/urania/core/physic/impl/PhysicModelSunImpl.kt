package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10

internal object PhysicModelSunImpl: Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
         return PhysicEphemeris.NorthPole(286.13.deg.toRad(), 63.87.deg.toRad())
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle : Degree): Double {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2015,
            Physic.Model.IAU2009,
            Physic.Model.IAU2018-> -26.742 + 5.0 * log10(distance * distanceFromSun)
        }
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2009,
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> 14.1844
        }
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return when (this) {
            Physic.Model.IAU2000 -> 84.1.deg
            Physic.Model.IAU2006,
            Physic.Model.IAU2009,
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> 84.176.deg
        }
    }
}