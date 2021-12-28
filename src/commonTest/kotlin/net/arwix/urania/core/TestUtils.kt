package net.arwix.urania.core

import kotlin.test.assertEquals

fun assertContentEquals(
    expected: DoubleArray,
    actual: DoubleArray,
    absoluteTolerance: Double,
    message: String? = null
) {

    actual.forEachIndexed { index, d ->
        assertEquals(expected[index], d, absoluteTolerance, message)
    }
}