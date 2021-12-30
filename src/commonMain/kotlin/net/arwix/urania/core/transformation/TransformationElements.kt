package net.arwix.urania.core.transformation

import net.arwix.urania.core.annotation.ExperimentalUrania
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.transformation.nutation.NutationElements
import net.arwix.urania.core.transformation.nutation.createElements
import net.arwix.urania.core.transformation.obliquity.ObliquityElements
import net.arwix.urania.core.transformation.obliquity.createElements
import net.arwix.urania.core.transformation.precession.Precession
import net.arwix.urania.core.transformation.precession.createElements

@ExperimentalUrania
class TransformationElements(precession: Precession, jt: JT) {

    private val precessionElements = precession.createElements(jt)
    private val obliquityElements: ObliquityElements = precession.findNearestObliquityModel().createElements(jt)
    private val nutationElements: NutationElements =
        precession.findNearestNutationModel().createElements(jt, obliquityElements.id)

    val matrix = if (precession.plane == Plane.Ecliptic) {
        nutationElements.equatorialMatrix!! * obliquityElements.eclipticToEquatorialMatrix * precessionElements.fromJ2000Matrix
    } else {
        nutationElements.equatorialMatrix!! * precessionElements.fromJ2000Matrix * obliquityElements.eclipticToEquatorialMatrix
    }
}