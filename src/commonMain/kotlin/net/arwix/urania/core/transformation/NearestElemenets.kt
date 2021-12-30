package net.arwix.urania.core.transformation

import net.arwix.urania.core.transformation.nutation.Nutation
import net.arwix.urania.core.transformation.obliquity.Obliquity
import net.arwix.urania.core.transformation.precession.Precession

fun Precession.findNearestObliquityModel(): Obliquity {
    return when (this) {
        Precession.DE4xxx,
        Precession.Williams1994 -> Obliquity.Williams1994
        Precession.IAU1976 -> Obliquity.IAU1976
        Precession.Laskar1986 -> Obliquity.Laskar1986
        Precession.Simon1994 -> Obliquity.Simon1994
        Precession.Vondrak2011 -> Obliquity.Vondrak2011
        Precession.IAU2000,
        Precession.IAU2006,
        Precession.IAU2009 -> Obliquity.IAU2006
    }
}

fun Precession.findNearestNutationModel(): Nutation {
    return when (this) {
        Precession.IAU2006,
        Precession.IAU2009 -> Nutation.IAU2006
        Precession.DE4xxx,
        Precession.IAU1976,
        Precession.Laskar1986,
        Precession.Simon1994,
        Precession.Williams1994 -> Nutation.IAU1980
        Precession.Vondrak2011,
        Precession.IAU2000 -> Nutation.IAU2000
    }
}