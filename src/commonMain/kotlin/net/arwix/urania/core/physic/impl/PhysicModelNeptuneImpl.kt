package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.times
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.math.angle.cos
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10

internal object PhysicModelNeptuneImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        val p = (357.85 + 52.316 * jT).deg.toRad()
        return PhysicEphemeris.NorthPole(
            rightAscension = (299.36 + 0.70 * sin(p)).deg.toRad(),
            declination = (43.46 - 0.51 * cos(p)).deg.toRad()
        )
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return -6.87 + 5.0 * log10(distance * distanceFromSun)
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2009 -> 536.3128492
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> 541.1397757
        }
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2009 -> {
                val p = (357.85 + 52.316 * jT).deg.toRad()
                (253.18 - 0.48 * sin(p)).deg
            }
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> {
                val p = (357.85 + 52.316 * jT).deg.toRad()
                (249.978 - 0.48 * sin(p)).deg
            }
        }
    }
}