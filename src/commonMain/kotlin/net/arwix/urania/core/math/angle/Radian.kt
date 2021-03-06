package net.arwix.urania.core.math.angle

import kotlin.jvm.JvmInline
import kotlin.math.floor

@Suppress("MemberVisibilityCanBePrivate")
@JvmInline
value class Radian(val value: Double): Comparable<Radian> {

    inline operator fun plus(other: Radian) = Radian(value + other.value)
    inline operator fun minus(other: Radian) = Radian(value - other.value)
    inline operator fun unaryMinus() = Radian(-value)
    inline operator fun div(other: Double) = Radian(value / other)
    inline operator fun div(other: Radian): Double  = value / other.value
    inline operator fun times(other: Double) = Radian(value * other)
    inline operator fun times(other: Radian): Double = value * other.value

    override fun compareTo(other: Radian) = value.compareTo(other.value)

    inline fun normalize(): Radian {
        if (this < Zero && this >= -PI2) return this + PI2
        if (this >= PI2 && this < PI4) return this - PI2
        if (this >= Zero && this < PI2) return this

        var d = this - PI2 * floor(this / PI2)
        // Can't use Math.IEEE remainder here because remainder differs
        // from modulus for negative numbers.
        if (d < Zero) d += PI2
        return d
    }

    inline fun cos(): Double = kotlin.math.cos(value)
    inline fun sin(): Double = kotlin.math.sin(value)

    companion object {
        val PI = Radian(kotlin.math.PI)
        val PI2 = Radian(kotlin.math.PI * 2.0)
        val PI4 = Radian(kotlin.math.PI * 4.0)
        val Zero = Radian(0.0)
    }

}

inline operator fun Double.plus(radian: Radian): Double = this + radian.value
inline operator fun Double.minus(radian: Radian): Double = this - radian.value
inline operator fun Double.times(radian: Radian): Double = this * radian.value
inline operator fun Double.div(radian: Radian): Double  = this / radian.value

inline fun cos(radian: Radian): Double = kotlin.math.cos(radian.value)
inline fun sin(radian: Radian): Double = kotlin.math.sin(radian.value)
inline fun tan(radian: Radian): Double = kotlin.math.tan(radian.value)

inline val Double.rad: Radian get() = Radian(this)
inline val Int.rad: Radian get() = Radian(this.toDouble())