package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.times
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10

internal object PhysicModelMarsImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2009 -> PhysicEphemeris.NorthPole(
                rightAscension = (317.68143 - 0.1061 * jT).deg.toRad(),
                declination = (52.8865 - 0.0609 * jT).deg.toRad()
            )
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> PhysicEphemeris.NorthPole(
                rightAscension = (317.269202 - 0.10927547 * jT +
                        0.000068 * sin((198.991226 + 19139.4819985 * jT).deg.toRad()) +
                        0.000238 * sin((226.292679 + 38280.8511281 * jT).deg.toRad()) +
                        0.000052 * sin((249.663391 + 57420.7251593 * jT).deg.toRad()) +
                        0.000009 * sin((266.183510 + 76560.6367950 * jT).deg.toRad()) +
                        0.419057 * sin((79.398797 + 0.5042615 * jT).deg.toRad())
                        ).deg.toRad(),
                declination = (54.432516 - 0.05827105 * jT +
                        0.000051 * cos((122.433576 + 19139.9407476 * jT).deg.toRad()) +
                        0.000141 * cos((43.058401 + 38280.8753272 * jT).deg.toRad()) +
                        0.000031 * cos((57.663379 + 57420.7517205 * jT).deg.toRad()) +
                        0.000005 * cos((79.476401 + 76560.6495004 * jT).deg.toRad()) +
                        1.591274 * cos((166.325722 + 0.5042615 * jT).deg.toRad())
                        ).deg.toRad()
            )
        }
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return -1.52 + 5.0 * log10(distance * distanceFromSun) + 0.016 * phaseAngle
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2009 -> 350.89198226
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> 350.891982443297
        }
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2009 -> 176.630.deg
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> {
                (176.049863 +
                        0.000145 * sin((129.071773 + 19140.0328244 * jT).deg.toRad()) +
                        0.000157 * sin((36.352167 + 38281.0473591 * jT).deg.toRad()) +
                        0.000040 * sin((56.668646 + 57420.9295360 * jT).deg.toRad()) +
                        0.000001 * sin((67.364003 + 76560.2552215 * jT).deg.toRad()) +
                        0.000001 * sin((104.792680 + 95700.4387578 * jT).deg.toRad()) +
                        0.584542 * sin((95.391654 + 0.5042615 * jT).deg.toRad())).deg
            }
        }
    }
}