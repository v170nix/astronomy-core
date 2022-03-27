@file:Suppress("unused")

package net.arwix.urania.core.ephemeris

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.jT
import net.arwix.urania.core.math.JULIAN_DAYS_PER_CENTURY
import net.arwix.urania.core.math.vector.Vector

interface Ephemeris {
    val metadata: Metadata
    suspend operator fun invoke(jT: JT): Vector
    suspend fun getEphemerisVector(jT: JT): EphemerisVector {
        return EphemerisVector(invoke(jT), metadata)
    }

    suspend fun getVelocity(vectorAtJTTime: Vector, jT: JT, deltaTimeInDay: Double = 0.01): Vector {
        val bodyPlus = invoke(jT + (deltaTimeInDay / JULIAN_DAYS_PER_CENTURY).jT)
        return (bodyPlus - vectorAtJTTime) / deltaTimeInDay
    }
}