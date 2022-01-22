package net.arwix.urania.core.transformation

import net.arwix.urania.core.annotation.ExperimentalUrania
import net.arwix.urania.core.math.AU
import net.arwix.urania.core.math.SPEED_OF_LIGHT
import net.arwix.urania.core.math.SUN_GRAVITATIONAL_CONSTANT
import net.arwix.urania.core.math.vector.RectangularVector
import net.arwix.urania.core.math.vector.Vector
import net.arwix.urania.core.rectangular
import net.arwix.urania.core.spherical
import kotlin.math.abs
import kotlin.math.pow

/**
 * Correct apparent coordinates for deflection, using an algorithm from
 * NOVAS package, based on Murray (1981) <I>Monthly Notices Royal
 * Astronomical Society 195, 639-648</I>. This correction is usually
 * lower than 1 arcsecond, and can be neglected most of the times. Only
 * for apparent coordinates.
 *
 * @param vep Vector from Earth (observer) to the planet (deflected body).
 * @param ves Vector from Earth (observer) to Sun.
 * @param vsp Vector from Sun to planet (deflected body).
 * @param deflector Vector from sun to deflector body. (0, 0, 0) if it is
 *        the sun.
 * @param relativeMassRatio Reciprocal mass of the deflector body in solar units,
 * equals to deflector body mass / Sun mass = 1 for Sun.
 * @return Array containing (x, y, z) corrected for deflection.
 */

@ExperimentalUrania
fun deflectionCorrection(
    vep: Vector,
    ves: Vector,
    vsp: Vector,
    deflector: Vector,
    relativeMassRatio: Double
): RectangularVector {

    if (relativeMassRatio == 0.0) return RectangularVector(vep)

    // Sun-Earth vector
    val vse = -ves

    // Deflector to Earth vector
    val deflectorToEarth = (vse - deflector).spherical
    // Deflector to planet vector
    val deflectorToPlanet = (vsp - deflector).spherical

    if (deflectorToEarth.r == 0.0 || deflectorToPlanet.r == 0.0 || vep.spherical.r == 0.0) {
        return vep.rectangular
    }

    // COMPUTE NORMALIZED DOT PRODUCTS OF VECTORS
    var DOT_PLANET = vep.rectangular[0] * deflectorToPlanet.rectangular[0] +
            vep.rectangular[1] * deflectorToPlanet.rectangular[1]+
            vep[2] * deflectorToPlanet.rectangular[2]

    var DOT_EARTH = deflectorToEarth.rectangular[0] * vep.rectangular[0] +
            deflectorToEarth.rectangular[1] * vep.rectangular[1] +
            deflectorToEarth.rectangular[2] * vep.rectangular[2]

    var DOT_DEFLECTOR = deflectorToPlanet.rectangular[0] * deflectorToEarth.rectangular[0] +
            deflectorToPlanet.rectangular[1] * deflectorToEarth.rectangular[1] +
            deflectorToPlanet.rectangular[2] * deflectorToEarth.rectangular[2]

    DOT_PLANET = DOT_PLANET / (vep.spherical.r * deflectorToPlanet.spherical.r)
    DOT_EARTH = DOT_EARTH / (vep.spherical.r * deflectorToEarth.spherical.r)
    DOT_DEFLECTOR = DOT_DEFLECTOR / (deflectorToEarth.spherical.r * deflectorToPlanet.spherical.r)

    // IF GRAVITATING BODY IS OBSERVED OBJECT, OR IS ON A STRAIGHT LINE
    // TOWARD OR AWAY FROM OBSERVED OBJECT TO WITHIN 1 ARCSEC,
    // DEFLECTION IS SET TO ZERO
    if (abs(DOT_DEFLECTOR) > 0.99999999999) return RectangularVector(vep)

    // COMPUTE SCALAR FACTORS
    val FAC1 = SUN_GRAVITATIONAL_CONSTANT * 2.0 /
            (SPEED_OF_LIGHT.pow(2.0) * AU * 1000.0 * deflectorToEarth.spherical.r * relativeMassRatio);
    val FAC2 = 1.0 + DOT_DEFLECTOR

    // CONSTRUCT CORRECTED POSITION VECTOR
    val result = doubleArrayOf(0.0, 0.0 ,0.0)

    for (i in 0..2) {
        val v: Double = vep[i] / vep.spherical.r +
                FAC1 * (DOT_PLANET * deflectorToEarth.rectangular[i] / deflectorToEarth.spherical.r -
                DOT_EARTH * deflectorToPlanet.rectangular[i] / deflectorToPlanet.spherical.r) / FAC2
        result[i] = v * vep.spherical.r
    }

    return RectangularVector(result)

}