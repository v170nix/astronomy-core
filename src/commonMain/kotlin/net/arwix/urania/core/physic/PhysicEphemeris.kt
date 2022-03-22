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
import net.arwix.urania.core.transformation.obliquity.Obliquity
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
    val elongation: Radian,
    val relative: Relative,
    val phaseAngle: Radian,
    val phase: Double,
    val distance: Double,
    val distanceFromSun: Double,
    val angularDiameter: Radian,
    val defectOfIllumination: Radian,
    val magnitude: Double,
    val northPole: NorthPole,

    /**
     * Apparent planetodetic latitude of the center of the target
     * disc seen by the OBSERVER. This is NOT exactly the same as the
     * "nearest" sub-point for a non-spherical target shape (since the center of
     * the disc might not be the point closest to the observer), but is generally
     * very close if not a very irregular body shape. Down-leg light travel-time
     * from target to observer is taken into account. Latitude is the angle between
     * the equatorial plane and the line perpendicular to the reference ellipsoid of
     * the body, so includes body oblateness. The reference ellipsoid is an oblate
     * spheroid with a single flatness coefficient in which the y-axis body radius
     * is taken to be the same value as the x-axis radius. Positive longitude is to
     * the WEST for this target. Cartographic system is given in the header.
     */
    val positionAngleOfPole: Radian,
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

            // - JT(ro * LIGHT_TIME_DAYS_PER_AU / 36525.0)
            val northPole = with(body) { model.getNorthPole(jT) }

            val currentNorthPoleVector = precessionElements.changeEpoch(
                SphericalVector(northPole.rightAscension, northPole.declination, ro),
                Epoch.Apparent
            ).spherical

            val locEq = obliquityElements.rotatePlane(target, Plane.Equatorial).spherical
            val locNP = SphericalVector(phi = currentNorthPoleVector.phi, theta = currentNorthPoleVector.theta, 1.0)
            val locEqSun = obliquityElements.rotatePlane(sunVector, Plane.Equatorial).spherical

            val positionAngleOfPole = planetocentricToPlanetogeodeticLatitude(
                body,
                dotProduct(currentNorthPoleVector.phi, currentNorthPoleVector.theta, locEq.phi, locEq.theta)
            )
            println("!positionAngleOfPole центральная точка f(e) ObsSub-LAT ${positionAngleOfPole.toDeg()} ")

            if (body != Physic.Body.Sun) {
                var fromSun = (target - sunVector).spherical
                val fromSunEq = obliquityElements.rotatePlane(fromSun, Plane.Equatorial).spherical
                val subsolarLatitude = dotProduct(currentNorthPoleVector.phi, currentNorthPoleVector.theta, fromSunEq.phi, fromSunEq.theta)
                println("!subsolarLatitude подсолнечная точка f(s) SunSub-LAT ${subsolarLatitude.toDeg()} ")
                val subsolarLatitude1 = planetocentricToPlanetogeodeticLatitude(body, subsolarLatitude)
                println("!subsolarLatitude1 подсолнечная точка f(s) SunSub-LAT ${subsolarLatitude1.toDeg()} ")

                fromSun = (locEq - locEqSun).spherical
                val ra = fromSun.phi
                val dec = fromSun.theta
                val D = cos(dec) * sin(locNP.phi - ra)
                var N = sin(locNP.theta) * cos(dec) * cos(locNP.phi - ra)
                N -= cos(locNP.theta) * sin(dec)

                var delta_lon = 0.0
                if (D != 0.0) delta_lon = atan(N / D).rad.toDeg().value
                if (D < 0.0) delta_lon += 180.0
                with(body) {

                    var subsolarLongitude =
                        (model.getSpeedRotation() * (jT * 36525.0)).deg - delta_lon.deg + model.getLongitudeJ2000(jT)
                    if (model.getSpeedRotation() < 0) subsolarLongitude = 360.deg - subsolarLongitude
                    println("!subsolarLongitude подсолнечная точка L(s) SunSub-LON ${subsolarLongitude.normalize()}")
                }

                val brightLimbAngle = (PI + atan2(
                    cos(locEqSun.theta) *
                            sin(currentNorthPoleVector.phi - locEqSun.phi), cos(locEqSun.theta) *
                            sin(currentNorthPoleVector.theta) * cos(currentNorthPoleVector.phi - locEqSun.phi) -
                            sin(locEqSun.theta) * cos(currentNorthPoleVector.theta)
                ))

                println("!brightLimbAngle ${brightLimbAngle.rad.toDeg()}")

            }

            val positionAngleOfAxis = (PI +
                    atan2(
                        cos(northPole.declination) * sin(locEq.phi - northPole.rightAscension),
                        cos(northPole.declination) * sin(locEq.theta) *
                                cos(locEq.phi - northPole.rightAscension) -
                                sin(northPole.declination) * cos(locEq.theta)
                    )).rad

            println("!positionAngleOfAxis позиционный угол оси ${positionAngleOfAxis.toDeg()} ")

