package net.arwix.urania.core.kepler

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.angle.Radian

interface KeplerElements {
    fun getSemiMajorAxis(jT: JT): Double //a
    fun getEccentricity(jT: JT): Radian // e
    fun getInclination(jT: JT): Radian //i

    fun getLongitude(jT: JT): Radian //L
    fun getPerihelionLongitude(jT: JT): Radian //w
    fun getAscendingNodeLongitude(jT: JT): Radian // O
    fun getMeanAnomaly(jT: JT): Radian = getLongitude(jT) - getPerihelionLongitude(jT)
}

enum class KeplerElementsObject {
    Mercury, Venus, Earth, EarthMoonBarycenter, Mars, Jupiter, Saturn, Uranus, Neptune, Pluto
}