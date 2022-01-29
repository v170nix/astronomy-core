package net.arwix.urania.core.transformation

import kotlinx.datetime.Instant
import net.arwix.urania.core.calendar.*
import net.arwix.urania.core.ephemeris.EphemerisVector
import net.arwix.urania.core.ephemeris.Orbit
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.cos
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.observer.Observer
import net.arwix.urania.core.spherical
import kotlin.math.asin
import kotlin.math.atan2

inline fun Vector.rotateToTopocentric(
    instant: Instant,
    position: Observer.Position,
    siderealTimeMethod: SiderealTimeMethod = SiderealTimeMethod.Williams1994,
    getEquationOfEquinoxes: () -> Radian = {
        getEquationOfEquinoxes(instant.toMJD().toJT())
    }
): Vector {
    val innerVector = this.spherical
    val lst = instant.toMJD(false).getLocalApparentSiderealTime(
        siderealTimeMethod,
        position,
        getEquationOfEquinoxes
    )

//        val obliquity = Obliquity.Vondrak2011.createElements((instant.toMJD()).toJT())
//        val nutation = Nutation.IAU2006.createElements((instant.toMJD()).toJT(), obliquity.id)
//        getEquationOfEquinoxes(obliquity.meanObliquity, nutation.angles)

    val angle = lst - innerVector.phi

//    val hourMatrix = Matrix.getRotateY(Radian.PI / 2.0 - position.latitude)
//    val sp1 = SphericalVector(angle, spherical.theta, spherical.r)
//    val azimuthalVector = (hourMatrix * sp1).spherical

    val sinLatitude = sin(position.latitude)
    val cosLatitude = cos(position.latitude)

    val sinDeclination = sin(innerVector.theta)
    val cosDeclination = cos(innerVector.theta)
    val cosAngle = cos(angle)

    val h = sinLatitude * sinDeclination + cosLatitude * cosDeclination * cosAngle
    val altitude = asin(h).rad
    val y: Double = sin(angle)
    val x = cosAngle * sinLatitude - sinDeclination * cosLatitude / cosDeclination
    val azimuth = Radian.PI + atan2(y, x).rad

    // paralactic angle
//    x = sinLatitude / cosLatitude * cosDeclination - sinDeclination * cosAngle

//    val paralacticAngle = if (x != 0.0) {
//        atan2(y, x)
//    } else {
//        y / abs(y) * PI_OVER_TWO
//    }

    return SphericalVector(
        phi = azimuth,
        theta =  altitude,
        innerVector.r
    )
}

inline fun EphemerisVector.rotateToTopocentric(
    instant: Instant,
    position: Observer.Position,
    siderealTimeMethod: SiderealTimeMethod = SiderealTimeMethod.Williams1994,
    getEquationOfEquinoxes: () -> Radian = {
        getEquationOfEquinoxes(instant.toMJD().toJT())
    }
): EphemerisVector {
    if (metadata.plane != Plane.Equatorial ||
        metadata.orbit != Orbit.Geocentric
    ) throw IllegalStateException()

    return EphemerisVector(
        value = this.value.rotateToTopocentric(instant, position, siderealTimeMethod, getEquationOfEquinoxes),
        metadata = this.metadata.copy(plane = Plane.Topocentric)
    )
}