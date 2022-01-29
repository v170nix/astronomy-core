package net.arwix.urania.core.transformation

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import net.arwix.urania.core.calendar.toJT
import net.arwix.urania.core.ephemeris.Ephemeris
import net.arwix.urania.core.ephemeris.fast.FastSunEphemeris
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.toDeclination
import net.arwix.urania.core.math.angle.toRightAscension
import net.arwix.urania.core.observer.Observer
import net.arwix.urania.core.spherical
import net.arwix.urania.core.toDeg
import net.arwix.urania.core.toRad
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.createElements
import kotlin.test.Test
import kotlin.test.assertEquals

class TopocentricTransformationTest {

    @Test
    fun toTopocentric() = runTest {
        val instant = LocalDate(2022, 1, 25).atStartOfDayIn(TimeZone.UTC)
        val sunEphemeris: Ephemeris = FastSunEphemeris
        val obliquity = Obliquity.Simon1994.createElements(instant.toJT())

        val observer = Observer(
            position = Observer.Position(
                longitude = (30.0 + 19.0 / 60.0 + 59.9 / 60.0 / 60.0).deg.toRad(),
                latitude = (59.0 + 57.0 / 60.0 + 0.0 / 60.0 / 60.0).deg.toRad(),
                altitude = 0.0),
        )

        val vector = sunEphemeris.getEphemerisVector(instant.toJT()).let {
            obliquity.rotatePlane(it)
        }

        val result = vector.rotateToTopocentric(instant, observer.position).value.spherical

        assertEquals("2h 30m 10", result.phi.toDeg().toRightAscension().toString().substring(0..8))
        assertEquals("-44deg 39m 11", result.theta.toDeg().toDeclination().toString().substring(0..12))

    }


}