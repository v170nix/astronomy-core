package net.arwix.urania.core.math.vector

import net.arwix.urania.core.assertContentEquals
import net.arwix.urania.core.convert
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.rectangular
import net.arwix.urania.core.spherical
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SphericalVectorTest {

    @Test
    fun div() {
        var leftVector = SphericalVector(phi = 0.25.rad, theta = 0.75.rad, r = 0.23)
        var resultVector = leftVector / 2.1

        assertContentEquals(
            doubleArrayOf(0.25, 0.75, 0.10952380952380952),
            resultVector.spherical.toArray(false),
            1e-14
        )

        assertContentEquals(
            doubleArrayOf(0.25, 0.75, 0.23),
            leftVector.spherical.toArray(false)
        )

        leftVector = SphericalVector(phi = 0.25.rad, theta = (-0.75).rad, r = -0.23)
        resultVector = leftVector / 2.1

        assertContentEquals(
            doubleArrayOf(3.391592653589793, 0.75, 0.10952380952380952),
            resultVector.spherical.toArray(false)
        )

        assertContentEquals(
            doubleArrayOf(0.25, -0.75, -0.23),
            leftVector.spherical.toArray(false),
            1e-14
        )

        leftVector = SphericalVector(phi = 0.25.rad, theta = (-0.75).rad, r = 0.23)
        leftVector /= 2.1

        assertContentEquals(
            doubleArrayOf(0.25, -0.75, 0.10952380952380952),
            leftVector.spherical.toArray(false),
            1e-14
        )
    }

    @Test
    fun normalize() {
        val leftVector = SphericalVector(phi = 20.22.rad , theta = 2.75.rad, r = -2.21)
        assertEquals(
            2.21,
            leftVector.normalize()
        )
    }

    @Test
    fun plus() {
        val leftVector = SphericalVector(phi = 20.22.rad, theta = 2.75.rad, r = 2.21)
        val rightVector = SphericalVector(phi = 0.1.rad, theta = (-2.2).rad, r = 3.3)

        val resultVector = leftVector + rightVector

        assertContentEquals(
            doubleArrayOf(-2.338880162566874, -2.195728870782442, -1.824567340168994),
            resultVector.rectangular.toArray(false),
            1e-14
        )

        assertContentEquals(
            doubleArrayOf(20.22, 2.75, 2.21),
            leftVector.spherical.toArray(false)
        )

        leftVector += rightVector

        assertContentEquals(
            doubleArrayOf(3.8954326902807646, -0.5171219214165093, 3.690613996023933),
            leftVector.spherical.toArray(false),
            1e-14
        )
    }

    @Test
    fun minus() {
        val leftVector = SphericalVector(phi = (-20.22).rad, theta = 2.75.rad, r = 2.21)
        val rightVector = SphericalVector(phi = 0.1.rad, theta = 2.2.rad, r = 3.3)

        val resultVector = leftVector - rightVector

        assertEquals(1.5258228528360769, resultVector.rectangular.toArray(false)[0], 1e-14)
        assertEquals(2.195728870782442, resultVector.rectangular.toArray(false)[1], 1e-14)
        assertEquals(-1.824567340168994, resultVector.rectangular.toArray(false)[2], 1e-14)

        leftVector -= rightVector

        assertEquals(0.9634980528930793, leftVector.spherical.toArray(false)[0], 1e-14)
        assertEquals(-0.5988023238918723, leftVector.spherical.toArray(false)[1], 1e-14)
        assertEquals(3.2370367052345452, leftVector.spherical.toArray(false)[2], 1e-14)
    }

    @Test
    fun times() {
        var leftVector = SphericalVector(phi = 20.22.rad, theta = (-2.75).rad, r = 2.21)
        val rightVector = SphericalVector(phi = (-0.1).rad, theta = 2.2.rad, r = 3.3)

        var resultVector = leftVector * rightVector

        assertContentEquals(
            doubleArrayOf(-5.177470492297332, 2.7145160106425488, -3.9470906290354018),
            resultVector.rectangular.toArray(false),
            1e-14
        )

        assertContentEquals(
            doubleArrayOf(20.22, -2.75, 2.21),
            leftVector.spherical.toArray(false)
        )

        assertContentEquals(
            doubleArrayOf(-0.1, 2.2, 3.3),
            rightVector.spherical.toArray(false)
        )

        resultVector = leftVector * 2.0

        assertContentEquals(
            doubleArrayOf(-0.8130573097307968, -4.003694031806777, -1.6869415848713059),
            resultVector.rectangular.toArray(false),
            1e-14
        )

        assertContentEquals(
            doubleArrayOf(20.22, -2.75, 2.21),
            leftVector.spherical.toArray(false)
        )

        leftVector *= 2.0

        assertContentEquals(
            doubleArrayOf(4.512036732051032, -0.39159265358979317, 4.42),
            leftVector.spherical.toArray(false),
            1e-14
        )

        leftVector = SphericalVector(phi = 20.22.rad, theta = (-2.75).rad, r = 2.21)

        leftVector *= rightVector

        assertContentEquals(
            doubleArrayOf(2.6586993736175835, -0.5938781752824578, 7.053674383217827),
            leftVector.spherical.toArray(false),
            1e-14
        )

    }

}