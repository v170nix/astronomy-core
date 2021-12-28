package net.arwix.urania.core.math.vector

import net.arwix.urania.core.convert
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.rad
import kotlin.jvm.JvmInline
import kotlin.math.abs

@JvmInline
value class SphericalVector(private val values: DoubleArray) : Vector {

    constructor(phi: Radian, theta: Radian, r: Double) : this(doubleArrayOf(phi.value, theta.value, r))
    constructor(phi: Radian, theta: Radian) : this(doubleArrayOf(phi.value, theta.value, 1.0))
    constructor(vector: Vector) : this(convert<SphericalVector>(vector).run {
        doubleArrayOf(vector[0], vector[1], vector[2])
    })

    inline var phi
        get() = this[0].rad
        set(value) {
            this[0] = value.value
        }
    inline var theta
        get() = this[1].rad
        set(value) {
            this[1] = value.value
        }
    inline var r
        get() = this[2]
        set(value) {
            this[2] = value
        }

    fun set(phi: Radian, theta: Radian, r: Double) {
        this.phi = phi
        this.theta = theta
        this.r = r
    }

    override fun set(vector: Vector) {
        val sphericalVector: SphericalVector = convert(vector)
        set(sphericalVector.phi, sphericalVector.theta, sphericalVector.r)
    }

    override fun set(i: Int, value: Double) {
        values[i] = value
    }

    override fun toArray(copy: Boolean): DoubleArray {
        return if (copy) doubleArrayOf(phi.value, theta.value, r) else values
    }

    override fun get(index: Int): Double = values[index]

    override fun unaryMinus(): Vector {
        val rectangularVector: RectangularVector = convert(this)
        rectangularVector.x = -rectangularVector.x
        rectangularVector.y = -rectangularVector.y
        rectangularVector.z = -rectangularVector.z
        return rectangularVector
    }

    override fun plus(vector: Vector) = convert<RectangularVector>(this) + vector
    override fun minus(vector: Vector) = convert<RectangularVector>(this) - vector
    override fun times(scalar: Double) = convert<RectangularVector>(this) * scalar
    override fun times(vector: Vector) = convert<RectangularVector>(this) * vector

    //    override fun times(right: Matrix) = convert<RectangularVector>(this) * right

    // TODO remove if
    override fun div(scalar: Double): Vector {
        return if (phi > 0.rad && theta > 0.rad && r > 0.0) {
            SphericalVector(phi, theta, r / scalar)
        } else {
            convert<RectangularVector>(this) / scalar
        }
    }

    override fun dot(vector: Vector) = convert<RectangularVector>(this) dot vector

    override fun normalize(): Double {
        return abs(r)
    }

    override fun component1(): Double = phi.value
    override fun component2(): Double = theta.value
    override fun component3(): Double = r

    // TODO equals all as RectangularVector
    override fun equalsVector(other: Vector): Boolean {
        return if (other is SphericalVector) {
            phi == other.phi && theta == other.theta && r == other.r
        } else {
            convert<SphericalVector>(other).let {
                phi == it.phi && theta == it.theta && r == it.r
            }
        }
    }

    companion object {
        inline val Zero get() = SphericalVector(doubleArrayOf(0.0, 0.0, 0.0))
    }
}