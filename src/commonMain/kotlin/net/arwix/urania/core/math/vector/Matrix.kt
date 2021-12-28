package net.arwix.urania.core.math.vector

import net.arwix.urania.core.convert
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.cos
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.math.vector.RectangularVector.Companion.Zero
import net.arwix.urania.core.rectangular
import kotlin.jvm.JvmInline

@JvmInline
value class Matrix(val elements: Array<DoubleArray>) {

    constructor(array1: DoubleArray, array2: DoubleArray, array3: DoubleArray) : this(
        arrayOf(array1, array2, array3)
    )

    constructor(vector1: Vector, vector2: Vector, vector3: Vector) : this(
        arrayOf(
            vector1.rectangular.toArray(false),
            vector2.rectangular.toArray(false),
            vector3.rectangular.toArray(false)
        )
    )

    inline fun transpose(): Matrix {
        val out = Zero
        for (i in 0..2) {
            for (j in 0..2) {
                out[i, j] = this[j, i]
            }
        }
        return out
    }

    inline operator fun times(right: Matrix): Matrix = Zero.apply {
        for (i in 0..2) {
            for (j in 0..2) {
                this[i, j] = (0..2).sumOf { k -> this@Matrix[i, k] * right[k, j] }
            }
        }
    }

    inline operator fun times(right: Vector): Vector {
        val vector: RectangularVector = convert(right)
        return RectangularVector.Zero.apply {
            for (i in 0..2) {
                this[i] = (0..2).sumOf { j -> this@Matrix[i, j] * vector[j] }
            }
        }

    }

    inline operator fun timesAssign(matrix: Matrix) {
        val result = (this * matrix).elements
        elements[0] = result[0]
        elements[1] = result[1]
        elements[2] = result[2]
    }

    inline operator fun get(i: Int, j: Int): Double = elements[i][j]

    inline operator fun set(i: Int, j: Int, element: Double) {
        elements[i][j] = element
    }

    inline operator fun get(i: Int): Vector = RectangularVector(elements[i])
    inline operator fun set(i: Int, array: DoubleArray) {
        elements[i] = array
    }

    inline operator fun set(i: Int, vector: Vector) {
        elements[i] = convert<RectangularVector>(vector).toArray(false)
    }

    inline fun toArray(copy: Boolean): Array<DoubleArray> = if (copy) elements.copyOf() else elements

    companion object {

        inline val Zero
            get() = Matrix(
                arrayOf(
                    doubleArrayOf(0.0, 0.0, 0.0),
                    doubleArrayOf(0.0, 0.0, 0.0),
                    doubleArrayOf(0.0, 0.0, 0.0)
                )
            )

        const val AXIS_X = 1
        const val AXIS_Y = 2
        const val AXIS_Z = 3

        inline operator fun invoke(axis: Int, angle: Radian): Matrix = when (axis) {
            AXIS_X -> getRotateX(angle)
            AXIS_Y -> getRotateY(angle)
            AXIS_Z -> getRotateZ(angle)
            else -> throw IndexOutOfBoundsException()
        }

        /**
         * матрицы поворта вокруг осей базиса
         * elementary rotations
         */
        inline fun getRotateX(angle: Radian): Matrix {
            val s = sin(angle)
            val c = cos(angle)
            return Matrix(
                doubleArrayOf(1.0, 0.0, 0.0),
                doubleArrayOf(0.0, c, s),
                doubleArrayOf(0.0, -s, c)
            )
        }

        inline fun getRotateY(angle: Radian): Matrix {
            val s = sin(angle)
            val c = cos(angle)
            return Matrix(
                doubleArrayOf(c, 0.0, -s),
                doubleArrayOf(0.0, 1.0, 0.0),
                doubleArrayOf(s, 0.0, c)
            )
        }

        inline fun getRotateZ(angle: Radian): Matrix {
            val s = sin(angle)
            val c = cos(angle)
            return Matrix(
                doubleArrayOf(c, s, 0.0),
                doubleArrayOf(-s, c, 0.0),
                doubleArrayOf(0.0, 0.0, 1.0)
            )
        }

    }
}

inline operator fun RectangularVector.times(right: Matrix): Vector {
    return Zero.apply {
        (0..2).forEach { j -> this[j] = (0..2).sumOf { i -> this@times[i] * right[i, j] } }
    }
}

inline operator fun Vector.times(right: Matrix): Vector {
    return convert<RectangularVector>(this) * right
}

inline operator fun Vector.timesAssign(matrix: Matrix) {
    set(this * matrix)
}