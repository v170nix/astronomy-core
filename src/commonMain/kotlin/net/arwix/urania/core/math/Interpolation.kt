package net.arwix.urania.core.math

class Interpolation(
    private val xArray: DoubleArray,
    private val yArray: DoubleArray,
    private val allowExtrapolation: Boolean
) {

    fun getLinearValue(x: Double): Double {
        var xPrev = x
        var yPrev = 0.0
        var xNext = x
        var yNext = 0.0

        var iprev = 0
        var inext = 0
        for (i in xArray.indices) {
            if (xArray[i] == x) return yArray[i]
            if (xArray[i] < x && (xArray[i] > xPrev || xPrev == x)) {
                xPrev = xArray[i]
                yPrev = yArray[i]
                iprev = i
            }
            if (xArray[i] > x && (xArray[i] < xNext || xNext == x)) {
                xNext = xArray[i]
                yNext = yArray[i]
                inext = i
            }
        }

        // Correct values if no previous or next point exist, if extrapolation is allowed
        if (allowExtrapolation) {
            if (xPrev == x && inext < xArray.size - 1) {
                xPrev = xArray[inext + 1]
                yPrev = yArray[inext + 1]
            }
            if (xNext == x && iprev > 0) {
                xNext = xArray[iprev - 1]
                yNext = yArray[iprev - 1]
            }
        } else {
            if (xPrev == x || xNext == x) throw IndexOutOfBoundsException()
        }

        // Interpolate
        var slope = 0.0
        if (xNext != xPrev) slope = (yNext - yPrev) / (xNext - xPrev)

        return yPrev + slope * (x - xPrev)
    }

}