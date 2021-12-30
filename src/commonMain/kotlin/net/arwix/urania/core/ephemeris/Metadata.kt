package net.arwix.urania.core.ephemeris

import kotlin.js.JsExport

@JsExport
data class Metadata(val orbit: Orbit, val plane: Plane, val epoch: Epoch)

