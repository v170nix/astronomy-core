package net.arwix.urania.core.math.vector

import kotlin.js.JsName
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
}