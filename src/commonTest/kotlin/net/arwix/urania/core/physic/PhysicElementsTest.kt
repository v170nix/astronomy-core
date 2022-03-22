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

class PhysicElementsTest {

    private val instant by lazy {
        LocalDateTime(2021, 3, 1, 1, 0).toInstant(TimeZone.UTC)
    }

    private val sunEphemeris: MoshierEphemeris by lazy {
        MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierSunEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )
    }

    @Test
    fun sun() = runTest {

        val sunVector = sunEphemeris.invoke(instant.toJT())
        val objectVector = sunEphemeris.invoke(instant.toJT())

        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
            Physic.Model.IAU2000,
            sunVector, objectVector, Physic.Body.Sun, instant.toJT()
        )

        assertEquals(1937.185, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 2e-2)
        assertEquals(0.0, elements.elongation.toDeg().value, 1e-7)
        assertEquals(Double.NaN, elements.phaseAngle.toDeg().value)
        assertEquals(Double.NaN, elements.phase)
        assertEquals(PhysicEphemeris.Relative.Leads, elements.relative)
        assertEquals(1937.185, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e0)
        assertEquals(Double.NaN, elements.defectOfIllumination.toDeg().value)
        assertEquals(286.13, elements.northPole.rightAscension.toDeg().value, 1e-3)
        assertEquals(63.87, elements.northPole.declination.toDeg().value, 1e-3)
    }

    @Test
    fun mercury() = runTest {
        val ephemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierMercuryEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )
        // 132 -7.32
        // 226 -0.0025

        val sunVector = sunEphemeris.invoke(instant.toJT())
        val objectVector = ephemeris.invoke(instant.toJT())

        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
            Physic.Model.IAU2018,
            sunVector, objectVector, Physic.Body.Mercury, instant.toJT())

        assertEquals(7.826734, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
        assertEquals(26.5303, elements.elongation.toDeg().value, 1e-3)
        assertEquals(PhysicEphemeris.Relative.Leads, elements.relative)
        assertEquals(93.4746, elements.phaseAngle.toDeg().value, 1e-2)
        assertEquals(0.4696519, elements.phase, 1e-3)
        assertEquals(7.826734, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
        assertEquals(4.1509, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e-3)
        assertEquals(0.285, elements.magnitude, 1e-3)
        assertEquals(281.00222, elements.northPole.rightAscension.toDeg().value, 1e-2)
        assertEquals(61.41318, elements.northPole.declination.toDeg().value, 1e-2)
    }

    @Test
    fun venus() = runTest {
        val ephemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierVenusEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )

        val sunVector = sunEphemeris.invoke(instant.toJT())
        val objectVector = ephemeris.invoke(instant.toJT())

        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
            Physic.Model.IAU2018,
            sunVector, objectVector, Physic.Body.Venus, instant.toJT()
        )

        assertEquals(1.0493, elements.elongation.toDeg().value, 1e-3)
        assertEquals(1.4458, elements.phaseAngle.toDeg().value, 1e-2)
        assertEquals(0.9998408, elements.phase, 1e-4)
        assertEquals(PhysicEphemeris.Relative.Trails, elements.relative)
        assertEquals(9.719397, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
        assertEquals(0.0015, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e-3)
    }

    @Test
    fun mars() = runTest {
        val ephemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierMarsEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )

        val sunVector = sunEphemeris.invoke(instant.toJT())
        val objectVector = ephemeris.invoke(instant.toJT())

        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
            Physic.Model.IAU2000,
            sunVector, objectVector, Physic.Body.Mars, instant.toJT()
        )

        assertEquals(1.0493, elements.elongation.toDeg().value, 1e-3)
        assertEquals(1.4458, elements.phaseAngle.toDeg().value, 1e-2)
        assertEquals(0.9998408, elements.phase, 1e-4)
        assertEquals(PhysicEphemeris.Relative.Trails, elements.relative)
        assertEquals(9.719397, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
        assertEquals(0.0015, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e-3)
    }

//
//    @Test
//    fun jupiter() = runTest {
//        val ephemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
//            bodyEphemeris = MoshierJupiterEphemeris,
//            epoch = Epoch.Apparent,
//            plane = Plane.Ecliptic
//        )
//
//        val sunVector = sunEphemeris.invoke(instant.toJT())
//        val objectVector = ephemeris.invoke(instant.toJT())
//
//        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
//            sunVector, objectVector, Physic.Body.Jupiter, instant.toJT()
//        )
//
//        assertEquals(150.8888, elements.elongation.toDeg().value, 1e-3)
//        assertEquals(5.6083, elements.phaseAngle.toDeg().value, 1e-2)
//        assertEquals(0.9976066, elements.phase, 1e-4)
//        assertEquals(PhysicEphemeris.Relative.Trails, elements.relative)
//        assertEquals(48.55479, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
//        assertEquals(0.1162, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e-3)
//    }
//
//    @Test
//    fun moon() = runTest {
//        val ephemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
//            bodyEphemeris = MoshierMoonEphemeris,
//            epoch = Epoch.Apparent,
//            plane = Plane.Ecliptic
//        )
//
//        val sunVector = sunEphemeris.invoke(instant.toJT())
//        val objectVector = ephemeris.invoke(instant.toJT())
//
//        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
//            sunVector, objectVector, Physic.Body.Moon, instant.toJT()
//        )
//
//        assertEquals(30.5190, elements.elongation.toDeg().value, 1e-3)
//        assertEquals(149.4049, elements.phaseAngle.toDeg().value, 1e-2)
//        assertEquals(0.0695856, elements.phase, 1e-4)
//        assertEquals(PhysicEphemeris.Relative.Leads, elements.relative)
//        assertEquals(1850.087, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e0)
//        assertEquals(1721.347, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e0)
//    }

}