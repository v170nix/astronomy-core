package net.arwix.urania.core.calendar

import net.arwix.urania.core.math.angle.toRA
import kotlin.test.Test
import kotlin.test.assertEquals

class SiderealTimeTest {
    @Test
    fun getGMST() {
        val mjd = MJD(2019, 1, 1, 8)
        assertEquals("14h 42m 45.38s", mjd.getGMSTLaskar1986().toRA().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMSTWilliams1994().toRA().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMSTIAU20xx(true).toRA().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMSTIAU20xx(false).toRA().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMST(SiderealTimeMethod.Laskar1986).toRA().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMST(SiderealTimeMethod.Williams1994).toRA().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMST(SiderealTimeMethod.IAU2000).toRA().toString())
        assertEquals("14h 42m 45.38s", mjd.getGMST(SiderealTimeMethod.IAU20xx).toRA().toString())
    }
}