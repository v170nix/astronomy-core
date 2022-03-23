package net.arwix.urania.core.physic

import net.arwix.urania.core.annotation.Apparent
import net.arwix.urania.core.annotation.Ecliptic
import net.arwix.urania.core.annotation.Equatorial
import net.arwix.urania.core.annotation.Geocentric
import net.arwix.urania.core.calendar.*
import net.arwix.urania.core.ephemeris.EphemerisVector
import net.arwix.urania.core.ephemeris.Epoch
import net.arwix.urania.core.ephemeris.Orbit
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.LIGHT_TIME_DAYS_PER_AU
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.spherical
import net.arwix.urania.core.toDeg
import net.arwix.urania.core.toRad
import net.arwix.urania.core.transformation.findNearestObliquityModel
import net.arwix.urania.core.transformation.obliquity.ObliquityElements
import net.arwix.urania.core.transformation.obliquity.createElements
import net.arwix.urania.core.transformation.precession.Precession
import net.arwix.urania.core.transformation.precession.PrecessionElements
import net.arwix.urania.core.transformation.precession.createElements
import kotlin.math.*


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

    companion object {

        // https://wgc.jpl.nasa.gov:8443/webgeocalc/#SubObserverPoint
        // https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/Tutorials/pdf/individual_docs/17_frames_and_coordinate_systems.pdf
        // IAU_MARS
        fun createElements(
            model: Physic.Model,
            @Apparent @Geocentric @Ecliptic sunVector: Vector, // observer-sun vector
            @Apparent @Geocentric @Ecliptic target: Vector, // observer-body vector
            body: Physic.Body,
            jT: JT,
            @Equatorial precessionElements: PrecessionElements = Precession.IAU2000.createElements(jT),
            obliquityElements: ObliquityElements = precessionElements.id.findNearestObliquityModel().createElements(jT)
        ): PhysicEphemeris {
            if (precessionElements.id.plane != Plane.Equatorial) {
                throw IllegalArgumentException("Need equatorial precession plane")
            }

            val helioObject = (sunVector - target).spherical // sun-body vector
            val re = sunVector.normalize()
            val rp = helioObject.r
            val ro = target.normalize()
            val lp = helioObject.phi
            val le = Radian.PI + target.spherical.phi

            val angularRadius = body.ellipsoid.getAngularRadius(ro)
            val elongation = acos((ro * ro + re * re - rp * rp) / (2.0 * ro * re)).rad
            val cosPhaseAngle = (ro * ro + rp * rp - re * re) / (2.0 * ro * rp)
            val sinPhaseAngle = re * sin(le - lp) / ro
            val phase = (0.5 * (1.0 + cosPhaseAngle))
            val phaseAngle = acos(cosPhaseAngle).rad
            val magnitude = with(body) {
                model.getMagnitude(ro, if (body == Physic.Body.Sun) 1.0 else rp, phaseAngle.toDeg())
            }

            val lightTime = ro * LIGHT_TIME_DAYS_PER_AU
            val northPole = with(body) { model.getNorthPole(jT - JT(lightTime / 36525.0)) }

            val currentNorthPoleVector = precessionElements.changeEpoch(
                SphericalVector(northPole.rightAscension, northPole.declination, ro),
                Epoch.Apparent
            ).spherical

            val locEq = obliquityElements.rotatePlane(target, Plane.Equatorial).spherical
            val locNP =
                SphericalVector(phi = currentNorthPoleVector.phi, theta = currentNorthPoleVector.theta, 1.0).spherical
            val locEqSun = obliquityElements.rotatePlane(sunVector, Plane.Equatorial).spherical

            val positionAngleOfPole = planetocentricToPlanetogeodeticLatitude(
                body,
                dotProduct(currentNorthPoleVector.phi, currentNorthPoleVector.theta, locEq.phi, locEq.theta)
            )

            val positionAngleOfAxis = (PI +
                    atan2(
                        cos(northPole.declination) * sin(locEq.phi - northPole.rightAscension),
                        cos(northPole.declination) * sin(locEq.theta) *
                                cos(locEq.phi - northPole.rightAscension) -
                                sin(northPole.declination) * cos(locEq.theta)
                    )).rad

            var longitudeOfCentralMeridian: Radian
            var longitudeOfCentralMeridianSystemI: Radian? = null
            var longitudeOfCentralMeridianSystemII: Radian? = null
            var longitudeOfCentralMeridianSystemIII: Radian? = null
            var subsolarLongitude: Radian? = null
            var subsolarLatitude: Radian? = null
            var brightLimbAngle: Radian? = null
            with(body) {

                val D = cos(locEq.theta) * sin(locNP.phi - locEq.phi)
                val N = sin(locNP.theta) * cos(locEq.theta) * cos(locNP.phi - locEq.phi) -
                        cos(locNP.theta) * sin(locEq.theta)

                val deltaLongitude = atan2(N, D).rad.toDeg()

                val d = jT.toMJD() - MJD.J2000 - lightTime.mJD
                val meridian0 = (model.getSpeedRotation() * d).deg - deltaLongitude
                var meridian = meridian0 + model.getLongitudeJ2000(jT)

                if (model.getSpeedRotation() < 0.0) meridian = 360.deg - meridian
                meridian = meridian.normalize()

                longitudeOfCentralMeridian = meridian.toRad()
                when (body) {
                    Physic.Body.Jupiter -> {
                        longitudeOfCentralMeridianSystemI =
                            (-(-67.1 + deltaLongitude - (meridian0 + deltaLongitude) * 877.9 / model.getSpeedRotation())).deg.toRad()
                        longitudeOfCentralMeridianSystemII =
                            (-(-43.3 + deltaLongitude - (meridian0 + deltaLongitude) * 870.27 / model.getSpeedRotation())).deg.toRad()
                        longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridian
                    }
                    Physic.Body.Saturn -> {
                        longitudeOfCentralMeridianSystemI =
                            (-(-227.2037 + deltaLongitude - (meridian0 + deltaLongitude) * 844.3 / model.getSpeedRotation())).deg.toRad()
                        longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridian
                    }
                    Physic.Body.Sun,
                    Physic.Body.Moon,
                    Physic.Body.Earth -> {
                        // This inversion is due to historical reasons
                        longitudeOfCentralMeridian = Radian.PI2 - longitudeOfCentralMeridian
                    }
                    Physic.Body.Uranus -> {
                        longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridian
                    }
                    Physic.Body.Neptune -> {
                        longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridian
                    }
                    else -> {}
                }
            }


            if (body != Physic.Body.Sun) {
                var fromSun = (target - sunVector).spherical
                val fromSunEq = obliquityElements.rotatePlane(fromSun, Plane.Equatorial).spherical
                subsolarLatitude = planetocentricToPlanetogeodeticLatitude(
                    body,
                    dotProduct(currentNorthPoleVector.phi, currentNorthPoleVector.theta, fromSunEq.phi, fromSunEq.theta)
                )

                fromSun = (locEq - locEqSun).spherical
                val ra = fromSun.phi
                val dec = fromSun.theta
                val D = cos(dec) * sin(locNP.phi - ra)
                var N = sin(locNP.theta) * cos(dec) * cos(locNP.phi - ra)
                N -= cos(locNP.theta) * sin(dec)

                var deltaLongitude = 0.0
                if (D != 0.0) deltaLongitude = atan(N / D).rad.toDeg().value
                if (D < 0.0) deltaLongitude += 180.0
                with(body) {
                    subsolarLongitude =
                        ((model.getSpeedRotation() * (jT * 36525.0 - lightTime)).deg - deltaLongitude.deg + model.getLongitudeJ2000(
                            jT
                        )).toRad()
                    if (model.getSpeedRotation() < 0) subsolarLongitude = Radian.PI2 - subsolarLongitude!!
                    subsolarLongitude = subsolarLongitude?.normalize()
                }

                when (body) {
                    Physic.Body.Moon,
                    Physic.Body.Earth -> {
                        // This inversion is due to historical reasons
                        subsolarLongitude = -subsolarLongitude!!
                    }
                    else -> {}
                }

                brightLimbAngle = (PI + atan2(
                    cos(locEqSun.theta) *
                            sin(currentNorthPoleVector.phi - locEqSun.phi), cos(locEqSun.theta) *
                            sin(currentNorthPoleVector.theta) * cos(currentNorthPoleVector.phi - locEqSun.phi) -
                            sin(locEqSun.theta) * cos(currentNorthPoleVector.theta)
                )).rad
            }

            return PhysicEphemeris(
                elongation = elongation,
                relative = if (sinPhaseAngle < 0) Relative.Trails else Relative.Leads,
                phaseAngle = acos(cosPhaseAngle).rad,
                phase = phase,
                distance = ro,
                distanceFromSun = rp,
                angularDiameter = angularRadius * 2.0,
                defectOfIllumination = angularRadius * 2.0 * (1.0 - phase),
                magnitude = magnitude,
                northPole = northPole,
                longitudeOfCentralMeridian = longitudeOfCentralMeridian,
                longitudeOfCentralMeridianSystemI = longitudeOfCentralMeridianSystemI?.normalize(),
                longitudeOfCentralMeridianSystemII = longitudeOfCentralMeridianSystemII?.normalize(),
                longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridianSystemIII?.normalize(),
                positionAngleOfPole = positionAngleOfPole,
                positionAngleOfAxis = positionAngleOfAxis,
                subsolarLongitude = subsolarLongitude,
                subsolarLatitude = subsolarLatitude,
                longitudeJ2000 = with(body) { model.getLongitudeJ2000(jT) },
                speedRotation = with(body) { model.getSpeedRotation() },
                brightLimbAngle = brightLimbAngle
            )
        }

        fun createElements(
            model: Physic.Model,
            @Apparent @Geocentric @Ecliptic sunVector: EphemerisVector, // observer-sun vector
            @Apparent @Geocentric @Ecliptic target: EphemerisVector, // observer-body vector
            body: Physic.Body, jT: JT
        ): PhysicEphemeris {

            if (sunVector.metadata.orbit != Orbit.Geocentric ||
                sunVector.metadata.plane != Plane.Ecliptic ||
                sunVector.metadata.epoch != target.metadata.epoch ||
                target.metadata.orbit != Orbit.Geocentric ||
                target.metadata.plane != Plane.Ecliptic
            ) throw IllegalArgumentException()

            return createElements(model, sunVector.value, target.value, body, jT)
        }
    }
}

