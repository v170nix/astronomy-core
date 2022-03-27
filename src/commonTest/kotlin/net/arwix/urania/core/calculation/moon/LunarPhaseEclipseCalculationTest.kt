package net.arwix.urania.core.calculation.moon

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import net.arwix.urania.core.ephemeris.calculation.moon.LunarPhaseAndEclipseCalculation
import kotlin.test.Test

class LunarPhaseEclipseCalculationTest {


    @Test
    fun invoke() = runTest {
        val instant = Clock.System.now().plus(10L, DateTimeUnit.DAY, TimeZone.UTC)
        val endInstant = LocalDate(2022, Month.DECEMBER, 31).atStartOfDayIn(TimeZone.UTC)
        val events = LunarPhaseAndEclipseCalculation.invoke(
            instant,
            endInstant,
        )

        events.forEach { event ->
            println(event)
        }
    }

}