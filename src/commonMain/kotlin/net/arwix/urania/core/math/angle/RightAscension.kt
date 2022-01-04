@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.arwix.urania.core.math.angle

import net.arwix.urania.core.toDeg
import kotlin.math.roundToInt

class RightAscension constructor(val value: Degree) {

    val hour: Int
    val minute: Int
    val second: Double

    init {
        val innerValue = value.normalize()
        val h = innerValue.value / 15.0
        hour = h.toInt()
        minute = ((h - hour) * 60.0).toInt()
        second = (h - hour - minute / 60.0) * 3600.0
    }

    override fun toString(): String {
        return "${hour}h ${minute}m ${(second * 100).roundToInt() / 100.0}s"
    }

}

inline val RightAscension.deg: Degree get() = this.value

inline fun Degree.toRA() = RightAscension(this)
inline fun Radian.toRA() = RightAscension(this.toDeg())
