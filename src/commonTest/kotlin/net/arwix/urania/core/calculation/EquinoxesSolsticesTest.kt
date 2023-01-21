package net.arwix.urania.core.calculation

import kotlinx.coroutines.test.runTest
import net.arwix.urania.core.ephemeris.Epoch
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.ephemeris.calculation.EquinoxesSolsticesCalculation
import net.arwix.urania.moshier.MoshierEphemerisFactory
import net.arwix.urania.moshier.MoshierSunEphemeris
import kotlin.test.Test
import kotlin.test.assertEquals

class EquinoxesSolsticesTest {

    @Test
    fun findEquinoxSolsticeTest() = runTest {

        val result = EquinoxesSolsticesCalculation.findEvents(
            2022,
            { jT0 ->
                MoshierEphemerisFactory(jT0)
                    .createGeocentricEphemeris(
                        bodyEphemeris = MoshierSunEphemeris,
                        epoch = Epoch.Apparent,
                        plane = Plane.Ecliptic
                    )
            }
        )

        assertEquals("2022-03-20T15:33:25", result[0].date.toString().substring(0..18))
        assertEquals("2022-06-21T09:13:51", result[1].date.toString().substring(0..18))
        assertEquals("2022-09-23T01:03:42", result[2].date.toString().substring(0..18))
        assertEquals("2022-12-21T21:48:13", result[3].date.toString().substring(0..18))
    }
}