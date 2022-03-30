package net.arwix.urania.core.ephemeris.calculation

import kotlinx.datetime.Instant
import net.arwix.urania.core.annotation.ExperimentalUrania
import net.arwix.urania.core.calendar.MJD
import net.arwix.urania.core.calendar.mJD
import net.arwix.urania.core.calendar.toJT
import net.arwix.urania.core.calendar.toMJD
import net.arwix.urania.core.ephemeris.Ephemeris
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.SECONDS_PER_DAY
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.rectangular
import net.arwix.urania.core.spherical
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.createElements
import kotlin.math.*

@ExperimentalUrania
class SolarEclipseCalculation(
    val jd0: Double,
    val moonEphemeris: Ephemeris,
    val sunEphemeris: Ephemeris
) {

    suspend fun init(time: Instant) {
        var mjd = time.toMJD()
        val mjd0 = mjd
        val step = 0.5 / SECONDS_PER_DAY
        var out = arrayOf(0.0.mJD, 0.0.mJD, 0.0.mJD, 0.0.mJD)
        var statusPrev = -1.0
        var outPrev: Array<MJD>? = null
        var mjdPrev = -1.0.mJD
        var detailedMode = false

        do {
            mjd += step.mJD

            val (eclipsed, totality) = checkEclipse(mjd)
            if (eclipsed && out[0] == 0.0.mJD) out[0] = mjd
            if (totality && out[1] == 0.0.mJD) out[1] = mjd

            if (!totality && out[2] == 0.0.mJD && out[1] != 0.0.mJD) out[2] = mjd
            if (!eclipsed && out[3] == 0.0.mJD && out[0] != 0.0.mJD) out[3] = mjd

            var status = 0.0
            out.forEachIndexed { index, data ->
                if (data.value != 0.0) status += 10.0.pow(index)
            }

            if (statusPrev >= 0 && status != statusPrev && !detailedMode) {
                out = outPrev!!
                mjd = mjdPrev
                detailedMode = true
                continue
            }
            if (detailedMode && status == statusPrev) continue

            detailedMode = false
            mjdPrev = mjd
            statusPrev = status
            outPrev = out.copyOf()
            mjd += (10.0 / SECONDS_PER_DAY).mJD
            if (mjd - mjd0 > 3.mJD) throw Exception("No eclipse found in the next 3 days.")
        } while (out[3] == 0.0.mJD)

        println(out)
        var jdMax = (out[0] + out[3]) * 0.5
        if (out[1] != 0.0.mJD) jdMax = (out[1] + out[2]) * 0.5


    }

    suspend fun checkEclipse(time: MJD): Pair<Boolean, Boolean> {
        val ephem_moon = moonEphemeris(time.toJT())
        val ephem_sun = sunEphemeris(time.toJT())
        val obliquityElements = Obliquity.Williams1994.createElements(time.toJT())
        val sun_loc = obliquityElements.rotatePlane(ephem_sun, Plane.Equatorial).spherical
        val moon_loc = obliquityElements.rotatePlane(ephem_moon, Plane.Equatorial).spherical
        val dist = getAngularDistance(moon_loc, sun_loc)
        val moonPhysicEphemeris = Physic.createElements(time.toJT(), Physic.Body.Moon, Physic.Model.IAU2018, ephem_sun, ephem_moon)
        val sunPhysic = Physic.createElements(time.toJT(), Physic.Body.Sun, Physic.Model.IAU2018, ephem_sun, ephem_sun)
        val pa = Radian.PI * 3.0 / 2.0 - getPositionAngle(sun_loc, moon_loc) - moonPhysicEphemeris.positionAngleOfPole
        val m_x = sin(pa) / moonPhysicEphemeris.angularDiameter / 2.0
        val m_y = cos(pa) / moonPhysicEphemeris.angularDiameter / 2.0
        val m_r = (1.0 / hypot(m_x, m_y)).rad
        val s_r = sunPhysic.angularDiameter / 2.0

        val eclipsed = dist <= (s_r + m_r)
        var totality: Boolean = false

        if (((dist + m_r) <= s_r && (m_r <= s_r)) || ((dist + s_r) <= m_r && (m_r >= s_r)))
        {
            totality = true
            if (m_r > s_r)	{
                println("total")
            } else {
                println("annular")
            }
        } else {
            if (eclipsed) println("partial")
        }

        return eclipsed to totality

    }

    private companion object {

        /**
         * Obtain angular distance between two spherical coordinates.
         *
         * @param loc1 Location object.
         * @param loc2 Location object.
         * @return The distance in radians, from 0 to PI.
         */
        fun getAngularDistance(vector1: SphericalVector, vector2: SphericalVector): Radian {
            val v1 = SphericalVector(vector1.phi, vector1.theta).rectangular
            val v2 = SphericalVector(vector2.phi, vector2.theta).rectangular

            val dx = v1[0] - v2[0]
            val dy = v1[1] - v2[1]
            val dz = v1[2] - v2[2]

            val r2 = dx * dx + dy * dy + dz * dz

            return acos(1.0 - r2 * 0.5).rad

        }


        /**
         * Obtain exact position angle between two spherical coordinates. Performance will be poor.
         *
         * @param loc1 Location object.
         * @param loc2 Location object.
         * @return The position angle in radians.
         */
        fun getPositionAngle(loc1: SphericalVector, loc2: SphericalVector): Radian {
            val al = loc1.phi
            val ap = loc1.theta
            val bl = loc2.phi
            val bp = loc2.theta
            val dl = bl - al
            val cbp = cos(bp)
            val y = sin(dl) * cbp
            val x = sin(bp) * cos(ap) - cbp * sin(ap) * cos(dl)
            var pa = 0.0
            if (x != 0.0 || y != 0.0) pa = -atan2(y, x)
            return pa.rad
        }
    }

}