//            val ephem = precessPoleFromJ2000(jT, precessionElements, body, model, target.normalize()).spherical


            val D = cos(locEq.theta) * sin(locNP.phi - locEq.phi)
            var N = sin(locNP.theta) * cos(locEq.theta) * cos(locNP.phi - locEq.phi)
            N -= cos(locNP.theta) * sin(locEq.theta)

            val delta_lon = atan2(N, D).rad.toDeg()

            val lightTime = (target.normalize() * LIGHT_TIME_DAYS_PER_AU)

            with(body) {

                val d = jT.toMJD() - MJD.J2000 - lightTime.mJD // lightTime.mJD
                val W = (model.getLongitudeJ2000(jT) + (model.getSpeedRotation() * d).deg).toRad()
                var meridian = W.toDeg() - delta_lon

                if (model.getSpeedRotation() < 0.0) meridian = 360.deg - meridian
                meridian = meridian.normalize()

                var longitudeOfCentralMeridian = meridian.toRad()
                when (body) {
                    Physic.Body.Jupiter -> {

                    }
                    Physic.Body.Saturn -> {

                    }
                    Physic.Body.Sun,
                    Physic.Body.Moon,
                    Physic.Body.Earth -> {
                        // This inversion is due to historical reasons
                        longitudeOfCentralMeridian = Radian.PI2 - longitudeOfCentralMeridian
                    }
                    Physic.Body.Uranus -> {

                    }
                    Physic.Body.Neptune -> {

                    }
                    else -> {}
                }

                println("!longitudeOfCentralMeridian центральная точка L(e) ObsSub-LON ${longitudeOfCentralMeridian.toDeg()}")
            }


            calsAxisJpForSun(model, jT, sunVector, target.spherical, body)



            println(northPole)

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
                positionAngleOfPole = positionAngleOfPole,
                longitudeJ2000 = with(body) { model.getLongitudeJ2000(jT) },
                speedRotation = with(body) { model.getSpeedRotation() }
            ).also {
                println("ephemeris $it")
            }
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

// https://wgc.jpl.nasa.gov:8443/webgeocalc/#SubObserverPoint
// https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/Tutorials/pdf/individual_docs/17_frames_and_coordinate_systems.pdf
// IAU_MARS

