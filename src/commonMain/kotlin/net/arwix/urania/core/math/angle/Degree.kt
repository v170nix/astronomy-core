package net.arwix.urania.core.math.angle

import kotlin.jvm.JvmInline
import kotlin.math.floor

@JvmInline
value class Degree(val value: Double): Comparable<Degree> {

    inline operator fun plus(other: Degree) = Degree(value + other.value)
    inline operator fun minus(other: Degree) = Degree(value - other.value)
    inline operator fun unaryMinus() = Degree(-value)
    inline operator fun div(other: Double) = Degree(value / other)
    inline operator fun div(other: Degree): Double  = value / other.value
    inline operator fun times(other: Double) = Degree(value * other)
    inline operator fun times(other: Degree): Double = value * other.value

    override fun compareTo(other: Degree) = value.compareTo(other.value)

    inline fun normalize(): Degree {
        if (value < 0.0 && value >= -360.0) return this + 360.0.deg
        if (value >= 360.0 && value < 720.0) return this - 360.0.deg
        if (value >= 0.0 && value < 360.0) return this

        var d = value - 360.0 * floor(value / 360.0)
        // Can't use Math.IEEE remainder here because remainder differs
        // from modulus for negative numbers.
        if (d < 0.0) d += 360.0

        return d.deg
    }

}

inline operator fun Double.plus(degree: Degree): Double = this + degree.value
inline operator fun Double.minus(degree: Degree): Double = this - degree.value
inline operator fun Double.times(degree: Degree): Double = this * degree.value
inline operator fun Double.div(other: Degree): Double  = this / other.value

inline val Int.deg: Degree get() = Degree(this.toDouble())
inline val Double.deg: Degree get() = Degree(this)
