package net.arwix.urania.core.physic.impl

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.MJD
import net.arwix.urania.core.calendar.times
import net.arwix.urania.core.calendar.toMJD
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.physic.PhysicEphemeris
import net.arwix.urania.core.toRad
import kotlin.math.log10
import kotlin.math.pow

// https://www.yumpu.com/en/document/read/29767823/pdf-document-orbital-and-celestial-mechanics-website
internal object PhysicModelMoonImpl : Physic.Elements {

    override fun Physic.Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole {

        val d = (jT.toMJD() - MJD.J2000).value

        val e1 = (125.045 - 0.0529921 * d).deg.toRad()
        val e2 = (250.089 - 0.1059842 * d).deg.toRad()
        val e3 = (260.008 + 13.0120009 * d).deg.toRad()
        val e4 = (176.625 + 13.3407154 * d).deg.toRad()
        val e6 = (311.589 + 26.4057084 * d).deg.toRad()
        val e7 = (134.963 + 13.0649930 * d).deg.toRad()
        val e10 = (15.134 - 0.1589763 * d).deg.toRad()
        val e13 = (25.053 + 12.9590088 * d).deg.toRad()

        return PhysicEphemeris.NorthPole(
            rightAscension = (269.9949 + 0.0031 * jT -
                    3.8787 * sin(e1) -
                    0.1204 * sin(e2) +
                    0.0700 * sin(e3) -
                    0.0172 * sin(e4) +
                    0.0072 * sin(e6) -
                    0.0052 * sin(e10) +
                    0.0043 * sin(e13)).deg.toRad(),
            declination = (66.5392 + 0.0130 * jT +
                    1.5419 * cos(e1) +
                    0.0239 * cos(e2) -
                    0.0278 * cos(e3) +
                    0.0068 * cos(e4) -
                    0.0029 * cos(e6) +
                    0.0009 * cos(e7) +
                    0.0008 * cos(e10) -
                    0.0009 * cos(e13)).deg.toRad()
        )
    }

    override fun Physic.Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double {
        return 0.23 + 5.0 * log10(distance * distanceFromSun) + 0.026 * phaseAngle + 4.0E-9 * phaseAngle.value.pow(4.0)
    }

    /**
     * @return rotation in degrees/day
     */
    override fun Physic.Model.getSpeedRotation(): Double {
        return 13.17635815
    }

    override fun Physic.Model.getLongitudeJ2000(jT: JT): Degree {
        val d = (jT.toMJD() - MJD.J2000).value
        val d2 = d * d
        val e1 = (125.045 - 0.0529921 * d).deg.toRad()
        val e2 = (250.089 - 0.1059842 * d).deg.toRad()
        val e3 = (260.008 + 13.0120009 * d).deg.toRad()
        val e4 = (176.625 + 13.3407154 * d).deg.toRad()
        val e5 = (357.529 + 0.9856003 * d).deg.toRad()
        val e6 = (311.589 + 26.4057084 * d).deg.toRad()
        val e7 = (134.963 + 13.0649930 * d).deg.toRad()
        val e8 = (276.617 + 0.3287146 * d).deg.toRad()
        val e9 = (34.226 + 1.7484877 * d).deg.toRad()
        val e10 = (15.134 - 0.1589763 * d).deg.toRad()
        val e11 = (119.743 + 0.0036096 * d).deg.toRad()
        val e12 = (239.961 + 0.1643573 * d).deg.toRad()
        val e13 = (25.053 + 12.9590088 * d).deg.toRad()

        return (38.3213 - 1.4E-12 * d2 +
                3.5610 * sin(e1) +
                0.1208 * sin(e2) -
                0.0642 * sin(e3) +
                0.0158 * sin(e4) +
                0.0252 * sin(e5) -
                0.0066 * sin(e6) -
                0.0047 * sin(e7) -
                0.0044 * sin(e13) -
                0.0046 * sin(e8) +
                0.0028 * sin(e9) +
                0.0052 * sin(e10) +
                0.0040 * sin(e11) +
                0.0019 * sin(e12)).deg
    }
}