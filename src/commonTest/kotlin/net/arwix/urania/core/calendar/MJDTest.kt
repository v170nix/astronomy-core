package net.arwix.urania.core.calendar

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class MJDTest {

    @Test
    fun invoke() {

        assertEquals(
            MJD(2018, 10, 30, 0, 0, 0),
            MJD(58421.0)
        )

        assertEquals(
            MJD(718, 2, 5, 7, 21, 12).value,
            -416661.6936111111,
            1e-14
        )

        assertEquals(
            MJD(718, 2, 5, 7, 21, 12, isJulianDate = true).value,
            -416657.6936111111,
            1e-14
        )


        val dateTime = LocalDateTime(2000, 1, 1, 12, 0, 0)
        val instant = dateTime.toInstant(TimeZone.UTC)
        assertEquals(MJD.J2000, instant.toMJD())

        val mjd = MJD(2000, 1, 1, 12)
        assertEquals(MJD.J2000, mjd)
        assertEquals(instant.toMJD(), mjd)
    }

    @Test
    fun convert() {
        val dateTime = LocalDateTime(2022, 1, 1, 0, 0)
        val instant = dateTime.toInstant(TimeZone.UTC)

        assertEquals(instant.epochSeconds, instant.toMJD().toInstant().epochSeconds)

        val jT = instant.toMJD().toJT()
        assertEquals(0.22, jT.value)
    }

}