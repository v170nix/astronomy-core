package net.arwix.urania.core.ephemeris

enum class Orbit { Geocentric, Heliocentric }
enum class Plane { Ecliptic, Equatorial, Azimuthal }
enum class Epoch { J2000, Apparent }


data class EphemerisMetadata(val orbit: Orbit, val plane: Plane, val epoch: Epoch)
