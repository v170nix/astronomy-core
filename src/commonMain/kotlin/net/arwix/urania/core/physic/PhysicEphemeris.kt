package net.arwix.urania.core.physic

import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.math.angle.Radian


/**
 *
 * @param angularDiameter The equatorial angular width of the target body full disk, if it were fully
 * illuminated and visible to the observer.
 *
 * @param defectOfIllumination Defect of illumination. The maximum angular width of the target body's
 * assumed circular disk diameter NOT illuminated by the Sun.
 */
data class PhysicEphemeris(
    // S-O-T /r
    val elongation: Radian,
    // S-T-O
    val relative: Relative,
    val phaseAngle: Radian,
    val phase: Double,
    val distance: Double,
    val distanceFromSun: Double,

    /**
     * The equatorial angular width of the target body full disk, if it were fully
     * illuminated and visible to the observer.
     */
    val angularDiameter: Radian,

    /**
     * The maximum angular width of the target body's
     * assumed circular disk diameter NOT illuminated by the Sun.
     */
    val defectOfIllumination: Radian,
    val magnitude: Double,
    val northPole: NorthPole,

    /**
     * Apparent planetodetic latitude of the center of the target
     * disc seen by the OBSERVER. (ObsSub-LON) This is NOT exactly the same as the
     * "nearest" sub-point for a non-spherical target shape (since the center of
     * the disc might not be the point closest to the observer), but is generally
     * very close if not a very irregular body shape. Down-leg light travel-time
     * from target to observer is taken into account.
     * The reference ellipsoid is an oblate
     * spheroid with a single flatness coefficient in which the y-axis body radius
     * is taken to be the same value as the x-axis radius. Positive longitude is to
     * the WEST for this target. Cartographic system is given in the header.
     */
    val longitudeOfCentralMeridian: Radian,
    val longitudeOfCentralMeridianSystemI: Radian?,
    val longitudeOfCentralMeridianSystemII: Radian?,
    val longitudeOfCentralMeridianSystemIII: Radian?,

    /**
     * Apparent planetodetic latitude of the center of the target
     * disc seen by the OBSERVER. (ObsSub-LAT) This is NOT exactly the same as the
     * "nearest" sub-point for a non-spherical target shape (since the center of
     * the disc might not be the point closest to the observer), but is generally
     * very close if not a very irregular body shape. Down-leg light travel-time
     * from target to observer is taken into account. Latitude is the angle between
     * the equatorial plane and the line perpendicular to the reference ellipsoid of
     * the body, so includes body oblateness. The reference ellipsoid is an oblate
     * spheroid with a single flatness coefficient in which the y-axis body radius
     * is taken to be the same value as the x-axis radius.
     */
    val positionAngleOfPole: Radian,

    val positionAngleOfAxis: Radian,

    /**
     * Apparent sub-solar longitude and latitude of the Sun on the target. The
     * apparent planetodetic longitude and latitude of the center of the target disc
     * as seen from the Sun, as seen by the observer at print-time. This is NOT
     * exactly the same as the "sub-solar" (nearest) point for a non-spherical target
     * shape (since the center of the disc seen from the Sun might not be the closest
     * point to the Sun), but is very close if not a highly irregular body shape.
     * Light travel-time from Sun to target and from target to observer is taken into
     * account.  Latitude is the angle between the equatorial plane and the line
     * perpendicular to the reference ellipsoid of the body. The reference ellipsoid
     * is an oblate spheroid with a single flatness coefficient in which the y-axis
     * body radius is taken to be the same value as the x-axis radius.  Positive
     * longitude is to the WEST for this target. Cartographic system is given in the
     * header.
     */
    val subsolarLatitude: Radian?,
    val subsolarLongitude: Radian?,
    val brightLimbAngle: Radian?,


    /**
     * longitude of planet at J2000
     */
    val longitudeJ2000: Degree,

    /**
     * speed rotation in degrees/day
     */
    val speedRotation: Double,
) {

    enum class Relative { Trails, Leads }

    /**
     * ICRF right ascension and declination of the target body's north-pole
     * direction at the time light left the body to be observed at print time.
     */
    data class NorthPole(val rightAscension: Radian, val declination: Radian)
}