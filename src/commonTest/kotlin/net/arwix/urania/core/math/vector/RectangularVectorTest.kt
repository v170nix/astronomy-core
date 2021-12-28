package net.arwix.urania.core.math.vector

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RectangularVectorTest {

    @Test
    fun div() {
        var leftVector = RectangularVector(x = 0.25, y = 0.75, z = 0.23)
        var resultVector = leftVector / 2.1

        assertContentEquals(
            doubleArrayOf(0.11904761904761904, 0.3571428571428571, 0.10952380952380952),
            resultVector.toArray(false)
        )

        assertContentEquals(
            doubleArrayOf(0.25, 0.75, 0.23),
            leftVector.toArray(false)
        )

        leftVector = RectangularVector(x = 0.25, y = -0.75, z = 0.23)
        resultVector = leftVector / 2.1

        assertContentEquals(
            doubleArrayOf(0.11904761904761904, -0.3571428571428571, 0.10952380952380952),
            resultVector.toArray(false)
        )

        assertContentEquals(
            doubleArrayOf(0.25, -0.75, 0.23),
            leftVector.toArray(false)
        )

        leftVector = RectangularVector(x = 0.25, y = -0.75, z = 0.23)
        leftVector /= 2.1

        assertContentEquals(
            doubleArrayOf(0.11904761904761904, -0.3571428571428571, 0.10952380952380952),
            leftVector.toArray(false)
        )
    }

    @Test
    fun normalize() {
        val leftVector = RectangularVector(x = 20.22, y = 2.75, z = -2.21)
//        val rightVector = RectangularVector(x = 0.1, y = 2.2, z = -3.3)

        val resultVector = leftVector.normalize()

        assertEquals(
            20.525471979957,
            leftVector.normalize()
        )
    }

    @Test
    fun plus() {
        val leftVector = RectangularVector(x = 20.22, y = 2.75, z = -2.21)
        val rightVector = RectangularVector(x = 0.1, y = 2.2, z = -3.3)

        val resultVector = leftVector + rightVector

        assertContentEquals(
            doubleArrayOf(20.32, 4.95, -5.51),
            resultVector.toArray(false)
        )

        leftVector += rightVector

        assertContentEquals(
            doubleArrayOf(20.32, 4.95, -5.51),
            leftVector.toArray(false)
        )
    }

    @Test
    fun minus() {
        val leftVector = RectangularVector(x = 20.22, y = 2.75, z = -2.21)
        val rightVector = RectangularVector(x = 0.1, y = 2.2, z = -3.3)

        val resultVector = leftVector - rightVector

        assertEquals(20.12, resultVector.toArray(false)[0], 1e-14)
        assertEquals(0.55, resultVector.toArray(false)[1], 1e-14)
        assertEquals(1.09, resultVector.toArray(false)[2], 1e-14)

        leftVector -= rightVector

        assertEquals(20.12, leftVector.toArray(false)[0], 1e-14)
        assertEquals(0.55, leftVector.toArray(false)[1], 1e-14)
        assertEquals(1.09, leftVector.toArray(false)[2], 1e-14)
    }

    @Test
    fun times() {
        val leftVector = RectangularVector(x = 20.22, y = -2.75, z = -2.21)
        val rightVector = RectangularVector(x = -0.1, y = 2.2, z = -3.3)

        var resultVector = leftVector * rightVector

        assertContentEquals(
            doubleArrayOf(13.937, 66.947, 44.209),
            resultVector.toArray(false)
        )

        assertContentEquals(
            doubleArrayOf(20.22, -2.75, -2.21),
            leftVector.toArray(false)
        )

        assertContentEquals(
            doubleArrayOf(-0.1, 2.2, -3.3),
            rightVector.toArray(false)
        )

        resultVector = leftVector * 2.0

        assertContentEquals(
            doubleArrayOf(40.44, -5.5, -4.42),
            resultVector.toArray(false)
        )

        assertContentEquals(
            doubleArrayOf(20.22, -2.75, -2.21),
            leftVector.toArray(false)
        )

        leftVector *= 2.0

        assertContentEquals(
            doubleArrayOf(40.44, -5.5, -4.42),
            leftVector.toArray(false)
        )

        leftVector *= rightVector

        assertContentEquals(
            doubleArrayOf(27.874, 133.894, 88.418),
            leftVector.toArray(false)
        )

    }

}