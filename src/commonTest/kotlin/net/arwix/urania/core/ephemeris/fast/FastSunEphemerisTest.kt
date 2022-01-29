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


//
//private suspend fun j2000Position(
//    jT :JT,
//    earthEphemeris: Ephemeris,
//    bodyEphemeris: Ephemeris
//) = coroutineScope {
//
//    val body = async { bodyEphemeris(jT) }
//    val earth = async { earthEphemeris(jT) }
//    val geoBody = body.await() - earth.await()
//
//    val oneWayDown = geoBody.spherical.r * LIGHT_TIME_DAYS_PER_AU
//
//    val result = jT
//        .let { bodyEphemeris(jT - (oneWayDown / JULIAN_DAYS_PER_CENTURY).jT ) - earthEphemeris(jT) }
//        .let { Obliquity.IAU2006.createElements(JT.J2000).rotatePlane(it, Plane.Equatorial) }
//        .spherical
//
//    val rac = result.phi.toRightAscension()
//    val decC = result.theta.toDeclination()
//    println("j2000 ra $rac")
//    println("j2000 dec $decC")
//}

//private suspend fun apparentPosition(
//    jT :JT,
//    earthEphemeris: Ephemeris,
//    bodyEphemeris: Ephemeris
//) = coroutineScope {
//
//    val body = async { bodyEphemeris(jT) }
//    val earth = async { earthEphemeris(jT) }
//    val geoBody = body.await() - earth.await()
//
//    val oneWayDown = geoBody.spherical.r * LIGHT_TIME_DAYS_PER_AU
//
//    val result = geoBody
//        .let {
//            val earthVelocity = earthEphemeris.getVelocity(
//                earth.await(),
//                jT
//            )
//            val bodyVelocity = bodyEphemeris.getVelocity(
//                body.await(),
//                jT,
//            )
//            it - (bodyVelocity - earthVelocity) * oneWayDown
//        }
//        .let { Precession.Williams1994.createElements(jT).changeEpoch(it, Epoch.Apparent) }
//        .let { Precession.Williams1994.createElements(jT).changeEpoch(it, Epoch.J2000) }
//        .let { Precession.Williams1994.createElements(jT).changeEpoch(it, Epoch.Apparent) }
//        .let { Nutation.IAU1980.createElements(jT, Obliquity.IAU2006).apply(it, Plane.Ecliptic) }
//        .let { Obliquity.IAU2006.createElements(jT).rotatePlane(it, Plane.Equatorial) }
//
//
////        .let { Obliquity.IAU2006.createElements(JT.J2000).rotatePlane(it, Plane.Equatorial) }
////        .let { Precession.IAU2006.createElements(jT).changeEpoch(it, Epoch.Apparent) }
////        .let { Precession.IAU2006.createElements(jT).changeEpoch(it, Epoch.J2000) }
////        .let { Precession.IAU2006.createElements(jT).changeEpoch(it, Epoch.Apparent) }
////        .let { Nutation.IAU2006.createElements(jT, Obliquity.IAU2006).apply(it, Plane.Equatorial) }
//        .spherical
//
//    val rac = result.phi.toRightAscension()
//    val decC = result.theta.toDeclination()
//    println("apparent ra $rac")
//    println("apparent dec $decC")
//}
//
//@Heliocentric
//@Ecliptic
//private suspend fun getBodyVelocity(
//    currentCoordinates: Vector, jT: JT, lightTime: Double = 0.01,
//    @Heliocentric
//    @Ecliptic
//    @J2000
//    findCoordinates: suspend (jT: JT) -> Vector
//): Vector {
//    //  val body = findCoordinates(jT)
//    val bodyPlus = findCoordinates(jT + (lightTime / JULIAN_DAYS_PER_CENTURY).jT)
//    return (bodyPlus - currentCoordinates) / lightTime
//}
