package net.arwix.urania.core.ephemeris

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.vector.Vector

interface Ephemeris {
    val metadata: Metadata
    suspend operator fun invoke(jT: JT): Vector
}