package net.arwix.urania.core.observer

import kotlinx.datetime.Instant
import net.arwix.urania.core.calendar.*
import net.arwix.urania.core.math.PI_OVER_TWO
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.math.vector.SphericalVector
import net.arwix.urania.core.toDeg
import net.arwix.urania.core.transformation.nutation.Nutation
import net.arwix.urania.core.transformation.nutation.createElements
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.obliquity.createElements
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2

// https://bitbucket.org/talonsoalbi/jparsec/raw/372f81ebe5e66d530bf89a0ced19ca2fd8570f24/src/main/java/jparsec/ephem/Ephem.java
fun topocentricTransformation(instant: Instant, body: SphericalVector, position: Observer.Position) {
    var lst = instant.toMJD(false).getLocalApparentSiderealTime(SiderealTimeMethod.Williams1994, position) {
        val obliquity = Obliquity.Williams1994.createElements((instant.toMJD(false) + 300.mJD).toJT())
        val nutation = Nutation.IAU1980.createElements((instant.toMJD(false) + 300.mJD).toJT(), obliquity.id)
        getEquationOfEquinoxes(obliquity.meanObliquity, nutation.angles)
    }
//    lst = ((18.0 + 1.0 / 60.0  + 53.6475 / 60.0 / 60.0) * 15.0).deg.toRad()
    println("L_Ap_Sid_Time ${lst.toRightAscension()}")

    lst = instant.toMJD(false).getLocalApparentSiderealTime(SiderealTimeMethod.Williams1994, position)
    println("L_Ap_Sid_Time ${lst.toRightAscension()}")


    val angle = lst - body.phi

    val sinlat = sin(position.latitude)
    val coslat = cos(position.latitude)

    val sindec = sin(body.theta)
    val cosdec = cos(body.theta)
    val cosangh = cos(angle)

//    val hourMatrix =Matrix.getRotateY(Radian.PI / 2.0 - position.latitude)
//    val sp1 = SphericalVector(angle, body.theta, body.r)
//    val azimuthalVector = (hourMatrix * sp1).spherical

    val h = sinlat * sindec + coslat * cosdec * cosangh
    val alt: Double = asin(h)
    val y: Double = sin(angle)
    var x = cosangh * sinlat - sindec * coslat / cosdec
    val azi: Double = PI + atan2(y, x)

    // paralactic angle
    x = sinlat / coslat * cosdec - sindec * cosangh
    var p = 0.0
    p = if (x != 0.0) {
        atan2(y, x)
    } else {
        y / abs(y) * PI_OVER_TWO
    }

    println("elevation ${alt.rad.toDeg().toDeclination()} ${(-5.691134).deg.toDeclination()}")
    println("azimuth ${azi.rad.toDeg().toRightAscension()} ${29.171914.deg.toRightAscension()}")
    println("paralactic angle ${p.rad.normalize().toDeg()}")
}