private fun calsAxisJpForSun(
    model: Physic.Model,
    jT: JT,
    sun: Vector,
    target: SphericalVector,
    body: Physic.Body

) {
    val obliquity = Obliquity.Williams1994.createElements(jT)

    val precessionEq = Precession.IAU2000.createElements(jT)

    val northPole = with(body) { model.getNorthPole(jT) }

    val ephem = precessPoleFromJ2000(precessionEq, northPole, body, target.normalize()).spherical // true
    val locEq = obliquity.rotatePlane(
        target,
        Plane.Equatorial
    ).spherical // obliquity.rotatePlane(ephem, Plane.Equatorial).spherical
    val locNP = SphericalVector(phi = ephem.phi, theta = ephem.theta, 1.0)
    val locEqSun = obliquity.rotatePlane(sun, Plane.Equatorial).spherical

//    val targetEq = obliquity.rotatePlane(target, Plane.Equatorial).spherical // true
    val lightTime = (target.normalize() * LIGHT_TIME_DAYS_PER_AU)

    if (body !is Physic.Body.Sun) {
        var fromSun = (target - sun).spherical
        val fromSunEq = obliquity.rotatePlane(fromSun, Plane.Equatorial).spherical
        val subsolarLatitude = dotProduct(ephem.phi, ephem.theta, fromSunEq.phi, fromSunEq.theta)
        println("subsolarLatitude ${subsolarLatitude.toDeg()} ")
        val subsolarLatitude1 = planetocentricToPlanetogeodeticLatitude(body, subsolarLatitude)
        println("subsolarLatitude1 ${subsolarLatitude1.toDeg()} ")

        fromSun = (locEq - locEqSun).spherical
        val ra = fromSun.phi
        val dec = fromSun.theta
        val D = cos(dec) * sin(locNP.phi - ra)
        var N = sin(locNP.theta) * cos(dec) * cos(locNP.phi - ra)
        N -= cos(locNP.theta) * sin(dec)

        var delta_lon = 0.0
        if (D != 0.0) atan(N / D).rad.toDeg().value
        if (D < 0.0) delta_lon += 180.0
        with(body) {

            var subsolarLongitude =
                (model.getSpeedRotation() * (jT * 36525.0)).deg - delta_lon.deg + model.getLongitudeJ2000(jT)
            if (model.getSpeedRotation() < 0) subsolarLongitude = 360.deg - subsolarLongitude
            println("subsolarLongitude ${subsolarLongitude.normalize()}")
        }

        val brightLimbAngle = (PI + atan2(
            cos(locEqSun.theta) *
                    sin(ephem.phi - locEqSun.phi), cos(locEqSun.theta) *
                    sin(ephem.theta) * cos(ephem.phi - locEqSun.phi) -
                    sin(locEqSun.theta) * cos(ephem.theta)
        ))

        println("brightLimbAngle ${brightLimbAngle.rad.toDeg()}")


    }

    // Obtain position angle of pole as seen from Earth
    val positionAngleOfPole = dotProduct(ephem.phi, ephem.theta, locEq.phi, locEq.theta)
    println("positionAngleOfPole ${positionAngleOfPole.toDeg()} ")

    // Correct value (planetocentric to planeto-geodetic latitude)
    val positionAngleOfPole1 = planetocentricToPlanetogeodeticLatitude(body, positionAngleOfPole)
    println("positionAngleOfPole1 ${positionAngleOfPole1.toDeg()} ")

    val D = cos(locEq.theta) * sin(locNP.phi - locEq.phi);
    var N = sin(locNP.theta) * cos(locEq.theta) *
            cos(locNP.phi - locEq.phi)
    N -= cos(locNP.theta) * sin(locEq.theta)

    val delta_lon = atan2(N, D).rad.toDeg()

    with(body) {

        val d = jT.toMJD() - MJD.J2000 - lightTime.mJD // lightTime.mJD
        val W = (model.getLongitudeJ2000(jT) + (model.getSpeedRotation() * d).deg).toRad()

        var meridian = W.toDeg() - delta_lon
        if (model.getSpeedRotation() < 0.0) meridian = 360.deg - meridian
        meridian = meridian.normalize()
        val longitudeOfCentralMeridian = meridian.toRad()

        println("longitudeOfCentralMeridian ${longitudeOfCentralMeridian.toDeg()}")
        // for sun
        val sunLongitudeOfCentralMeridian = Radian.PI2 - longitudeOfCentralMeridian
        println("sunLongitudeOfCentralMeridian ${sunLongitudeOfCentralMeridian.toDeg()}")
        println("====================================================================")
    }
}

private fun precessPoleFromJ2000(

    precession: PrecessionElements,
    northPole: PhysicEphemeris.NorthPole,
    body: Physic.Body,
    distance: Double
): Vector {
    val locationElement = with(body) {
        SphericalVector(
            phi = northPole.rightAscension,
            theta = northPole.declination,
            r = distance
        )
    }
    return precession.changeEpoch(locationElement, Epoch.Apparent)
}

