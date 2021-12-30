package net.arwix.urania.core.transformation.precession

import net.arwix.urania.core.Ecliptic
import net.arwix.urania.core.Equatorial
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.ephemeris.EphemerisVector
import net.arwix.urania.core.ephemeris.Epoch
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.vector.Matrix
import net.arwix.urania.core.math.vector.Vector
import kotlin.js.JsExport

interface PrecessionElements {

    val id: Precession
    val jT: JT
    val fromJ2000Matrix: Matrix
    val toJ2000Matrix: Matrix

    fun changeEpoch(vector: Vector, toEpoch: Epoch): Vector {
        return when (toEpoch) {
            Epoch.J2000 -> toJ2000Matrix * vector
            Epoch.Apparent -> fromJ2000Matrix * vector
        }
    }

    fun changeEpoch(ephemerisVector: EphemerisVector): EphemerisVector {
        if (ephemerisVector.metadata.plane != this.id.plane) throw IllegalStateException()
        val toEpoch = when (ephemerisVector.metadata.epoch) {
            Epoch.Apparent -> Epoch.J2000
            Epoch.J2000 -> Epoch.Apparent
        }
        return ephemerisVector.copy(
            value = changeEpoch(ephemerisVector.value, toEpoch),
            metadata = ephemerisVector.metadata.copy(epoch = toEpoch)
        )
    }

    fun Vector.changeEpoch(toEpoch: Epoch): Vector {
        return changeEpoch(this, toEpoch)
    }

    fun EphemerisVector.changeEpoch(toEpoch: Epoch): EphemerisVector {
        return changeEpoch(this)
    }

}

@JsExport
sealed class Precession(val plane: Plane) {

    /**
     * Precession for selecting IAU 1976 formulae of precession, nutation (IAU
     * 1980), and Greenwich mean sidereal time. This will use
     * old formulae that will match results from IMCCE ephemeris server. You
     * may consider using this formulae for VSOP82 theory. See
     * J. H. Lieske, T. Lederle, W. Fricke, and B. Morando, "Expressions for the
     * Precession Quantities Based upon the IAU (1976) System of Astronomical
     * Constants," Astronomy and Astrophysics 58, 1-16 (1977).
     */
    @Ecliptic
    object IAU1976 : Precession(Plane.Ecliptic)

    /**
     * Precession for selecting Laskar formulae of precession, nutation (IAU
     * 1980), and Greenwich mean sidereal time. See J. Laskar,
     * "Secular terms of classical planetary theories using the results of
     * general theory," Astronomy and Astrophysics 157, 59070 (1986).
     */
    @Ecliptic
    object Laskar1986 : Precession(Plane.Ecliptic)

    /**
     * Precession for selecting Williams formulae of precession (DE403 JPL
     * Ephemeris), nutation (IAU 1980), obliquity, and Greenwich mean sidereal
     * time. See James G. Williams, "Contributions to the Earth's obliquity rate,
     * precession, and nutation," Astron. J. 108, 711-724 (1994). It is convenient
     * to use this when obtaining ephemeris of the Moon using Moshier method.
     */
    @Ecliptic
    object Williams1994 : Precession(Plane.Ecliptic)

    /**
     * Precession for selecting SIMON formulae of precession, obliquity,
     * nutation (IAU 1980), and Greenwich mean sidereal time. See
     * J. L. Simon, P. Bretagnon, J. Chapront, M. Chapront-Touze', G. Francou,
     * and J. Laskar, "Numerical Expressions for precession formulae and mean
     * elements for the Moon and the planets," Astronomy and Astrophysics 282,
     * 663-683 (1994).
     */
    @Ecliptic
    object Simon1994 : Precession(Plane.Ecliptic)

    /**
     * Precession for selecting JPL DE403/404/405/406 formulae for precession,
     * obliquity, nutation (IAU 1980), and Greenwich mean sidereal time. Quite
     * similar to Williams formulae. Adequate for planets using Moshier
     * method, Series96, or JPL DE40x ephemerides.
     */
    @Ecliptic
    object DE4xxx : Precession(Plane.Ecliptic)

    /**
     * Precession following IAU2000 definitions. From SOFA software library.
     * Reference: Capitaine et al., Astronomy & Astrophysics 400, 1145-1154,
     * 2003. See also Lieske et al. 1977.
     */
    @Equatorial
    object IAU2000 : Precession(Plane.Equatorial)

    /**
     * Precession following Capitaine et al. 2003.
     *
     * Capitaine formula of precession is to be officially adopted by the IAU,
     * see recommendation in the report of the IAU Division I Working Group on
     * Precession and the Ecliptic (Hilton et al. 2006, Celest. Mech., 94,
     * 351-367).
     * Reference: Capitaine et al., Astronomy & Astrophysics 412, 567-586,
     * 2003.
     */
    @Equatorial
    object IAU2006 : Precession(Plane.Equatorial)

    /**
     * Same as IAU2006, but planetary rotation models are those recommended by
     * the IAU working group on carthographic coordinates, in 2009.
     */
    @Equatorial
    object IAU2009 : Precession(Plane.Equatorial)

    /**
     * Precession following Vondrak et al. 2011. See A&amp;A 534, A22.
     */
    @Equatorial
    object Vondrak2011 : Precession(Plane.Equatorial)
}