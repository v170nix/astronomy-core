package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.times
import net.arwix.urania.core.calendar.toMJD
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.math.angle.times
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10
import kotlin.math.pow

internal object PhysicModelMercuryImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006 -> PhysicEphemeris.NorthPole(
                rightAscension = (281.01 - 0.033 * jT).deg.toRad(),
                declination = (61.45 - 0.005 * jT).deg.toRad()
            )
            Physic.Model.IAU2009 -> PhysicEphemeris.NorthPole(
                rightAscension = (281.0097 - 0.0328 * jT).deg.toRad(),
                declination = (61.4143 - 0.0049 * jT).deg.toRad()
            )
            Physic.Model.IAU2015,
            Physic.Model.IAU2018 -> PhysicEphemeris.NorthPole(
                rightAscension = (281.0103 - 0.0328 * jT).deg.toRad(),
                declination = (61.4155 - 0.0049 * jT).deg.toRad()
            )
        }
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return if (this == Physic.Model.IAU2018 && phaseAngle >= 2.deg && phaseAngle <= 170.deg) {
            -0.613 + 5.0 * log10(distance * distanceFromSun) +
                    6.3280e-02 * phaseAngle -
                    1.6336e-03 * phaseAngle.value.pow(2.0) +
                    3.3644e-05 * phaseAngle.value.pow(3.0) -
                    3.4265e-07 * phaseAngle.value.pow(4.0) +
                    1.6893e-09 * phaseAngle.value.pow(5.0) -
                    3.0334e-12 * phaseAngle.value.pow(6.0)
        } else -0.36 + 5.0 * log10(distance * distanceFromSun) +
                0.038 * phaseAngle -
                0.000273 * phaseAngle * phaseAngle +
                2.0E-6 * phaseAngle.value.pow(3.0)
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006,
            Physic.Model.IAU2009 -> 6.1385025
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> 6.1385108
        }
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        return when (this) {
            Physic.Model.IAU2000,
            Physic.Model.IAU2006 -> 329.548.deg
            Physic.Model.IAU2009 -> {
                val mjd = jT.toMJD()
                val m1 = (174.791086 + 4.092335 * mjd).deg.toRad()
                val m2 = (349.582171 + 8.184670 * mjd).deg.toRad()
                val m3 = (164.373257 + 12.277005 * mjd).deg.toRad()
                val m4 = (339.164343 + 16.369340 * mjd).deg.toRad()
                val m5 = (153.955429 + 20.461675 * mjd).deg.toRad()
                (329.5469
                        + 0.00993822 * sin(m1)
                        - 0.00104581 * sin(m2)
                        - 0.0001028 * sin(m3)
                        - 0.00002364 * sin(m4)
                        - 0.00000532 * sin(m5)).deg
            }
            Physic.Model.IAU2015,
            Physic.Model.IAU2018-> {
                val mjd = jT.toMJD()
                val m1 = (174.7910857 + 4.092335 * mjd).deg.toRad()
                val m2 = (349.5821714 + 8.184670 * mjd).deg.toRad()
                val m3 = (164.3732571 + 12.277005 * mjd).deg.toRad()
                val m4 = (339.1643429 + 16.36934 * mjd).deg.toRad()
                val m5 = (153.9554286 + 20.461675 * mjd).deg.toRad()
                (329.5988
                        + 0.01067257 * sin(m1)
                        - 0.00112309 * sin(m2)
                        - 0.00011040 * sin(m3)
                        - 0.00002539 * sin(m4)
                        - 0.00000571 * sin(m5)).deg
            }
        }
    }
}