@file:Suppress("TestFunctionName")

package net.arwix.urania.core.transformation

import net.arwix.urania.core.assertContentEquals
import net.arwix.urania.core.calendar.jT
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.transformation.precession.Precession
import net.arwix.urania.core.transformation.precession.createElements
import kotlin.test.Test
import kotlin.test.assertEquals


class PrecessionElementsTest {

    @Test
    fun DE4xxxTest() {
        var precession = Precession.DE4xxx.createElements(4.5.jT)

        assertEquals(precession.jT, 4.5.jT)
        assertEquals(precession.id, Precession.DE4xxx)
        assertEquals(precession.id.plane, Plane.Ecliptic)

        assertContentEquals(
            doubleArrayOf(0.9939758714280893, -0.10959911959800385, 1.4536138229003546E-6),
            precession.fromJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10959906383941706, 0.9939753521879893, -0.0010219825273615779),
            precession.fromJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(1.1056352893180911E-4, 0.0010159852879326793, 0.999999477774664),
            precession.fromJ2000Matrix[2].toArray(false), 1e-14)

        precession = Precession.DE4xxx.createElements((-4.5).jT)

        assertEquals(precession.jT, (-4.5).jT)

        assertContentEquals(
            doubleArrayOf(0.9939996192421173, -0.1093835074083365, -7.248155591952142E-5),
            precession.toJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10938337620211797, 0.9939991068022618, -0.0010260053273318276),
            precession.toJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(1.8427466316683617E-4, 0.001011920627409361, 0.9999994710296063),
            precession.toJ2000Matrix[2].toArray(false), 1e-14)
    }

    @Test
    fun Williams1994Test() {
        var precession = Precession.Williams1994.createElements(4.5.jT)

        assertEquals(precession.jT, 4.5.jT)
        assertEquals(precession.id, Precession.Williams1994)
        assertEquals(precession.id.plane, Plane.Ecliptic)

        assertContentEquals(
            doubleArrayOf(0.9939759240090339, -0.10959864273007952, 1.4531235183140977E-6),
            precession.fromJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10959858697124363, 0.9939754047689607, -0.0010219825280588435),
            precession.fromJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(1.1056352893180911E-4, 0.0010159852879326793, 0.999999477774664),
            precession.fromJ2000Matrix[2].toArray(false), 1e-14)

        precession = Precession.Williams1994.createElements((-4.5).jT)

        assertEquals(precession.jT, (-4.5).jT)

        assertContentEquals(
            doubleArrayOf(0.9939996715693059, -0.1093830318949695, -7.248155591952142E-5),
            precession.toJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10938290068899613, 0.9939991591293875, -0.0010260053273318276),
            precession.toJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(1.8427417908033992E-4, 0.001011920715563266, 0.9999994710296063),
            precession.toJ2000Matrix[2].toArray(false), 1e-14)
    }

    @Test
    fun IAU1976Test() {
        var precession = Precession.IAU1976.createElements(4.5.jT)

        assertEquals(precession.jT, 4.5.jT)
        assertEquals(precession.id, Precession.IAU1976)
        assertEquals(precession.id.plane, Plane.Ecliptic)

        assertContentEquals(
            doubleArrayOf(0.9939750803037024, -0.10960629422219606, 1.457994880148167E-6),
            precession.fromJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10960623846481332, 0.993974561068123, -0.0010219779972164766),
            precession.fromJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(1.1056601123048495E-4, 0.0010159804671863782, 0.9999994777792873),
            precession.fromJ2000Matrix[2].toArray(false), 1e-14)


        precession = Precession.IAU1976.createElements((-4.5).jT)

        assertEquals(precession.jT, (-4.5).jT)

        assertContentEquals(
            doubleArrayOf(0.9939989626791464, -0.10938947362023542, -7.24835739575672E-5),
            precession.toJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10938934240986604, 0.9939984502457602, -0.0010260000799978512),
            precession.toJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(1.8428216886738012E-4, 0.0010119140847358503, 0.9999994710348438),
            precession.toJ2000Matrix[2].toArray(false), 1e-14)
    }

    @Test
    fun IAU2000Test() {
        var precession = Precession.IAU2000.createElements(4.5.jT)

        assertEquals(precession.jT, 4.5.jT)
        assertEquals(precession.id, Precession.IAU2000)
        assertEquals(precession.id.plane, Plane.Equatorial)

        assertContentEquals(
            doubleArrayOf(0.9939757969501904, -0.10055647788717825, -0.04359483721909311),
            precession.fromJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10055642745468002, 0.9949289253206782, -0.002199649470218612),
            precession.fromJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.043594953547233806, -0.0021973427510487142, 0.9990468716281778),
            precession.fromJ2000Matrix[2].toArray(false), 1e-14)


        precession = Precession.IAU2000.createElements((-4.5).jT)

        assertEquals(precession.jT, (-4.5).jT)

        assertContentEquals(
            doubleArrayOf(0.9939996777871914, 0.10028346663332051, 0.04367913551071372),
            precession.fromJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(-0.10028351706723035, 0.9949564792557171, -0.0021955845904954585),
            precession.fromJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(-0.04367901971869468, -0.0021978869559635683, 0.9990431985301449),
            precession.fromJ2000Matrix[2].toArray(false), 1e-14)

        assertContentEquals(
            doubleArrayOf(0.9939750804369492, -0.10056243965279528, -0.04359742195862297),
            precession.toJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10056236521001514, 0.9949283233853327, -0.002200461092187911),
            precession.toJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.04359759366900712, -0.0021970563781123498, 0.9990467570487097),
            precession.toJ2000Matrix[2].toArray(false), 1e-14)
    }

    @Test
    fun IAU2006Test() {
        var precession = Precession.IAU2006.createElements(4.5.jT)

        assertEquals(precession.jT, 4.5.jT)
        assertEquals(precession.id, Precession.IAU2006)
        assertEquals(precession.id.plane, Plane.Equatorial)

        assertContentEquals(
            doubleArrayOf(0.9939758609877645, -0.10055590552791842, -0.04359469735062571),
            precession.fromJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10055585580057137, 0.9949289831761305, -0.0021996136764357854),
            precession.fromJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.043594812051974796, -0.0021973392025833594, 0.9990468778103366),
            precession.fromJ2000Matrix[2].toArray(false), 1e-14)


        precession = Precession.IAU2006.createElements((-4.5).jT)

        assertEquals(precession.jT, (-4.5).jT)

        assertContentEquals(
            doubleArrayOf(0.9939758609877645, -0.10055590552791842, -0.04359469735062571),
            precession.toJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10055585580057137, 0.9949289831761305, -0.0021996136764357854),
            precession.toJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.043594812051974796, -0.0021973392025833594, 0.9990468778103366),
            precession.toJ2000Matrix[2].toArray(false), 1e-14)
    }

    @Test
    fun Vondrak2011Test() {
        var precession = Precession.Vondrak2011.createElements(4.5.jT)

        assertEquals(precession.jT, 4.5.jT)
        assertEquals(precession.id, Precession.Vondrak2011)
        assertEquals(precession.id.plane, Plane.Equatorial)

        assertContentEquals(
            doubleArrayOf(0.9939758644481955, -0.10055587429557582, -0.04359469049224532),
            precession.fromJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10055582473686407, 0.9949289863265145, -0.0021996087795378427),
            precession.fromJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.043594804804602434, -0.0021973420185071313, 0.9990468781203925),
            precession.fromJ2000Matrix[2].toArray(false), 1e-14)


        precession = Precession.Vondrak2011.createElements((-4.5).jT)

        assertEquals(precession.jT, (-4.5).jT)

        assertContentEquals(
            doubleArrayOf(0.9939996060873487, 0.10028410875315605, 0.043679292917593066),
            precession.fromJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(-0.10028415868955495, 0.9949564145113955, -0.002195618035144231),
            precession.fromJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(-0.04367917826749818, -0.0021978976803437855, 0.9990431915746499),
            precession.fromJ2000Matrix[2].toArray(false), 1e-14)

        assertContentEquals(
            doubleArrayOf(0.9939758644481955, -0.10055587429557582, -0.04359469049224532),
            precession.toJ2000Matrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.10055582473686407, 0.9949289863265145, -0.0021996087795378427),
            precession.toJ2000Matrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(0.043594804804602434, -0.0021973420185071313, 0.9990468781203925),
            precession.toJ2000Matrix[2].toArray(false), 1e-14)
    }

//    @Test
//    fun NPM() {
//        val precession0 = Precession.DE4xxx.createElements(-2.jT)
//        val precession1 = Precession.IAU2006.createElements(-2.jT)
//        val obliquity0 = Obliquity.Williams1994.createElements(-2.jT)
//        val obliquity1 = Obliquity.IAU2006.createElements(-2.jT)
//
//        val m0 = obliquity0.eclipticToEquatorialMatrix * precession0.fromJ2000Matrix
//        val m1 = precession1.fromJ2000Matrix * obliquity1.eclipticToEquatorialMatrix
//
//        println(m0[1].toArray(false).toList())
//        println(m1[1].toArray(false).toList())
//    }


}