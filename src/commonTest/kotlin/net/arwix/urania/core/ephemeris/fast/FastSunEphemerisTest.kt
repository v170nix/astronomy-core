@file:OptIn(ExperimentalCoroutinesApi::class)

package net.arwix.urania.core.ephemeris.fast

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.arwix.urania.core.assertContentEquals
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.jT
import net.arwix.urania.core.ephemeris.*
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.math.vector.times
import net.arwix.urania.core.spherical
import net.arwix.urania.core.toDeg
import net.arwix.urania.core.toRad
import net.arwix.urania.core.transformation.TransformationElements
import net.arwix.urania.core.transformation.nutation.Nutation
import net.arwix.urania.core.transformation.nutation.createElements
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.createElements
import net.arwix.urania.core.transformation.precession.Precession
import net.arwix.urania.core.transformation.precession.createElements
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

    @Test
    fun invoke1() = runTest {
        val jt = JT(0.22)
        val result = FastSunEphemeris(jt).spherical
        val obliquity = Obliquity.Simon1994.createElements(jt)
        val equSun = obliquity.rotatePlane(result, Plane.Equatorial).spherical
        val raSun = equSun.phi.toDeg() / 15.0
        val raDeg = raSun.value.toInt()
        val raMin = ((raSun.value - raDeg.toDouble()) * 60.0).toInt()
        val raSec = ((raSun.value - raDeg.toDouble() - raMin / 60.0)) * 3600.0
        val decSun = equSun.theta.toDeg()
        val rSun = equSun.r
        println( "ra $raDeg $raMin $raSec" )
        println( "dec $decSun" )
        println( "rSun $rSun")
        val horizonSun = SphericalVector(
            phi = ((18.0 + 45.0 / 60.0 + 48.42 / 3600.0) * 15.0).deg.toRad(),
            theta = (-23.0 - 01.0 / 60.0 - 12.4 / 3600.0).deg.toRad(),
            r = 0.98335557932966
        )
        val horizonEphVector = EphemerisVector(horizonSun, Metadata(
            orbit = Orbit.Geocentric,
            plane = Plane.Equatorial,
            epoch = Epoch.Apparent
        )
        )

        val obliquity1 = Obliquity.IAU2006.createElements(jt)
        val precession = Precession.Vondrak2011.createElements(jt)
        val nutation = Nutation.IAU2006.createElements(jt, Obliquity.Vondrak2011)

        val tElements = TransformationElements(Precession.IAU2006, jt)

        val astroSun = (horizonSun *
                (tElements.nutationElements.equatorialMatrix!! * tElements.precessionElements.fromJ2000Matrix)
                ).spherical

        val raastroSun = astroSun.phi.toDeg() / 15.0
        val raDegastroSun = raastroSun.value.toInt()
        val raMinastroSun = ((raastroSun.value - raDegastroSun.toDouble()) * 60.0).toInt()
        val raSecastroSun = ((raastroSun.value - raDegastroSun.toDouble() - raMinastroSun / 60.0)) * 3600.0
        val decSunastroSun = astroSun.theta.toDeg()
        val rSunastroSun = astroSun.r
        println( "a ra $raDegastroSun $raMinastroSun $raSecastroSun" )
        println( "a dec $decSunastroSun" )
        println( "a rSun $rSunastroSun")


        assertContentEquals(
            doubleArrayOf(11.17653591468365, 0.0, 0.9833084337672905),
            FastSunEphemeris(jt).spherical.toArray(false),
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