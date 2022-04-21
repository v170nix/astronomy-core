package net.arwix.urania.core.calculation.moon

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import net.arwix.urania.core.calendar.*
import net.arwix.urania.core.ephemeris.*
import net.arwix.urania.core.ephemeris.calculation.SolarEclipseCalculationIntersect
import net.arwix.urania.core.ephemeris.calculation.moon.LunarEclipseBruteForceCalculation
import net.arwix.urania.core.ephemeris.calculation.moon.LunarPhaseAndEclipseCalculation
import net.arwix.urania.core.ephemeris.fast.FastMoonEphemeris
import net.arwix.urania.core.ephemeris.fast.FastSunEphemeris
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.toDeg
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.createElements
import net.arwix.urania.moshier.MoshierEphemeris
import net.arwix.urania.moshier.MoshierEphemerisFactory
import net.arwix.urania.moshier.MoshierMoonEphemeris
import net.arwix.urania.moshier.MoshierSunEphemeris
import kotlin.test.Test

class LunarPhaseEclipseCalculationTest {


    @Test
    fun invoke() = runTest {
        val instant = Clock.System.now().plus(10L, DateTimeUnit.DAY, TimeZone.UTC)
        val endInstant = LocalDate(2022, Month.DECEMBER, 31).atStartOfDayIn(TimeZone.UTC)
        val events = LunarPhaseAndEclipseCalculation.invoke(
            instant,
            endInstant,
        )

        events.forEach { event ->
            println(event)
        }
    }

    @Test
    fun test2() = runTest {
        // https://eclipse.gsfc.nasa.gov/SEplot/SEplot2001/SE2017Aug21T.GIF
        // https://eclipse.gsfc.nasa.gov/SEpath/SEpath2001/SE2017Aug21Tpath.html
        // https://eclipse.gsfc.nasa.gov/solar.html
        // https://eclipse.gsfc.nasa.gov/SEsaros/SEsaros0-180.html


        val instant = LocalDateTime(2017, Month.AUGUST, 21, 18 , 25, 32).toInstant(TimeZone.UTC)
//        val instant = LocalDateTime(2019, Month.JULY, 2, 19, 22, 59).toInstant(TimeZone.UTC)
        val mjd = instant.toMJD(true)
        val dd = instant.getDeltaTTUT1()
        println(dd)
        val obliquity = Obliquity.Williams1994.createElements(mjd.toJT())
        val sunEphemeris: MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierSunEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Equatorial
        )

        val aSunEphemeris: Ephemeris = object : Ephemeris {
            override val metadata: Metadata
                get() = Metadata(
                    orbit = Orbit.Geocentric,
                    plane = Plane.Equatorial,
                    epoch = Epoch.Apparent
                )

            override suspend fun invoke(jT: JT): Vector {
                return obliquity.rotatePlane(FastSunEphemeris(jT), Plane.Equatorial)
            }

        }

        val aMoonEphemeris: Ephemeris = object : Ephemeris {
            override val metadata: Metadata
                get() = Metadata(
                    orbit = Orbit.Geocentric,
                    plane = Plane.Equatorial,
                    epoch = Epoch.Apparent
                )

            override suspend fun invoke(jT: JT): Vector {
                return obliquity.rotatePlane(FastMoonEphemeris(jT), Plane.Equatorial)
            }

        }

        val moonEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierMoonEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Equatorial
        )

        val cals = SolarEclipseCalculationIntersect(aMoonEphemeris, aSunEphemeris)

        (0..(300 / 3)).forEach {
            //                cals.init()

            cals.intersect(mjd + (it * 3 / 60.0 / 24.0).mJD )
        }


    }

    @Test
    fun test3() = runTest {

        val instant = LocalDateTime(2022, Month.MAY, 16, 0, 0, 0).toInstant(TimeZone.UTC)

        val sunEphemeris: MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierSunEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Equatorial
        )

        val moonEphemeris: MoshierEphemeris = MoshierEphemerisFactory(instant.toJT()).createGeocentricEphemeris(
            bodyEphemeris = MoshierMoonEphemeris,
            epoch = Epoch.Apparent,
            plane = Plane.Equatorial
        )

        val (result, pa) = LunarEclipseBruteForceCalculation(instant.toMJD(), sunEphemeris, moonEphemeris)

        result.forEachIndexed { index, d ->
            val date = runCatching { d.mJD.toInstant() }.getOrNull()
            println("$index $date ${pa[index].rad.toDeg()}")
        }

    }
}