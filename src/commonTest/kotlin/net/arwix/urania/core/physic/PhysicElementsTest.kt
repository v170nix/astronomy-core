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

        val sunVector = sunEphemeris.invoke(instant.toJT())
        val objectVector = ephemeris.invoke(instant.toJT())

        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
            Physic.Model.IAU2018,
            sunVector, objectVector, Physic.Body.Mercury, instant.toJT())

        assertEquals(26.5303, elements.elongation.toDeg().value, 1e-3)
        assertEquals(PhysicEphemeris.Relative.Leads, elements.relative)
        assertEquals(93.4746, elements.phaseAngle.toDeg().value, 1e-2)
        assertEquals(0.4696519, elements.phase, 1e-3)
        assertEquals(7.826734, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
        assertEquals(4.1509, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e-3)
        assertEquals(0.285, elements.magnitude, 1e-3)
        assertEquals(281.00276, elements.northPole.rightAscension.toDeg().value, 1e-2)
        assertEquals(61.41326, elements.northPole.declination.toDeg().value, 1e-2)
        assertEquals(133.434734, elements.longitudeOfCentralMeridian.toDeg().value, 2e-1)
        assertEquals(-7.518352, elements.positionAngleOfPole.toDeg().value, 2e-2)
//        assertEquals(343.7464513346991, elements.positionAngleOfAxis.toDeg().value)
        assertEquals(226.945782, elements.subsolarLongitude!!.toDeg().value, 1e-1)
        assertEquals(-0.007998, elements.subsolarLatitude!!.toDeg().value, 5e-3)
//        assertEquals(119.14675184010902, elements.brightLimbAngle!!.toDeg().value)
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

        assertEquals(6.4394, elements.elongation.toDeg().value, 1e-3)
        assertEquals(PhysicEphemeris.Relative.Leads, elements.relative)
        assertEquals(8.7792, elements.phaseAngle.toDeg().value, 1e-2)
        assertEquals(0.9941345, elements.phase, 1e-4)
        assertEquals(9.793184, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
        assertEquals(0.0574, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e-3)
        assertEquals(-3.898, elements.magnitude, 1e-3)
        assertEquals(272.76, elements.northPole.rightAscension.toDeg().value, 1e-2)
        assertEquals(67.16, elements.northPole.declination.toDeg().value, 1e-2)
        assertEquals(281.646572, elements.longitudeOfCentralMeridian.toDeg().value, 3e-1)
        assertEquals(0.660804, elements.positionAngleOfPole.toDeg().value, 2e-2)
//        assertEquals(343.7464513346991, elements.positionAngleOfAxis.toDeg().value)
        assertEquals(273.082899, elements.subsolarLongitude!!.toDeg().value, 4e-1)
        assertEquals(2.63655, elements.subsolarLatitude!!.toDeg().value, 5e-3)
//        assertEquals(119.14675184010902, elements.brightLimbAngle!!.toDeg().value)
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
            Physic.Model.IAU2018,
            sunVector, objectVector, Physic.Body.Mars, instant.toJT()
        )

        assertEquals(77.5493, elements.elongation.toDeg().value, 1e-3)
        assertEquals(PhysicEphemeris.Relative.Trails, elements.relative)
        assertEquals(37.6667, elements.phaseAngle.toDeg().value, 1e-2)
        assertEquals(0.8957954, elements.phase, 1e-4)
        assertEquals(6.384991, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
        assertEquals(0.6653, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e-3)
//        assertEquals(0.926, elements.magnitude, 1e-3)
        assertEquals(317.65898, elements.northPole.rightAscension.toDeg().value, 1e-2)
        assertEquals(52.87361, elements.northPole.declination.toDeg().value, 1e-2)
        assertEquals(329.253289, elements.longitudeOfCentralMeridian.toDeg().value, 3e-1)
        assertEquals(-12.381792, elements.positionAngleOfPole.toDeg().value, 2e-2)
//        assertEquals(343.7464513346991, elements.positionAngleOfAxis.toDeg().value)
        assertEquals(295.303676, elements.subsolarLongitude!!.toDeg().value, 3e0)
        assertEquals(4.541551, elements.subsolarLatitude!!.toDeg().value, 5e-3)
//        assertEquals(119.14675184010902, elements.brightLimbAngle!!.toDeg().value)
    }


    @Test
    fun jupiter() = runTest {
        val ephemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierJupiterEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )

        val sunVector = sunEphemeris.invoke(instant.toJT())
        val objectVector = ephemeris.invoke(instant.toJT())

        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
            Physic.Model.IAU2018,
            sunVector, objectVector, Physic.Body.Jupiter, instant.toJT()
        )

        assertEquals(24.0465, elements.elongation.toDeg().value, 1e-3)
        assertEquals(PhysicEphemeris.Relative.Leads, elements.relative)
        assertEquals(4.5590, elements.phaseAngle.toDeg().value, 1e-2)
        assertEquals(0.9984143, elements.phase, 1e-4)
        assertEquals(33.05129, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e-2)
        assertEquals(0.0524, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e-3)
        assertEquals(-1.978, elements.magnitude, 1e-3)
        assertEquals(268.05797, elements.northPole.rightAscension.toDeg().value, 1e-2)
        assertEquals(64.49702, elements.northPole.declination.toDeg().value, 1e-2)
        assertEquals(201.848132, elements.longitudeOfCentralMeridian.toDeg().value, 3e-1)
        assertEquals(-0.269565, elements.positionAngleOfPole.toDeg().value, 2e-2)
        assertEquals(340.0 + 24.0 / 60.0 + 20.0 / 60.0 / 60.0, elements.positionAngleOfAxis.toDeg().value, 2e-2)
        assertEquals(206.412062, elements.subsolarLongitude!!.toDeg().value, 4e-1)
        assertEquals(-0.338773, elements.subsolarLatitude!!.toDeg().value, 5e-3)
