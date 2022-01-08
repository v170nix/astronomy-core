package net.arwix.urania.core.ephemeris

import kotlin.js.JsExport

@JsExport
sealed class Plane {
    object Ecliptic: Plane()
    object Equatorial: Plane()
    object Topocentric: Plane()
}