package net.arwix.urania.core.ephemeris.calculation.moon

import net.arwix.urania.core.annotation.Apparent
import net.arwix.urania.core.annotation.Equatorial
import net.arwix.urania.core.annotation.Geocentric
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.MJD
import net.arwix.urania.core.calendar.mJD
import net.arwix.urania.core.calendar.toJT
import net.arwix.urania.core.ephemeris.Ephemeris
import net.arwix.urania.core.math.AU
import net.arwix.urania.core.math.SECONDS_PER_DAY
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.math.vector.getAngularDistance
import net.arwix.urania.core.math.vector.getPositionAngle
import net.arwix.urania.core.physic.Physic
import net.arwix.urania.core.spherical
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.pow

object LunarEclipseBruteForceCalculation {


    data class LunarEclipse internal constructor(
        val maximum: MJD,
        val p1: MJD,
        val u1: MJD,
        val u2: MJD,
        val u3: MJD,
        val u4: MJD,
        val p4: MJD,
    ) {



    }

    private suspend fun check(
        jt: JT,
        @Geocentric @Apparent @Equatorial sunEphemeris: Ephemeris,
        @Geocentric @Apparent @Equatorial moonEphemeris: Ephemeris,
    ): Pair<Radian, BooleanArray> {
        val insideShadow: Boolean
        val totality: Boolean
        val insidePenumbra: Boolean
        val totalityPenumbra: Boolean

        val moon = moonEphemeris(jt).spherical
        val sun = sunEphemeris(jt).spherical

        // Get shadow cone direction

        val sunRadius = Physic.Body.Sun.ellipsoid.getAngularRadius(sun.r * AU)
        val moonRadius = Physic.Body.Moon.ellipsoid.getAngularRadius(moon.r * AU)
        val shadow = SphericalVector(sun.phi + Radian.PI, -sun.theta, moon.r)
        val motherBody = Physic.Body.Earth.ellipsoid

        val earthShadowConeSize = motherBody.equatorialRadius / (AU * tan(sunRadius))

        val shadowNormalizedVector = SphericalVector(shadow.phi, shadow.theta, 1.0)

        val angularRadiusMax: Double = atan2(motherBody.equatorialRadius / AU, moon.r) *
                (1.0131 - moon.r / earthShadowConeSize)

        val angularRadiusMin: Double = atan2(motherBody.getPolarRadius() / AU, moon.r) *
                (1.015 - moon.r / earthShadowConeSize)

        val penumbraAngularRadius: Double = 2.0 * sunRadius
        val penumbraScaleMax = angularRadiusMax + penumbraAngularRadius
        val penumbraScaleMin = angularRadiusMin + penumbraAngularRadius

        val moonNormalizedVector = SphericalVector(moon.phi, moon.theta, 1.0)

        val angularDistance = moonNormalizedVector.getAngularDistance(shadowNormalizedVector)

        val positionAngle = Radian.PI * 3.0 / 2.0 - moonNormalizedVector.getPositionAngle(shadowNormalizedVector)

        var sx = sin(positionAngle) / angularRadiusMax
        var sy = cos(positionAngle) / angularRadiusMin
        var sr = (1.0 / hypot(sx, sy)).rad

        insideShadow = angularDistance <= (sr + moonRadius)
        totality = angularDistance <= (sr - moonRadius)

        sx = sin(positionAngle) / penumbraScaleMax
        sy = cos(positionAngle) / penumbraScaleMin
        sr = (1.0 / hypot(sx, sy)).rad

        insidePenumbra = angularDistance <= (sr + moonRadius)
        totalityPenumbra = angularDistance <= (sr - moonRadius)

        return positionAngle to booleanArrayOf(insidePenumbra, totalityPenumbra, insideShadow, totality)
    }

