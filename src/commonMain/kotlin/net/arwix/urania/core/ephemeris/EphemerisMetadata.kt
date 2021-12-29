package net.arwix.urania.core.ephemeris

import net.arwix.urania.core.math.vector.Vector
import kotlin.js.JsExport

@JsExport
sealed class Orbit {
    object Geocentric: Orbit()
    object Heliocentric: Orbit()
}

@JsExport
sealed class Plane {
    object Ecliptic: Plane()
    object Equatorial: Plane()
    object Azimuthal: Plane()
}

@JsExport
sealed class Epoch {
    object J2000: Epoch()
    object Apparent: Epoch()
}

@JsExport
data class EphemerisMetadata(val orbit: Orbit, val plane: Plane, val epoch: Epoch)

data class EphemerisVector(val value: Vector, val metadata: EphemerisMetadata)
