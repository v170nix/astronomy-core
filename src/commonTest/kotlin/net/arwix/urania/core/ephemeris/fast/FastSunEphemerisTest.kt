@file:OptIn(ExperimentalCoroutinesApi::class)

package net.arwix.urania.core.ephemeris.fast

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.arwix.urania.core.assertContentEquals
import net.arwix.urania.core.calendar.jT
import net.arwix.urania.core.ephemeris.Epoch
import net.arwix.urania.core.ephemeris.Orbit
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.spherical
import kotlin.test.Test
import kotlin.test.assertEquals

class FastSunEphemerisTest {

    @Test
    fun metadata() {
        assertEquals(FastSunEphemeris.metadata.epoch, Epoch.Apparent)
        assertEquals(FastSunEphemeris.metadata.orbit, Orbit.Geocentric)
        assertEquals(FastSunEphemeris.metadata.plane, Plane.Ecliptic)
    }

    @Test
    fun invoke() = runTest {
        assertContentEquals(
            doubleArrayOf(11.17653591468365, 0.0, 0.9833084337672905),
            FastSunEphemeris(0.jT).spherical.toArray(false),
            1e-14
        )

        assertContentEquals(
            doubleArrayOf(11.17925484009307, 0.0, 0.9833199547171038),
            FastSunEphemeris(0.21.jT).spherical.toArray(false),
            1e-14
        )

        assertContentEquals(
            doubleArrayOf(11.173943661007298, 0.0, 0.9832971305001807),
            FastSunEphemeris((-0.21).jT).spherical.toArray(false),
            1e-14
        )
    }


}