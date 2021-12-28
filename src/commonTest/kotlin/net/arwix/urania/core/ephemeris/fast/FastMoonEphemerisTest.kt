@file:OptIn(ExperimentalCoroutinesApi::class)

package net.arwix.urania.core.ephemeris.fast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.test.runTest
import net.arwix.urania.core.assertContentEquals
import net.arwix.urania.core.calendar.jT
import net.arwix.urania.core.ephemeris.Epoch
import net.arwix.urania.core.ephemeris.Orbit
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.spherical
import kotlin.test.Test
import kotlin.test.assertEquals

class FastMoonEphemerisTest {

    @Test
    fun metadata() {
        assertEquals(FastMoonEphemeris.metadata.epoch, Epoch.Apparent)
        assertEquals(FastMoonEphemeris.metadata.orbit, Orbit.Geocentric)
        assertEquals(FastMoonEphemeris.metadata.plane, Plane.Ecliptic)
    }

    @Test
    fun invoke() = runTest {
        assertContentEquals(
            doubleArrayOf(3.897580699834961, 0.0902602318375678, 0.002690177410307512),
            FastMoonEphemeris(0.jT).spherical.toArray(false),
            1e-14
        )

        assertContentEquals(
            doubleArrayOf(2.085051381047725, 0.05849167492439595, 0.0025889864641208027),
            FastMoonEphemeris(0.21.jT).spherical.toArray(false),
            1e-14
        )

        assertContentEquals(
            doubleArrayOf(5.488887078446313, 0.05133887486340649, 0.002409531558438556),
            FastMoonEphemeris((-0.21).jT).spherical.toArray(false),
            1e-14
        )
    }


}