// https://github.com/v170nix/astronomy-java-lib/blob/101c17e62da127a9ee8d5adc5c30e7a4d0737193/src/main/java/net/arwix/astronomy/physic/PhysicOrientation.java

/**
 * Transforms a given latitude from planetocentric to planetogeodetic.
 * @param lat Latitude in radians.
 * @param target Target body.
 * @return Planetogeodetic latitude.
 */
private fun planetocentricToPlanetogeodeticLatitude(body: Physic.Body, lat: Radian): Radian {
    val shape = 1.0 / body.ellipsoid.inverseFlatteningFactor
    if (shape == 1.0) return lat
    return atan(tan(lat.value) / ((1.0 - shape).pow(2.0))).rad
}


/**
 * Performs adequate dot product for axis orientation calculations. The
 * result is the planetocentric latitude of the object supposed that the
 * object's axis is pointing to pole_ra, pole_dec, and the object is
 * observed is a position p_ra, p_dec. The value should later be corrected
 * to planetogeodetic by applying the formula: geo_lat =
 * atan(tan(planeto_lat) / (1.0 - shape)^2), where shape = (equatorial -
 * polar radius) / (equatorial radius).
 *
 * @param poleRA Right ascension of the north pole.
 * @param poleDec Declination of the north pole.
 * @param pRA Right ascension of some planet as seen by the observer.
 * @param pDec Declination of some planet as seen by the observer.
 * @return Result of the dot product as a double precission value.
 */
private fun dotProduct(poleRA: Radian, poleDec: Radian, pRA: Radian, pDec: Radian): Radian {
    val poleDecIn = Radian.PI / 2.0 - poleDec
    val pDecIn = Radian.PI / 2.0 - pDec
    var dot = sin(poleDecIn) * cos(poleRA) * sin(pDecIn) * cos(pRA)
    dot += sin(poleDecIn) * sin(poleRA) * sin(pDecIn) * sin(pRA)
    dot += cos(poleDecIn) * cos(pDecIn)

    return -asin(dot).rad
}