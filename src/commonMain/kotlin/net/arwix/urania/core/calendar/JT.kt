package net.arwix.urania.core.calendar

import net.arwix.urania.core.math.angle.Degree
import kotlin.jvm.JvmInline

@JvmInline
value class JT(val value: Double): Comparable<JT> {

    inline operator fun plus(other: JT) = JT(value + other.value)
    inline operator fun minus(other: JT) = JT(value - other.value)
    inline operator fun unaryMinus() = JT(-value)
    inline operator fun div(other: Double): Double = value / other
    inline operator fun div(other: JT): Double  = value / other.value
    inline operator fun times(other: Double): Double = value * other
    inline operator fun times(other: JT): Double = value * other.value

    override fun compareTo(other: JT) = value.compareTo(other.value)
}

inline operator fun Double.times(jT: JT): Double = jT * this

inline val Int.jT: JT get() = JT(this.toDouble())
inline val Double.jT: JT get() = JT(this)