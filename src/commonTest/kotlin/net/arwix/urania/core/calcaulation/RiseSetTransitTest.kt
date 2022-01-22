@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.arwix.urania.core.calcaulation

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.toJT
import net.arwix.urania.core.calendar.toMJD
import net.arwix.urania.core.ephemeris.*
import net.arwix.urania.core.ephemeris.calculation.RiseSetTransitCalculation
import net.arwix.urania.core.ephemeris.fast.FastSunEphemeris
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.observer.Observer
import net.arwix.urania.core.toRad
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.createElements
import kotlin.test.Test
import kotlin.test.assertEquals

class RiseSetTransitTest {

    @Test
    fun obtainCertainRiseSetTransit() {

        val instant = LocalDate(2022, 1, 14).atStartOfDayIn(TimeZone.UTC)
        val sunEphemeris: Ephemeris = FastSunEphemeris
        val obliquity = Obliquity.Simon1994.createElements(instant.toJT())

        val aSunEphemeris: Ephemeris = object : Ephemeris {
            override val metadata: Metadata
                get() = Metadata(
                    orbit = Orbit.Geocentric,
                    plane = Plane.Topocentric,
                    epoch = Epoch.Apparent
                )

            override suspend fun invoke(jT: JT): Vector {
                return obliquity.rotatePlane(sunEphemeris(jT), Plane.Equatorial)
            }

        }

        runTest {
            val observer = Observer(
                position = Observer.Position(
                    longitude = (30.0 + 19.0 / 60.0 + 36.0 / 60.0 / 60.0).deg.toRad(),
                    latitude = (60.0 + 3.0 / 60.0 + 32.6 / 60.0 / 60.0).deg.toRad(),
                    altitude = 0.0),
            )
            val riseSetTransitCalculation = RiseSetTransitCalculation()

            val obtainResult: Set<RiseSetTransitCalculation.Result> = riseSetTransitCalculation.obtainNextResults(
                instant.toMJD(), observer,
                ephemeris = aSunEphemeris,
                request = setOf(
                    RiseSetTransitCalculation.Request.RiseSet.HorizonAstronomical((15 / 60.0).deg.toRad() , Radian.Zero),
                    RiseSetTransitCalculation.Request.UpperTransit(),
                    RiseSetTransitCalculation.Request.DownTransit()
                ),
            )

            assertEquals(4, obtainResult.size)



            obtainResult.forEach {
                when (it) {
                    is RiseSetTransitCalculation.Result.Rise.Value -> {
                        assertEquals(
                            "2022-01-14T06:47:42",
                            it.time.toLocalDateTime(TimeZone.UTC).toString().substring(0..18)
                        )
                    }
                    is RiseSetTransitCalculation.Result.Set.Value -> {
                        assertEquals(
                            "2022-01-14T13:25:51",
                            it.time.toLocalDateTime(TimeZone.UTC).toString().substring(0..18)
                        )
                    }
                    is RiseSetTransitCalculation.Result.UpperTransit -> {
                        assertEquals(
                            "2022-01-14T10:06:31",
                            it.time.toLocalDateTime(TimeZone.UTC).toString().substring(0..18)
                        )
                    }
                    is RiseSetTransitCalculation.Result.DownTransit -> {
                        assertEquals(
                            "2022-01-13T22:06:20",
                            it.time.toLocalDateTime(TimeZone.UTC).toString().substring(0..18)
                        )
                    }
                    else -> throw IllegalStateException()
                }
            }

        }

    }

}