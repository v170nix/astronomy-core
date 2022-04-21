package net.arwix.urania.core.ephemeris.calculation

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import net.arwix.urania.core.annotation.Apparent
import net.arwix.urania.core.annotation.Equatorial
import net.arwix.urania.core.annotation.Geocentric
import net.arwix.urania.core.calendar.MJD
import net.arwix.urania.core.calendar.isNaN
import net.arwix.urania.core.calendar.mJD
import net.arwix.urania.core.ephemeris.Ephemeris
import net.arwix.urania.core.ephemeris.calculation.moon.LunarEclipseBruteForceCalculation
import net.arwix.urania.core.ephemeris.calculation.moon.LunarPhaseAndEclipseCalculation

sealed class LunarEclipse(
    val timeOfMaximum: MJD,
    val magnitude: Double,
    val contacts: Contacts,
    val durationInSec: Double,
) {
    class Penumbral(timeOfMaximum: MJD, magnitude: Double, contacts: Contacts, durationInSec: Double) :
        LunarEclipse(timeOfMaximum, magnitude, contacts, durationInSec)

    class Partial(timeOfMaximum: MJD, magnitude: Double, contacts: Contacts, durationInSec: Double) :
        LunarEclipse(timeOfMaximum, magnitude, contacts, durationInSec)

    class Total(timeOfMaximum: MJD, magnitude: Double, contacts: Contacts, durationInSec: Double) :
        LunarEclipse(timeOfMaximum, magnitude, contacts, durationInSec)

    data class Contacts(
        val penumbralBegins: MJD,
        val partialBegins: MJD,
        val totalBegins: MJD,
        val totalEnds: MJD,
        val partialEnds: MJD,
        val penumbralEnds: MJD
    ): Iterable<MJD> {
        internal constructor(array: DoubleArray) : this(
            penumbralBegins = array[0].mJD,
            partialBegins = array[2].mJD,
            totalBegins = array[3].mJD,
            totalEnds = array[4].mJD,
            partialEnds = array[5].mJD,
            penumbralEnds = array[7].mJD
        )

        override fun iterator(): Iterator<MJD> {
            return ContactsIterator(this)
        }
    }

    companion object {
        suspend fun find(
            @Geocentric @Apparent @Equatorial sunEphemeris: Ephemeris,
            @Geocentric @Apparent @Equatorial moonEphemeris: Ephemeris,
            beginInstant: Instant,
            endInstant: Instant,
            dispatcher: CoroutineDispatcher = Dispatchers.Default
        ) {
            val result = LunarPhaseAndEclipseCalculation(beginInstant, endInstant, true, dispatcher)
                .asSequence()
                .map { event -> event.simpleEclipse }
                .filterIsInstance<LunarPhaseAndEclipseCalculation.SimpleLunarEclipse>()
                .toList()
            find(sunEphemeris, moonEphemeris, result)
        }

        suspend fun find(
            @Geocentric @Apparent @Equatorial sunEphemeris: Ephemeris,
            @Geocentric @Apparent @Equatorial moonEphemeris: Ephemeris,
            simpleEclipses: List<LunarPhaseAndEclipseCalculation.SimpleLunarEclipse>,
        ) {
            simpleEclipses.map { eclipse ->
                val (array, _) = LunarEclipseBruteForceCalculation(
                    eclipse.timeOfMaximum - (8.0 / 24.0).mJD, sunEphemeris, moonEphemeris
                )
                val contacts = Contacts(array)

                when (eclipse) {
                    is LunarPhaseAndEclipseCalculation.SimpleLunarEclipse.Partial -> Partial(
                        timeOfMaximum = getMaximum(contacts),
                        magnitude = eclipse.magnitude,
                        contacts = contacts,
                        durationInSec = array[7] - array[0]
                    )
                    is LunarPhaseAndEclipseCalculation.SimpleLunarEclipse.Penumbral -> Penumbral(
                        timeOfMaximum = getMaximum(contacts),
                        magnitude = eclipse.magnitude,
                        contacts = contacts,
                        durationInSec = array[7] - array[0]
                    )
                    is LunarPhaseAndEclipseCalculation.SimpleLunarEclipse.Total -> Total(
                        timeOfMaximum = getMaximum(contacts),
                        magnitude = eclipse.magnitude,
                        contacts = contacts,
                        durationInSec = array[7] - array[0]
                    )
                }
            }
        }

        private fun getMaximum(contacts: Contacts): MJD {
            return when {
                !contacts.totalBegins.isNaN() -> contacts.totalBegins + contacts.totalEnds
                !contacts.partialBegins.isNaN() -> contacts.partialBegins + contacts.partialEnds
                else -> contacts.penumbralBegins + contacts.penumbralEnds
            } * 0.5
        }

    }
}

private class ContactsIterator(private val contacts: LunarEclipse.Contacts): Iterator<MJD> {
    private var start = -1

    override fun hasNext(): Boolean {
        return start <= 5
    }

    override fun next(): MJD {
        start++
        return when (start) {
            0 -> contacts.penumbralBegins
            1 -> contacts.partialBegins
            2 -> contacts.totalBegins
            3 -> contacts.totalEnds
            4 -> contacts.partialEnds
            5 -> contacts.penumbralEnds
            else -> throw IllegalArgumentException()
        }
    }

}