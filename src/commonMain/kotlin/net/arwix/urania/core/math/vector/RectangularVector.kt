package net.arwix.urania.core.math.vector

import net.arwix.urania.core.convert
import kotlin.jvm.JvmInline

@JvmInline
value class RectangularVector(private val values: DoubleArray): Vector {

    constructor(x: Double, y: Double, z: Double) : this(doubleArrayOf(x, y, z))
    constructor(vector: Vector) : this(convert<RectangularVector>(vector).run {
        doubleArrayOf(vector[0], vector[1], vector[2])
    })

    inline var x
        get() = this[0]
        set(value) {
            this[0] = value
        }
    inline var y
        get() = this[1]
        set(value) {
            this[1] = value
        }
    inline var z
        get() = this[2]
        set(value) {
            this[2] = value
        }

    override fun set(vector: Vector) {
        val rectangularVector: RectangularVector = convert(vector)
        set(rectangularVector.x, rectangularVector.y, rectangularVector.z)
    }

    fun set(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun set(i: Int, value: Double) {
        values[i] = value
    }

    override fun toArray(copy: Boolean): DoubleArray {
        return if (copy) values.copyOf() else values
    }

    override fun get(index: Int): Double = values[index]

    override fun unaryMinus(): Vector = RectangularVector(-x, -y, -z)

    override fun plus(vector: Vector): Vector {
        val rightVector: RectangularVector = convert(vector)
        return RectangularVector(x + rightVector.x, y + rightVector.y, z + rightVector.z)
    }

    override fun minus(vector: Vector): Vector {
        val rightVector: RectangularVector = convert(vector)
        return RectangularVector(x - rightVector.x, y - rightVector.y, z - rightVector.z)
    }

    override fun times(scalar: Double): Vector {
        return RectangularVector(x * scalar, y * scalar, z * scalar)
    }

//    override fun times(right: Matrix): Vector {
//        return Zero.apply {
//            (0..2).forEach { j -> this[j] = (0..2).sumOf { i -> this@RectangularVector[i] * right[i, j] } }
//        }
//    }

    override fun times(vector: Vector): Vector {
        val rightVector: RectangularVector = convert(vector)
        return RectangularVector(
            y * rightVector.z - z * rightVector.y,
            z * rightVector.x - x * rightVector.z,
            x * rightVector.y - y * rightVector.x
        )
    }

    override fun div(scalar: Double): Vector {
        return this * (1.0 / scalar)
    }

    override fun dot(vector: Vector): Double {
        val rightVector: RectangularVector = convert(vector)
        return x * rightVector.x + y * rightVector.y + z * rightVector.z
    }

    override fun component1(): Double {
        return x
    }

    override fun component2(): Double {
        return y
    }

    override fun component3(): Double {
        return z
    }

    override fun equalsVector(other: Vector): Boolean {
        return if (other is RectangularVector) {
            x == other.x && y == other.y && z == other.z
        } else {
            convert<RectangularVector>(other).let {
                x == it.x && y == it.y && z == it.z
            }
        }
    }

    companion object {
        inline val Zero get() = RectangularVector(0.0, 0.0, 0.0)
    }

}