package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.times
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10
import kotlin.math.pow

internal object PhysicModelVenusImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        return PhysicEphemeris.NorthPole(
            rightAscension = 272.76.deg.toRad(),
            declination = 67.16.deg.toRad()
        )
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return if (this == Physic.Model.IAU2018 && phaseAngle >= 2.deg && phaseAngle <= 179.deg) {
            -4.384 + 5.0 * log10(distance * distanceFromSun) + if (phaseAngle < 163.7.deg) {
                -1.044E-03 * phaseAngle +
                        3.687E-04 * phaseAngle * phaseAngle -
                        2.814E-06 * phaseAngle.value.pow(3.0) +
                        8.938E-09 * phaseAngle.value.pow(4.0)
            } else {
                236.05828 + 4.384 - 2.81914E+00 * phaseAngle + 8.39034E-03 * phaseAngle * phaseAngle
            }
        } else -4.29 + 5.0 * log10(distance * distanceFromSun) +
                0.0009 * phaseAngle +
                0.000239 * phaseAngle * phaseAngle -
                6.5E-7 * phaseAngle.value.pow(3.0)
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return -1.4813688
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return 160.20.deg
    }
}