//        assertEquals(119.14675184010902, elements.brightLimbAngle!!.toDeg().value)
    }

    @Test
    fun moon() = runTest {
        val ephemeris : MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierMoonEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Ecliptic
        )

        val sunVector = sunEphemeris.invoke(instant.toJT())
        val objectVector = ephemeris.invoke(instant.toJT())

        val elements: PhysicEphemeris = PhysicEphemeris.createElements(
            Physic.Model.IAU2018,
            sunVector, objectVector, Physic.Body.Moon, instant.toJT()
        )

        assertEquals(156.7212, elements.elongation.toDeg().value, 1e-3)
        assertEquals(23.2232, elements.phaseAngle.toDeg().value, 1e-2)
        assertEquals(0.9595057, elements.phase, 1e-4)
        assertEquals(PhysicEphemeris.Relative.Leads, elements.relative)
        assertEquals(1956.864, elements.angularDiameter.toDeg().value * 60.0 * 60.0, 1e0)
        assertEquals(79.2419, elements.defectOfIllumination.toDeg().value * 60.0 * 60.0, 1e0)
        assertEquals(-12.236, elements.magnitude, 1e-3)
//        assertEquals(266.22699, elements.northPole.rightAscension.toDeg().value, 1e-2)
//        assertEquals(66.89277, elements.northPole.declination.toDeg().value, 1e-2)
        assertEquals(357.652298, elements.longitudeOfCentralMeridian.toDeg().value, 3e-1)
//        assertEquals(-6.228038, elements.positionAngleOfPole.toDeg().value, 2e-2)
//        assertEquals(340.0 + 24.0 / 60.0 + 20.0 / 60.0 / 60.0, elements.positionAngleOfAxis.toDeg().value, 2e-2)
//        assertEquals(334.854834, elements.subsolarLongitude!!.toDeg().value, 4e-1)
//        assertEquals(-1.531415, elements.subsolarLatitude!!.toDeg().value, 5e-3)
    }

}