    suspend operator fun invoke(
        initMJD: MJD,
        @Geocentric @Apparent @Equatorial sunEphemeris: Ephemeris,
        @Geocentric @Apparent @Equatorial moonEphemeris: Ephemeris
    ): Pair<DoubleArray, DoubleArray> {

        var out: DoubleArray = doubleArrayOf(
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN
        )

        var pa: DoubleArray = doubleArrayOf(
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN
        )

        var jd = initMJD
        var jdPrev = Double.NaN
        var statusPrev = Double.NaN
        var outPrev: DoubleArray? = null
        var paPrev: DoubleArray? = null
        var detailedMode = false
        do {
            jd += step.mJD
            val (positionAngle, event: BooleanArray) = check(jd.toJT(), sunEphemeris, moonEphemeris)
            if (event[PenumbraIngressStart] && out[PenumbraIngressStart].isNaN()) {
                out[PenumbraIngressStart] = jd.value
                pa[PenumbraIngressStart] = positionAngle.value
            }
            if (event[PenumbraTotalIngress] && out[PenumbraTotalIngress].isNaN()) {
                out[PenumbraTotalIngress] = jd.value
                pa[PenumbraTotalIngress] = positionAngle.value
            }
            if (event[ShadowIngress] && out[ShadowIngress].isNaN()) {
                out[ShadowIngress] = jd.value
                pa[ShadowIngress] = positionAngle.value
            }
            if (event[ShadowTotalIngress] && out[ShadowTotalIngress].isNaN()) {
                out[ShadowTotalIngress] = jd.value
                pa[ShadowTotalIngress] = positionAngle.value
            }
            if (!event[ShadowTotalIngress] && out[ShadowTotalEgress].isNaN() && !out[ShadowTotalIngress].isNaN()) {
                out[ShadowTotalEgress] = jd.value
                pa[ShadowTotalEgress] = positionAngle.value
            }
            if (!event[ShadowIngress] && out[ShadowEgress].isNaN() && !out[ShadowIngress].isNaN()) {
                out[ShadowEgress] = jd.value
                pa[ShadowEgress] = positionAngle.value
            }
            if (!event[PenumbraTotalIngress] && out[PenumbraTotalEgress].isNaN() && !out[PenumbraTotalIngress].isNaN()) {
                out[PenumbraTotalEgress] = jd.value
                pa[PenumbraTotalEgress] = positionAngle.value
            }
            if (!event[PenumbraIngressStart] && out[PenumbraEgress].isNaN() && !out[PenumbraIngressStart].isNaN()) {
                out[PenumbraEgress] = jd.value
                pa[PenumbraEgress] = positionAngle.value
            }

            var status = 0.0

            for (i in out.indices) {
                if (!out[i].isNaN()) status += 10.0.pow(i.toDouble())
            }
            if (statusPrev >= 0 && status != statusPrev && !detailedMode) {
                out = outPrev!!
                pa = paPrev!!
                jd = jdPrev.mJD
                detailedMode = true
                continue
            }
            if (detailedMode && status == statusPrev) continue
            detailedMode = false
            jdPrev = jd.value
            statusPrev = status
            outPrev = out.copyOf()
            paPrev = pa.copyOf()
            jd += (60 / SECONDS_PER_DAY).mJD
            if (jd - initMJD > 3.mJD) throw IllegalArgumentException("No eclipse found in the next 3 days.")
        } while (out[PenumbraEgress].isNaN())


        if (!out[ShadowTotalIngress].isNaN()) {
            LunarEclipse(
                ((out[ShadowTotalIngress] + out[ShadowTotalEgress]) * 0.5).mJD,
                p1 = out[PenumbraIngressStart].mJD,
                u1 = out[ShadowIngress].mJD,
                u2 = out[ShadowTotalIngress].mJD,
                u3 = out[ShadowTotalEgress].mJD,
                u4 = out[ShadowEgress].mJD,
                p4 = out[PenumbraEgress].mJD,
            )
        }

        return out to pa

    }
}

private const val PenumbraIngressStart = 0
private const val PenumbraTotalIngress = 1
private const val ShadowIngress = 2
private const val ShadowTotalIngress = 3
private const val ShadowTotalEgress = 4
private const val ShadowEgress = 5
private const val PenumbraTotalEgress = 6
private const val PenumbraEgress = 7
private const val step = 1.0 / SECONDS_PER_DAY