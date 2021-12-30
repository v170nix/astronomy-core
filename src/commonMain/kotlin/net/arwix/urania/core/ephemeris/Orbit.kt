package net.arwix.urania.core.ephemeris

import kotlin.js.JsExport

@JsExport
sealed class Orbit {
    object Geocentric: Orbit()
    object Heliocentric: Orbit()
}