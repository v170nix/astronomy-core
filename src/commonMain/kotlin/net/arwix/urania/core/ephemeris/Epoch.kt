package net.arwix.urania.core.ephemeris

import kotlin.js.JsExport

@JsExport
sealed class Epoch {
    object J2000: Epoch()
    object Apparent: Epoch()
}