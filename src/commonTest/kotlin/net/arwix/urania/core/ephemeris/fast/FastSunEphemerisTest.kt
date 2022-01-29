@file:OptIn(ExperimentalCoroutinesApi::class)

package net.arwix.urania.core.ephemeris.fast

//import net.arwix.urania.core.math.angle.toDeclination
//import net.arwix.urania.core.math.angle.toRightAscension
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

//    @Test
//    fun invoke1() = runTest {
//
//        val sunEphemeris = object : Ephemeris {
//            override val metadata: Metadata
//                get() = Metadata(
//                    orbit = Orbit.Heliocentric,
//                    plane = Plane.Ecliptic,
//                    epoch = Epoch.J2000
//                )
//
//            override suspend fun invoke(jT: JT): Vector {
//                return -getEphemerisVsop87A(Vsop87AEarthRectangularData, jT).spherical
//            }
//        }
//        val time = LocalDate(2022, 1, 11).atStartOfDayIn(TimeZone.UTC)
//        val result = sunEphemeris(time.toJT()).spherical
//        val obliquity = Obliquity.IAU2006.createElements(JT.J2000)
//        val equResult = obliquity.rotatePlane(result, Plane.Equatorial).spherical
//        val ra = equResult.phi.toRightAscension()
//        val dec = equResult.theta.toDeclination()
//        println("ICRF ra $ra")
//        println("ICRF dec $dec")
//
//
//        val currentTime = LocalDate(2022, 1, 11).atStartOfDayIn(TimeZone.UTC)
//        val result0 = (sunEphemeris(currentTime.toJT())).spherical
//        val timeInDay = result0.r * LIGHT_TIME_DAYS_PER_AU
//
//
//        println("r ${timeInDay}")
//        val currentResult =
//            LocalDate(2022, 1, 11).atStartOfDayIn(TimeZone.UTC)
//                .let { time.toJT() - (timeInDay/ 36525.0).jT }
//                .let { sunEphemeris(it) }
//                .let { EphemerisVector(it, Metadata(Orbit.Geocentric, Plane.Ecliptic, Epoch.J2000)) }
//
////                .let { Precession.Simon1994.createElements(time.toJT()).changeEpoch(it) }
////                .let { Nutation.IAU1980.createElements(time.toJT(), null).apply(it) }
////                .let { Obliquity.IAU2006.createElements(time.toJT()).rotatePlane(it) }
//
//            .let { Obliquity.IAU2006.createElements(JT.J2000).rotatePlane(it) }
//            .let { Precession.IAU2006.createElements(time.toJT()).changeEpoch(it) }
//            .let { Nutation.IAU2006.createElements(time.toJT(), Obliquity.IAU2006).apply(it) }
//                .value.spherical
//
//        val rac = currentResult.phi.toRightAscension()
//        val decC = currentResult.theta.toDeclination()
//        println("apparent ra $rac")
//        println("apparent dec $decC")
//
////        assertContentEquals(
////            doubleArrayOf(11.17653591468365, 0.0, 0.9833084337672905),
////            FastSunEphemeris(jt).spherical.toArray(false),
////            1e-14
////        )
////
////        assertContentEquals(
////            doubleArrayOf(11.17925484009307, 0.0, 0.9833199547171038),
////            FastSunEphemeris(0.21.jT).spherical.toArray(false),
////            1e-14
////        )
////
////        assertContentEquals(
////            doubleArrayOf(11.173943661007298, 0.0, 0.9832971305001807),
////            FastSunEphemeris((-0.21).jT).spherical.toArray(false),
////            1e-14
////        )
//    }

//    @Test
//    fun invoke2() = runTest {
//
//        val marsEphemeris = object : Ephemeris {
//            override val metadata: Metadata
//                get() = Metadata(
//                    orbit = Orbit.Heliocentric,
//                    plane = Plane.Ecliptic,
//                    epoch = Epoch.J2000
//                )
//
//            override suspend fun invoke(jT: JT): Vector {
//                return getEphemerisVsop87A(Vsop87AMarsRectangularData, jT)
//            }
//
//        }
//
//        val earthEphemeris = object : Ephemeris {
//            override val metadata: Metadata
//                get() = Metadata(
//                    orbit = Orbit.Heliocentric,
//                    plane = Plane.Ecliptic,
//                    epoch = Epoch.J2000
//                )
//
//            override suspend fun invoke(jT: JT): Vector {
//                return getEphemerisVsop87A(Vsop87AEarthRectangularData, jT).spherical
//            }
//        }
//
//        val sunEphemeris = object : Ephemeris {
//            override val metadata: Metadata
//                get() = Metadata(
//                    orbit = Orbit.Heliocentric,
//                    plane = Plane.Ecliptic,
//                    epoch = Epoch.J2000
//                )
//
//            override suspend fun invoke(jT: JT): Vector {
//                return RectangularVector.Zero
//            }
//        }
//
//        val time = LocalDate(2022, 1, 11).atStartOfDayIn(TimeZone.UTC)
//        apparentPosition(
//            time.toJT(),
//            earthEphemeris,
//            marsEphemeris
//        )
//
//        j2000Position(
//            time.toJT(),
//            earthEphemeris,
//            marsEphemeris
//        )
//
////        assertContentEquals(
////            doubleArrayOf(11.17653591468365, 0.0, 0.9833084337672905),
////            FastSunEphemeris(jt).spherical.toArray(false),
////            1e-14
////        )
////
////        assertContentEquals(
////            doubleArrayOf(11.17925484009307, 0.0, 0.9833199547171038),
////            FastSunEphemeris(0.21.jT).spherical.toArray(false),
////            1e-14
////        )
////
////        assertContentEquals(
////            doubleArrayOf(11.173943661007298, 0.0, 0.9832971305001807),
////            FastSunEphemeris((-0.21).jT).spherical.toArray(false),
////            1e-14
////        )
//    }


}

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

//private class VsopEphemeris(private val bodyData: VsopRectangularData,
//                            private val earthEphemeris: Ephemeris,
//                            private val jT0: JT
//                            ) : Ephemeris {
//
//    private val obliquity = Obliquity.IAU2006.createElements(JT.J2000)
//
//    override val metadata: Metadata
//        get() = TODO("Not yet implemented")
//
//    override suspend fun invoke(jT: JT): Vector {
//        return getEphemerisVsop87A(bodyData, jT)
//    }
//
//    suspend fun getE(jT: JT): Vector = coroutineScope {
//
//        val body = async { invoke(jT) }
//        val earth = async { earthEphemeris(jT) }
//        val geoBody = body.await() - earth.await()
//
//        val oneWayDown = geoBody.spherical.r * LIGHT_TIME_DAYS_PER_AU
//
//        val result = geoBody
//            .let {
//                val earthVelocity = earthEphemeris.getVelocity(
//                    earth.await(),
//                    jT
//                )
//                val bodyVelocity = getVelocity(
//                    body.await(),
//                    jT,
//                )
//                it - (bodyVelocity - earthVelocity) * oneWayDown
//            }
//            .let { obliquity.rotatePlane(it, Plane.Equatorial) }
//            .let { Precession.IAU2006.createElements(jT).changeEpoch(it, Epoch.Apparent) }
//            .let { Nutation.IAU2006.createElements(jT, Obliquity.IAU2006).apply(it, Plane.Equatorial) }
//            .spherical
//
//        val rac = result.phi.toRightAscension()
//        val decC = result.theta.toDeclination()
//        println("apparent ra $rac")
//        println("apparent dec $decC")
//        result
//    }
//
//}

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
