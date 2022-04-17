@file:Suppress("unused")

package net.arwix.urania.core.math.vector

import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.cos
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.rectangular
import net.arwix.urania.core.spherical
import kotlin.js.JsName
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt


interface Vector {
    fun set(vector: Vector)
    fun toArray(copy: Boolean): DoubleArray

    operator fun get(index: Int): Double
    @JsName("setComponent")
    operator fun set(i: Int, value: Double)
    operator fun unaryMinus(): Vector
    operator fun plus(vector: Vector): Vector
    operator fun minus(vector: Vector): Vector
    @JsName("timesScalar")
    operator fun times(scalar: Double): Vector
    operator fun times(vector: Vector): Vector
    operator fun div(scalar: Double): Vector
    infix fun dot(vector: Vector): Double
    fun normalize(): Double = sqrt(this dot this)
    operator fun plusAssign(vector: Vector) {
        set(this + vector)
    }
    operator fun minusAssign(vector: Vector) {
        set(this - vector)
    }
    @JsName("timesAssign")
    operator fun timesAssign(scalar: Double) {
        set(this * scalar)
    }
    operator fun timesAssign(vector: Vector) {
        set(this * vector)
    }
    operator fun divAssign(scalar: Double) {
        set(this / scalar)
    }

    operator fun component1(): Double
    operator fun component2(): Double
    operator fun component3(): Double

    fun equalsVector(other: Vector): Boolean

    companion object {

        /**
         * Obtain angular distance between two spherical coordinates.
         *
         * @return The distance in radians, from 0 to PI.
         */
        inline fun getAngularDistance(vector1: SphericalVector, vector2: SphericalVector): Radian {
            val v1 = SphericalVector(vector1.phi, vector1.theta).rectangular
            val v2 = SphericalVector(vector2.phi, vector2.theta).rectangular

            val dx = v1[0] - v2[0]
            val dy = v1[1] - v2[1]
            val dz = v1[2] - v2[2]

            val r2 = dx * dx + dy * dy + dz * dz

            return acos(1.0 - r2 * 0.5).rad
        }

        /**
         * Obtain exact position angle between two spherical coordinates.
         *
         * @return The position angle in radians.
         */
        inline fun getPositionAngle(vector1: SphericalVector, vector2: SphericalVector): Radian {
            val dl = vector2.theta - vector1.theta
            val cbp: Double = cos(vector2.phi)
            val y: Double = sin(dl) * cbp
            val x: Double = sin(vector2.phi) * cos(vector1.phi) - cbp * sin(vector1.phi) * cos(dl)
            return if (x != 0.0 || y != 0.0) -atan2(y, x).rad else Radian.Zero
        }
    }
}

/**
 * Obtain angular distance between two spherical coordinates.
 *
 * @return The distance in radians, from 0 to PI.
 */
inline fun Vector.getAngularDistance(other: Vector): Radian = Vector.getAngularDistance(this.spherical, other.spherical)

/**
 * Obtain exact position angle between two spherical coordinates.
 *
 * @return The position angle in radians.
 */
inline fun Vector.getPositionAngle(other: Vector): Radian = Vector.getPositionAngle(this.spherical, other.spherical)