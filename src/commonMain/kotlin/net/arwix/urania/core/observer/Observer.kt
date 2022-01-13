@file:Suppress("unused")

package net.arwix.urania.core.observer

import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.angle.times
import net.arwix.urania.core.physic.Ellipsoid
import net.arwix.urania.core.physic.EllipsoidObject
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sqrt

data class Observer(
    val position: Position,
    val weather: Weather = Weather(),
    val ellipsoid: Ellipsoid = EllipsoidObject.Earth
) {
    /**
     * @param longitude in radians measured to the east of the observer
     * @param latitude in radians of the observer.
     * @param altitude above sea level in meters of the observer
     */
    data class Position(val longitude: Radian, val latitude: Radian, val altitude: Double)

    /**
     * @param temperature in celsius
     * @param pressure in millibars
     * @param humidity in percentage
     */
    data class Weather(val temperature: Int = 10, val pressure: Int = 1010, val humidity: Int = 20) {

        companion object {
            fun createFromAltitudeWithDefaultPressure(
                temperature: Int = 10,
                humidity: Int = 20,
                altitude: Double
            ): Weather {
                return Weather(temperature, (1010.0 * exp(-altitude / 9100.0)).toInt(), humidity)
            }
        }

    }

    /**
     * Gets the angle of depression of the horizon. An object will be just in
     * the geometric horizon when it's elevation is equal to minus this value.
     * This correction can modify the time of the events by some minutes.
     *
     * @return The angle in radians.
     */
    fun getHorizonDepression(): Radian {
        val ratio: Double = ellipsoid.getPolarRadius() / ellipsoid.equatorialRadius * 0.5
        val rho: Double = ellipsoid.equatorialRadius * (1.0 - ratio + ratio * cos(2.0 * position.latitude))
        return acos(sqrt(rho / (rho + position.altitude / 1000.0))).rad
    }


}