package net.arwix.urania.core.calendar

//import net.arwix.urania.core.math.angle.toRightAscension
import net.arwix.urania.core.math.angle.toRightAscension
import net.arwix.urania.core.toDeg
import net.arwix.urania.core.transformation.nutation.Nutation
import net.arwix.urania.core.transformation.nutation.getNutationAngles
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.getEps
import kotlin.test.Test
import kotlin.test.assertEquals

class SiderealTimeTest {

    @Test
    fun getGMSTTest() {
        val mjd = MJD(2019, 1, 1, 8)
        assertEquals("14h 42m 45.38s", mjd.getGMSTLaskar1986().toRightAscension().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMSTWilliams1994().toRightAscension().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMSTIAU20xx(true).toRightAscension().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMSTIAU20xx(false).toRightAscension().toString())
        assertEquals("14h 42m 45.38s", mjd.getGreenwichMeanSiderealTime(SiderealTimeMethod.Laskar1986).toRightAscension().toString())
        assertEquals("14h 42m 45.38s", mjd.getGreenwichMeanSiderealTime(SiderealTimeMethod.Williams1994).toRightAscension().toString())
        assertEquals("14h 42m 45.38s", mjd.getGreenwichMeanSiderealTime(SiderealTimeMethod.IAU2000).toRightAscension().toString())
        assertEquals("14h 42m 45.38s", mjd.getGreenwichMeanSiderealTime(SiderealTimeMethod.IAU20xx).toRightAscension().toString())
    }

    @Test
    fun getEquationOfEquinoxesTest() {
        val mjd = MJD(2021, 1, 1)
        val jt = mjd.toJT()
        var result = getEquationOfEquinoxes(jt).toDeg() / 15.0 * 60.0 * 60.0
        assertEquals(-0.988, result.value, 1e-3)

        result =  getEquationOfEquinoxes(
            meanObliquity = Obliquity.Williams1994.getEps(jt),
            nutationAngles = Nutation.IAU1980.getNutationAngles(jt)
        ).toDeg() / 15.0 * 60.0 * 60.0
        assertEquals(-0.988, result.value, 1e-3)
    }
}