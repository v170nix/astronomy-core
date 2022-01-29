package net.arwix.urania.core.math.angle

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclinationTest {

    @Test
    fun invoke() {
        var degree = (23.0 + 02.0 / 60.0 + 20.2 / 3600.0).deg
        var dec = degree.toDeclination()
        assertEquals(false, dec.isNegative)
        assertEquals(23, dec.degree)
        assertEquals(2, dec.minute)
        assertEquals(20.2, dec.second, 1e-8)
        assertEquals("23deg 2m 20.2s", dec.toString())

        degree = (93.0 + 02.0 / 60.0 + 20.2 / 3600.0).deg
        dec = degree.toDeclination()
        assertEquals(false, dec.isNegative)
        assertEquals(86, dec.degree)
        assertEquals(57, dec.minute)
        assertEquals(39.8, dec.second, 1e-8)
        assertEquals("86deg 57m 39.8s", dec.toString())

        degree = (-23.0 - 02.0 / 60.0 - 20.2 / 3600.0).deg
        dec = degree.toDeclination()
        assertEquals(true, dec.isNegative)
        assertEquals(23, dec.degree)
        assertEquals(2, dec.minute)
        assertEquals(20.2, dec.second, 1e-8)
        assertEquals("-23deg 2m 20.2s", dec.toString())

        degree = (-93.0 - 02.0 / 60.0 - 20.2 / 3600.0).deg
        dec = degree.toDeclination()
        assertEquals(true, dec.isNegative)
        assertEquals(86, dec.degree)
        assertEquals(57, dec.minute)
        assertEquals(39.8, dec.second, 1e-8)
        assertEquals("-86deg 57m 39.8s", dec.toString())
    }

}