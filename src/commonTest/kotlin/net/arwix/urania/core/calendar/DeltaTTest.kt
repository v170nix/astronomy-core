package net.arwix.urania.core.calendar

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test

class DeltaTTest {

    @Test
    fun getDeltaTTest() {
        val secs = LocalDate(2022, 2, 10).atStartOfDayIn(TimeZone.UTC).getDeltaTTUT1()
        val secs1 = getDeltaT(MJD(2022, 2, 10))
        println(secs)
        println(secs1)
    }

    @Test
    fun getDeltaT1Test() {
        val secs = LocalDate(2022, 2, 10).atStartOfDayIn(TimeZone.UTC).getDeltaTTUT1()
        val secs1 = getDeltaT(MJD(1922, 2, 10))
        println(secs)
        println(secs1)
    }

}