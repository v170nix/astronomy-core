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
import net.arwix.urania.core.physic.impl.*
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


// https://github.com/v170nix/astronomy-java-lib/blob/101c17e62da127a9ee8d5adc5c30e7a4d0737193/src/main/java/net/arwix/astronomy/physic/PhysicOrientation.java
// https://bitbucket.org/v170n1x/sunexplorer.org/src/72854a8bdbcb12741971a24279ea6a560a172bfa/mobile/src/main/kotlin/org/sunexplorer/old/GraticulePath.kt?at=master#GraticulePath.kt-4
// https://web.archive.org/web/20200512151452/http://www.hnsky.org/iau-iag.htm

object Physic {

    sealed class Model {
        object IAU2000 : Model()
        object IAU2006 : Model()
        object IAU2009 : Model()
        object IAU2015 : Model()

        // https://arxiv.org/pdf/1808.01973.pdf
        object IAU2018 : Model()
    }

    interface Elements {
        fun Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole
        fun Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double

        /**
         * @return rotation in degrees/day
         */
        fun Model.getSpeedRotation(): Double
        fun Model.getLongitudeJ2000(jT: JT): Degree
    }

    sealed class Body(val ellipsoid: Ellipsoid) : Elements {
        object Sun : Body(EllipsoidObject.Sun), Elements by PhysicModelSunImpl
        object Mercury : Body(EllipsoidObject.Mercury), Elements by PhysicModelMercuryImpl
        object Venus : Body(EllipsoidObject.Venus), Elements by PhysicModelVenusImpl
        object Earth : Body(EllipsoidObject.Earth), Elements by PhysicModelEarthImpl
        object Mars : Body(EllipsoidObject.Mars), Elements by PhysicModelMarsImpl
        object Jupiter : Body(EllipsoidObject.Jupiter), Elements by PhysicModelJupiterImpl
        object Saturn : Body(EllipsoidObject.Saturn), Elements by PhysicModelSaturnImpl
        object Uranus : Body(EllipsoidObject.Uranus), Elements by PhysicModelUranusImpl
        object Neptune : Body(EllipsoidObject.Neptune), Elements by PhysicModelNeptuneImpl
        object Pluto : Body(EllipsoidObject.Pluto), Elements by PhysicModelPlutoImpl

        object Moon : Body(EllipsoidObject.Moon), Elements by PhysicModelMoonImpl
//
//        object Ida : Body(EllipsoidObject.None)
//        object Ceres : Body(EllipsoidObject.None)
//        object Pallas : Body(EllipsoidObject.None)
//        object Lutetia : Body(EllipsoidObject.None)
//        object Davida : Body(EllipsoidObject.None)
//        object Gaspra : Body(EllipsoidObject.None)
//        object Vesta : Body(EllipsoidObject.None)
//        object Eros : Body(EllipsoidObject.None)
//        object Steins : Body(EllipsoidObject.None)
//        object Itokawa : Body(EllipsoidObject.None)
//        object P9Tempel1 : Body(EllipsoidObject.None)
//        object P19Borrelly : Body(EllipsoidObject.None)
    }

    fun isSuperMoon(moonDistanceInKm: Double) = moonDistanceInKm < 360_000


    // https://wgc.jpl.nasa.gov:8443/webgeocalc/#SubObserverPoint
    // https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/Tutorials/pdf/individual_docs/17_frames_and_coordinate_systems.pdf
    // IAU_MARS
    fun createElements(
        jT: JT,
        body: Body,
        model: Model,
        @Apparent @Geocentric @Ecliptic sunVector: Vector, // observer-sun vector
        @Apparent @Geocentric @Ecliptic target: Vector, // observer-body vector
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
            model.getMagnitude(ro, if (body == Body.Sun) 1.0 else rp, phaseAngle.toDeg())
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
            val meridian0 = (model.getSpeedRotation() * d.value).deg - deltaLongitude
            var meridian = meridian0 + model.getLongitudeJ2000(jT)

            if (model.getSpeedRotation() < 0.0) meridian = 360.deg - meridian
            meridian = meridian.normalize()

            longitudeOfCentralMeridian = meridian.toRad()
            when (body) {
                Body.Jupiter -> {
                    longitudeOfCentralMeridianSystemI =
                        (-(-67.1 + deltaLongitude - (meridian0 + deltaLongitude) * 877.9 / model.getSpeedRotation())).deg.toRad()
                    longitudeOfCentralMeridianSystemII =
                        (-(-43.3 + deltaLongitude - (meridian0 + deltaLongitude) * 870.27 / model.getSpeedRotation())).deg.toRad()
                    longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridian
                }
                Body.Saturn -> {
                    longitudeOfCentralMeridianSystemI =
                        (-(-227.2037 + deltaLongitude - (meridian0 + deltaLongitude) * 844.3 / model.getSpeedRotation())).deg.toRad()
                    longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridian
                }
                Body.Sun,
                Body.Moon,
                Body.Earth -> {
                    // This inversion is due to historical reasons
                    longitudeOfCentralMeridian = Radian.PI2 - longitudeOfCentralMeridian
                }
                Body.Uranus -> {
                    longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridian
                }
                Body.Neptune -> {
                    longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridian
                }
                else -> {}
            }
        }


        if (body != Body.Sun) {
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
                Body.Moon,
                Body.Earth -> {
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
            relative = if (sinPhaseAngle < 0) PhysicEphemeris.Relative.Trails else PhysicEphemeris.Relative.Leads,
            phaseAngle = acos(cosPhaseAngle).rad,
            phase = phase,
            distance = ro,
            distanceFromSun = rp,
            angularDiameter = angularRadius * 2.0,
            defectOfIllumination = angularRadius * 2.0 * (1.0 - phase),
            magnitude = magnitude,
            northPole = northPole,
            longitudeOfCentralMeridian = longitudeOfCentralMeridian.normalize(),
            longitudeOfCentralMeridianSystemI = longitudeOfCentralMeridianSystemI?.normalize(),
            longitudeOfCentralMeridianSystemII = longitudeOfCentralMeridianSystemII?.normalize(),
            longitudeOfCentralMeridianSystemIII = longitudeOfCentralMeridianSystemIII?.normalize(),
            positionAngleOfPole = positionAngleOfPole,
            positionAngleOfAxis = positionAngleOfAxis,
            subsolarLongitude = subsolarLongitude?.normalize(),
            subsolarLatitude = subsolarLatitude,
            longitudeJ2000 = with(body) { model.getLongitudeJ2000(jT) },
            speedRotation = with(body) { model.getSpeedRotation() },
            brightLimbAngle = brightLimbAngle
        )
    }

    fun createElements(
        jT: JT,
        model: Model,
        @Apparent @Geocentric @Ecliptic sunVector: EphemerisVector, // observer-sun vector
        body: Body,
        @Apparent @Geocentric @Ecliptic target: EphemerisVector, // observer-body vector
        @Equatorial precessionElements: PrecessionElements = Precession.IAU2000.createElements(jT),
        obliquityElements: ObliquityElements = precessionElements.id.findNearestObliquityModel().createElements(jT)
    ): PhysicEphemeris {
        if (sunVector.metadata.orbit != Orbit.Geocentric ||
            sunVector.metadata.plane != Plane.Ecliptic ||
            sunVector.metadata.epoch != target.metadata.epoch ||
            target.metadata.orbit != Orbit.Geocentric ||
            target.metadata.plane != Plane.Ecliptic
        ) throw IllegalArgumentException()

        return createElements(jT, body, model, sunVector.value, target.value, precessionElements, obliquityElements)
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

}