package net.arwix.urania.core.math.vector

import net.arwix.urania.core.assertContentEquals
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.rectangular
import kotlin.test.Test
import kotlin.test.assertContentEquals

class MatrixTest {

    @Test
    fun transpose() {
        val leftMatrix = Matrix(
            doubleArrayOf(0.1, 2.3, -3.1),
            doubleArrayOf(-2.5, 1.7, 3.3),
            doubleArrayOf(4.2, 0.23, 1.2),
        )

        val result = leftMatrix.transpose()

        assertContentEquals(doubleArrayOf(0.1, -2.5, 4.2), result[0].toArray(false))
        assertContentEquals(doubleArrayOf(2.3, 1.7, 0.23), result[1].toArray(false))
        assertContentEquals(doubleArrayOf(-3.1, 3.3, 1.2), result[2].toArray(false))
    }

    @Test
    fun times() {
        val leftMatrix = Matrix(
            doubleArrayOf(0.1, 2.3, -3.1),
            doubleArrayOf(-2.5, 1.7, 3.3),
            doubleArrayOf(4.2, 0.23, 1.2),
        )

        val rightMatrix = Matrix(
            doubleArrayOf(1.2, -0.3, 0.1),
            doubleArrayOf(15.5, 4.7, 1.45),
            doubleArrayOf(3.4, 1.3, 7.2),
        )

        val result = leftMatrix * rightMatrix

        assertContentEquals(doubleArrayOf(25.23, 6.75, -18.975), result[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(34.57, 13.03, 25.975), result[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(12.685, 1.381, 9.3935), result[2].toArray(false), 1e-14)

        assertContentEquals(doubleArrayOf(0.1, 2.3, -3.1), leftMatrix[0].toArray(false))
        assertContentEquals(doubleArrayOf(-2.5, 1.7, 3.3), leftMatrix[1].toArray(false))
        assertContentEquals(doubleArrayOf(4.2, 0.23, 1.2), leftMatrix[2].toArray(false))

        assertContentEquals(doubleArrayOf(1.2, -0.3, 0.1), rightMatrix[0].toArray(false))
        assertContentEquals(doubleArrayOf(15.5, 4.7, 1.45), rightMatrix[1].toArray(false))
        assertContentEquals(doubleArrayOf(3.4, 1.3, 7.2), rightMatrix[2].toArray(false))

        val vector = RectangularVector(1.2, 3.2, -3.8)

        val result1 = leftMatrix * vector

        assertContentEquals(
            doubleArrayOf(19.26, -10.1, 1.216),
            result1.rectangular.toArray(false), 1e-14
        )

        assertContentEquals(doubleArrayOf(0.1, 2.3, -3.1), leftMatrix[0].toArray(false))
        assertContentEquals(doubleArrayOf(-2.5, 1.7, 3.3), leftMatrix[1].toArray(false))
        assertContentEquals(doubleArrayOf(4.2, 0.23, 1.2), leftMatrix[2].toArray(false))

        assertContentEquals(
            doubleArrayOf(1.2, 3.2, -3.8),
            vector.rectangular.toArray(false), 1e-14
        )

        leftMatrix *= rightMatrix

        assertContentEquals(doubleArrayOf(25.23, 6.75, -18.975), leftMatrix[0].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(34.57, 13.03, 25.975), leftMatrix[1].toArray(false), 1e-14)
        assertContentEquals(doubleArrayOf(12.685, 1.381, 9.3935), leftMatrix[2].toArray(false), 1e-14)


        val result2 = vector * rightMatrix

        assertContentEquals(
            doubleArrayOf(38.12, 9.74, -22.6),
            result2.rectangular.toArray(false),
            1e-14
        )

        assertContentEquals(doubleArrayOf(1.2, -0.3, 0.1), rightMatrix[0].toArray(false))
        assertContentEquals(doubleArrayOf(15.5, 4.7, 1.45), rightMatrix[1].toArray(false))
        assertContentEquals(doubleArrayOf(3.4, 1.3, 7.2), rightMatrix[2].toArray(false))

        assertContentEquals(doubleArrayOf(1.2, 3.2, -3.8), vector.rectangular.toArray(false),)

        vector *= rightMatrix

        assertContentEquals(
            doubleArrayOf(38.12, 9.74, -22.6),
            vector.rectangular.toArray(false),
            1e-14
        )

    }

    @Test
    fun angles() {
        var matrix = Matrix(Matrix.AXIS_X, 2.4.rad)
        assertContentEquals(
            doubleArrayOf(1.0, 0.0, 0.0),
            matrix[0].toArray(false)
        )
        assertContentEquals(
            doubleArrayOf(0.0, -0.7373937155412454, 0.675463180551151),
            matrix[1].toArray(false), 1e-14
        )
        assertContentEquals(
            doubleArrayOf(0.0, -0.675463180551151, -0.7373937155412454),
            matrix[2].toArray(false), 1e-14
        )

        matrix = Matrix(Matrix.AXIS_Y, (-2.4).rad)
        assertContentEquals(
            doubleArrayOf(-0.7373937155412454, 0.0, 0.675463180551151),
            matrix[0].toArray(false), 1e-14
        )
        assertContentEquals(
            doubleArrayOf(0.0, 1.0, 0.0),
            matrix[1].toArray(false)
        )
        assertContentEquals(
            doubleArrayOf(-0.675463180551151, 0.0, -0.7373937155412454),
            matrix[2].toArray(false), 1e-14
        )

        matrix = Matrix(Matrix.AXIS_Z, (-2.4).rad)
        assertContentEquals(
            doubleArrayOf(-0.7373937155412454, -0.675463180551151, 0.0),
            matrix[0].toArray(false), 1e-14
        )
        assertContentEquals(
            doubleArrayOf(0.675463180551151, -0.7373937155412454, 0.0),
            matrix[1].toArray(false), 1e-14
        )
        assertContentEquals(
            doubleArrayOf(0.0, 0.0, 1.0),
            matrix[2].toArray(false)
        )
    }

}