// https://github.com/v170nix/astronomy-java-lib/blob/101c17e62da127a9ee8d5adc5c30e7a4d0737193/src/main/java/net/arwix/astronomy/physic/PhysicOrientation.java
//private fun calcAxis(jT: JT,
//                     sun: Vector,
//                     target: Vector,
//                     model: Physic.PhysicModel,
//                     rotationElements: PhysicElements,
//                     body: Physic.Body
//) {
//
//    var n = 0.0
//    var d = 0.0
//    var deltaLon = 0.0
//
//    val precession = Precession.DE4xxx.createElements(jT)
//    val obliquity = Obliquity.Williams1994.createElements(jT)
//
//
//    val targetEq = obliquity.rotatePlane(target, Plane.Equatorial)
//    val sunEq = obliquity.rotatePlane(sun, Plane.Equatorial)
//    val deltaR = (- targetEq)
//
//    val lightTime =  (deltaR.spherical.r * LIGHT_TIME_DAYS_PER_AU / 36525.0).jT
//    val lt0 = (12.19873734 / 60.0 / 24.0  / 36525.0).jT
//
////    val elements = body.createRotationElements(Physic.PhysicModel.IAU2015, jT)
//
//    val eqPrecession = Precession.IAU2000.createElements(jT - lt0)
//
//    val ephem = precessPoleFromJ2000(jT, eqPrecession, rotationElements, target.normalize()).spherical
//
//    d = (jT - lt0) * 36525.0
//
//    val W = (model.getLongitudeJ2000(jT) + (model.getSpeedRotation() * d).deg).toRad()
//    val orientation = Matrix(Matrix.AXIS_Z, W) *
//            Matrix(Matrix.AXIS_X, Radian.PI / 2.0 - ephem.theta) *
//            Matrix(Matrix.AXIS_Z, Radian.PI / 2.0 + ephem.phi)
//
//    val orientation1 = eqPrecession.toJ2000Matrix * rotationElements.createRotationMatrix(jT - lt0)
//    val e =  orientation1
//    val s1 = (e * deltaR).spherical
//    val long = Radian.PI2 - s1.phi
//
//    println("paOfPole ${long.toDeg()}")
//
//    val planetocentricLatitude = s1.theta
//
//    println("position Angle Of Pole ${s1.theta.toDeg()}")
//
//    val positionAngleOfPol = planetocentricToPlanetogeodeticLatitude(body, planetocentricLatitude)
//
//    println("position Angle Of Pole ${positionAngleOfPol.toDeg()}")
//
//
//    val locationElement = SphericalVector(
//        phi = rotationElements.northPole.rightAscension,
//        theta = rotationElements.northPole.declination,
//        r = target.normalize()
//    )
//
//    println("cNorthPole ${locationElement.phi.toDeg()}")
//
//    val currentNorthPole = Precession.DE4xxx.createElements(jT).changeEpoch(locationElement, Epoch.Apparent).spherical
//
//    println("currentNorthPole ${currentNorthPole.phi.toDeg()}")
//
//    val locNP = SphericalVector(phi = currentNorthPole.phi, theta = currentNorthPole.theta, 1.0)
//
//    val locEq = obliquity.rotatePlane(currentNorthPole, Plane.Equatorial).spherical
//    println("currentLocEq ${locEq.phi.toDeg()}")
//
//    val locEqSun = obliquity.rotatePlane(sun, Plane.Equatorial)
//
//    val targetEqu = obliquity.rotatePlane(target, Plane.Equatorial).spherical
//
//    val e1 = targetEqu / targetEqu.r
//    var e2 =  SphericalVector(Radian.Zero, Radian.Zero, 1.0) * targetEqu
//    e2 = e2 / e2.normalize()
//    val e3 = e1 * e2
//    val c = currentNorthPole dot e3
//    val s = currentNorthPole dot e2
//    val phi = atan2(c, s)
//    println("phi ${phi.rad.toDeg()}")
//
//    val paOfPole = dotProduct(currentNorthPole.phi, currentNorthPole.theta, targetEqu.phi, targetEqu.theta)
//
//    println("paOfPole!!! ${paOfPole.toDeg()}")
//
//    val positionAngleOfPole = planetocentricToPlanetogeodeticLatitude(body, paOfPole)
//
////    val positionAngleOfAxis = (Radian.PI + atan2(cos(locNP)))
//
//    println("position Angle Of Pole ${positionAngleOfPole.toDeg()}")
//
//
//}


/**
 * Transforms a given latitude from planetocentric to planetogeodetic.
 * @param lat Latitude in radians.
 * @param target Target body.
 * @return Planetogeodetic latitude.
 */
