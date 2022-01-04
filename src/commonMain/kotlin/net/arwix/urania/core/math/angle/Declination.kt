@file:Suppress("unused")

package net.arwix.urania.core.math.angle

import net.arwix.urania.core.toDeg
import kotlin.math.roundToInt

class Declination constructor(val value: Degree) {

    val isNegative: Boolean
    val degree: Int
    val minute: Int
    val second: Double

    init {
        val innerValue = value.normalize()
            .also { isNegative = it >= 180.deg }
            .let { if (isNegative) 360.deg - it else it }
            .let { if (it > 90.deg)  180.deg - it else it }
        degree = innerValue.value.toInt()
        minute = ((innerValue.value - degree) * 60.0).toInt()
        second = (innerValue.value - degree - minute / 60.0) * 3600.0
    }

    override fun toString(): String {
        return buildString {
            if (isNegative) append("-")
            append("${degree}deg ${minute}m ${(second * 10).roundToInt() / 10.0}s")
        }
    }

}

inline val Declination.deg: Degree get() = this.value

inline fun Degree.toDec() = Declination(this)
inline fun Radian.toDec() = Declination(this.toDeg())