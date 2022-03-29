package net.arwix.urania.core.math

import kotlin.math.floor

inline fun DoubleArray.polynomialSum(x: Double): Double {
    var t = 1.0
    return fold(0.0) { acc, d -> (acc + d * t).let { t *= x; it } }
}

inline fun Double.mod3600() = this - 1296000.0 * floor(this / 1296000.0)

inline fun frac(x: Double): Double {
    return x - floor(x)
}

infix fun Double.modulo(y: Double) = y * frac(this / y)