fun planetocentricToPlanetogeodeticLatitude(body: Physic.Body, lat: Radian): Radian {
    val shape = 1.0 / body.ellipsoid.inverseFlatteningFactor
    if (shape == 1.0) return lat
    println("lat ${tan(lat.value)}")
    println("shape ${((1.0 - shape).pow(2.0))}")



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

//private fun calsAxisJpForSunVersion1(
//    jT: JT,
//    sun: Vector,
//    target: Vector,
//    rotationElements: PhysicElements,
//    body: Physic.Body
//
//) {
//    val precession = Precession.DE4xxx.createElements(jT)
//    val obliquity = Obliquity.Williams1994.createElements(jT)
//    val ephem = precessPoleFromJ2000(jT, precession, rotationElements, target.normalize()).spherical
//    val locEq = obliquity.rotatePlane(ephem, Plane.Equatorial).spherical
//    val locNP = SphericalVector(phi = ephem.phi, theta = ephem.theta, 1.0)
//    val locEqSun = obliquity.rotatePlane(sun, Plane.Equatorial).spherical
//
//    val targetEq = obliquity.rotatePlane(target, Plane.Equatorial).spherical
//
//    if (body !is Physic.Body.Sun) {
//        var fromSun = (target - sun).spherical
//        val fromSunEq = obliquity.rotatePlane(fromSun, Plane.Equatorial).spherical
//        val subsolarLatitude = dotProduct(ephem.phi, ephem.theta, fromSunEq.phi, fromSun.theta)
//        println("subsolarLatitude ${subsolarLatitude.toDeg()} ")
//        val subsolarLatitude1 =  planetocentricToPlanetogeodeticLatitude(body, subsolarLatitude)
//        println("subsolarLatitude1 ${subsolarLatitude1.toDeg()} ")
//
//        fromSun = (locEq - locEqSun).spherical
//        val ra = fromSun.phi
//        val dec = fromSun.theta
//        val D = cos(dec) * sin(locNP.phi - ra)
//        var N = sin(locNP.theta) * cos(dec) * cos(locNP.phi - ra)
//        N -= cos(locNP.theta) * sin(dec)
//
//        var delta_lon = 0.0
//        if (D != 0.0) atan(N / D).rad.toDeg().value
//        if (D < 0.0) delta_lon += 180.0
//        var subsolarLongitude = (rotationElements.speedRotation * (jT * 36525.0)).deg - delta_lon.deg + rotationElements.longitudeJ2000
//        if (rotationElements.speedRotation < 0) subsolarLongitude = 360.deg - subsolarLongitude
//        println("subsolarLongitude ${subsolarLongitude.normalize()}")
//
//        val brightLimbAngle = (PI + atan2(cos(locEqSun.theta) *
//                sin(ephem.phi - locEqSun.phi), cos(locEqSun.theta) *
//                sin(ephem.theta) * cos(ephem.phi - locEqSun.phi) -
//                sin(locEqSun.theta) * cos(ephem.theta)))
//
//        println("brightLimbAngle ${brightLimbAngle.rad.toDeg()}")
//
//
//    }
//
//    // Obtain position angle of pole as seen from Earth
//    val positionAngleOfPole = dotProduct(ephem.phi, ephem.theta, targetEq.phi, targetEq.theta)
//    println("positionAngleOfPole ${positionAngleOfPole.toDeclination()} ")
//
//    // Correct value (planetocentric to planeto-geodetic latitude)
//    val positionAngleOfPole1 =  planetocentricToPlanetogeodeticLatitude(body, positionAngleOfPole)
//    println("positionAngleOfPole1 ${positionAngleOfPole1.toDeclination()} ")
//
//    val D = cos(locEq.theta) * sin(locNP.phi - locEq.phi)
//    var N = sin(locNP.theta) * cos(locEq.theta) * cos(locNP.phi - locEq.phi)
//    N -= cos(locNP.theta) * sin(locEq.theta)
//
//    val delta_lon = atan2(N, D).rad.toDeg()
//
//    val meridian0 = (rotationElements.speedRotation * (jT * 36525.0)).deg - delta_lon
//    var meridian = meridian0 + rotationElements.longitudeJ2000
//    if (rotationElements.speedRotation < 0.0) meridian = 360.deg - meridian
//    meridian = meridian.normalize()
//    val longitudeOfCentralMeridian = meridian.toRad()
//
//    println("longitudeOfCentralMeridian ${longitudeOfCentralMeridian.toDeg()}")
//    // for sun
//    val sunLongitudeOfCentralMeridian = Radian.PI2 - longitudeOfCentralMeridian
//    println("sunLongitudeOfCentralMeridian ${sunLongitudeOfCentralMeridian.toDeg()}")
//    println("====================================================================")
//}
