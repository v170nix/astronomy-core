package net.arwix.urania.core.ephemeris.calculation.moon

import net.arwix.urania.core.calendar.*
import net.arwix.urania.core.ephemeris.Ephemeris
import net.arwix.urania.core.math.AU
import net.arwix.urania.core.math.SECONDS_PER_DAY
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.rectangular
import net.arwix.urania.core.spherical
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.pow

class LunarEclipseCalculation(
    val moonEphemeris: Ephemeris,
    val sunEphemeris: Ephemeris,
) {

    val step  = 1.0 / SECONDS_PER_DAY

    private suspend fun check(jt: JT): BooleanArray {

        var inside_shadow = false
        var totality = false
        var inside_penumbra = false
        var totality_penumbra = false

        val ephem_moon = moonEphemeris.invoke(jt).spherical
        val ephem_sun = sunEphemeris.invoke(jt).spherical

        // Get shadow cone direction

        val sun_size = Physic.Body.Sun.ellipsoid.getAngularRadius(ephem_sun.r * AU)
        val ephem = SphericalVector(ephem_sun.phi + Radian.PI, -ephem_sun.theta, ephem_moon.r)

        val val_eq = 1.0131
        val val_pol = 1.015

        val motherBody = Physic.Body.Earth.ellipsoid
        val targetBody = Physic.Body.Moon.ellipsoid

        val ephem_moon_angularRadius = targetBody.getAngularRadius(ephem_moon.r * AU)

        val EarthShadowConeSize = motherBody.equatorialRadius / (AU * tan(sun_size))

        val shadow_loc = SphericalVector(ephem.phi, ephem.theta, 1.0)

        val ang_radius_max: Double = atan2(motherBody.equatorialRadius / AU, ephem_moon.r) *
                (val_eq - ephem_moon.r / EarthShadowConeSize)

        val ang_radius_min: Double = atan2(motherBody.getPolarRadius() / AU, ephem_moon.r) *
                (val_pol - ephem_moon.r / EarthShadowConeSize)
        val penumbra_ang_radius: Double = 2.0 * sun_size
        val penumbra_scale_max = ang_radius_max + penumbra_ang_radius
        val penumbra_scale_min = ang_radius_min + penumbra_ang_radius

        val moon_loc = SphericalVector(ephem_moon.phi, ephem_moon.theta, 1.0)

        val dist = getAngularDistance(moon_loc, shadow_loc)

        val pa: Radian = Radian.PI * 3.0 / 2.0 - getPositionAngle(moon_loc, shadow_loc)

        val s_x = sin(pa) / ang_radius_max
        val s_y = cos(pa) / ang_radius_min
        val s_r = (1.0 / hypot(s_x, s_y)).rad

        if (dist <= (s_r + ephem_moon_angularRadius)) inside_shadow = true
        if (dist <= (s_r - ephem_moon_angularRadius)) totality = true

        val s_x1 = sin(pa) / penumbra_scale_max
        val s_y1 = cos(pa) / penumbra_scale_min
        val s_r1 = (1.0 / hypot(s_x1, s_y1)).rad

        if (dist <= (s_r1 + ephem_moon_angularRadius)) inside_penumbra = true
        if (dist <= (s_r1 - ephem_moon_angularRadius)) totality_penumbra = true

        return booleanArrayOf(inside_penumbra, totality_penumbra ,inside_shadow, totality)
    }

    suspend fun init(initMJD: MJD): DoubleArray {

        var out: DoubleArray? = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        var jd = initMJD
        var jdPrev = -1.0
        var statusPrev = -1.0
        var outPrev: DoubleArray? = null
        var detailedMode = false
        do {
            jd += step.mJD
            val new_time = jd
            val event: BooleanArray = check(new_time.toJT())
            if (event[0] && out!![0] == 0.0) out[0] = jd.value
            if (event[1] && out!![1] == 0.0) out[1] = jd.value
            if (event[2] && out!![2] == 0.0) out[2] = jd.value
            if (event[3] && out!![3] == 0.0) out[3] = jd.value
            if (!event[3] && out!![4] == 0.0 && out[3] != 0.0) out[4] = jd.value
            if (!event[2] && out!![5] == 0.0 && out[2] != 0.0) out[5] = jd.value
            if (!event[1] && out!![6] == 0.0 && out[1] != 0.0) out[6] = jd.value
            if (!event[0] && out!![7] == 0.0 && out[0] != 0.0) out[7] = jd.value
            var status = 0.0
            for (i in out!!.indices) {
                if (out[i] != 0.0) status += 10.0.pow(i.toDouble())
            }
            if (statusPrev >= 0 && status != statusPrev && !detailedMode) {
                out = outPrev
                jd = jdPrev.mJD
                detailedMode = true
                continue
            }
            if (detailedMode && status == statusPrev) continue
            detailedMode = false
            jdPrev = jd.value
            statusPrev = status
            outPrev = out.copyOf()
            jd += (60 / SECONDS_PER_DAY).mJD
            if (jd - initMJD > 3.mJD) throw IllegalArgumentException("No eclipse found in the next 3 days.")
        } while (out!![7] == 0.0)

        return out

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
        fun getPositionAngle(vector1: SphericalVector, vector2: SphericalVector): Radian {
            val al = vector1.theta
            val ap = vector1.phi
            val bl = vector2.theta
            val bp = vector2.phi
            val dl = bl - al
            val cbp: Double = cos(bp)
            val y: Double = sin(dl) * cbp
            val x: Double = sin(bp) * cos(ap) - cbp * sin(ap) * cos(dl)
            var pa = Radian.Zero
            if (x != 0.0 || y != 0.0) pa = -atan2(y, x).rad
            return pa
        }
    }
}