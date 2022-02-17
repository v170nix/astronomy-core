package net.arwix.urania.core.physic

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.arwix.urania.core.calendar.toJT
import net.arwix.urania.core.ephemeris.Epoch
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.toDeg
import net.arwix.urania.moshier.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PhysicAnglesTest {

    @Test
    fun invoke() = runTest {

        val instant = LocalDateTime(2022, 10, 23, 0, 0).toInstant(TimeZone.UTC)

        val sunEphemeris: MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierSunEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )

        val venusEphemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierVenusEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )

        val sunVector = sunEphemeris.invoke(instant.toJT())
        val venusVector = venusEphemeris.invoke(instant.toJT())

        val venusAngles = PhysicAngles.getAngles(sunVector, venusVector)

        assertEquals(1.0493, venusAngles.elongation.toDeg().value, 1e-3)
        assertEquals(1.4458, venusAngles.phaseAngle.toDeg().value, 1e-3)
        assertEquals(0.9998408, venusAngles.phase, 1e-6)
        assertEquals(PhysicAngles.Relative.Trails, venusAngles.relative)

        val sunAngles = PhysicAngles.getAngles(sunVector, sunVector)

        assertEquals(0.0, sunAngles.elongation.toDeg().value, 1e-3)
        assertEquals(Double.NaN, sunAngles.phaseAngle.toDeg().value, 1e-3)
        assertEquals(Double.NaN, sunAngles.phase, 1e-6)
        assertEquals(PhysicAngles.Relative.Leads, sunAngles.relative)

        val jupiterEphemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierJupiterEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )

        val jupiterVector = jupiterEphemeris.invoke(instant.toJT())

        val jupiterAngles = PhysicAngles.getAngles(sunVector, jupiterVector)

        assertEquals(150.8888, jupiterAngles.elongation.toDeg().value, 1e-3)
        assertEquals(5.6083, jupiterAngles.phaseAngle.toDeg().value, 1e-3)
        assertEquals(0.9976066, jupiterAngles.phase, 1e-6)
        assertEquals(PhysicAngles.Relative.Trails, jupiterAngles.relative)

    }

}