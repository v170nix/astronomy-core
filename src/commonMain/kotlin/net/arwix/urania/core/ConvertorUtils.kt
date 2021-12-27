package net.arwix.urania.core

import net.arwix.urania.core.math.angle.Radian.Companion.Zero
import net.arwix.urania.core.math.angle.asRadian
import net.arwix.urania.core.math.angle.cos
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.math.vector.RectangularVector
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.math.vector.Vector
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

inline fun <reified T: Vector> convert(vector: Vector): T {
    when (vector) {
        is T -> return vector
        is SphericalVector -> {
            val cosEl = cos(vector.theta)
            val rectangularVector = RectangularVector(
                vector.r * cos(vector.phi) * cosEl,
                vector.r * sin(vector.phi) * cosEl,
                vector.r * sin(vector.theta)
            )
            if (rectangularVector is T) return rectangularVector else throw NotImplementedError()
        }
        is RectangularVector -> {
            val sphericalVector = SphericalVector.Zero
            val XYSqr = vector.x * vector.x + vector.y * vector.y
            // Модуль вектора
            sphericalVector.r = sqrt(XYSqr + vector.z * vector.z)
            // Азимут вектора
            sphericalVector.phi = if (vector.x == 0.0 && vector.y == 0.0) 0.asRadian else atan2(vector.y, vector.x).asRadian
            if (sphericalVector.phi < 0.asRadian) sphericalVector.phi += 2.asRadian * PI
            // высота вектора
            val rho = sqrt(XYSqr)
            sphericalVector.theta = if (vector.z == 0.0 && rho == 0.0) 0.asRadian else atan2(vector.z, rho).asRadian
            if (sphericalVector is T) return sphericalVector else throw NotImplementedError()
        }
        else -> throw NotImplementedError()
    }
}