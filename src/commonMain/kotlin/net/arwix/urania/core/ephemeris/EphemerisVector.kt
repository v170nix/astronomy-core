package net.arwix.urania.core.ephemeris

import net.arwix.urania.core.math.vector.Vector
import kotlin.js.JsExport

@JsExport
data class EphemerisVector(val value: Vector, val metadata: Metadata)