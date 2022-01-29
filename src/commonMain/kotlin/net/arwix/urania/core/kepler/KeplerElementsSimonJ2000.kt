package net.arwix.urania.core.kepler

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.ARCSEC_TO_DEG
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.deg
import net.arwix.urania.core.math.angle.rad
import net.arwix.urania.core.math.polynomialSum
import net.arwix.urania.core.toRad

fun getKeplerElementsSimonJ2000(id: KeplerElementsObject): KeplerElements = when (id) {
    KeplerElementsObject.Mercury -> MercurySimonJ2000
    KeplerElementsObject.Venus -> VenusSimonJ2000
    KeplerElementsObject.Earth -> EarthSimonJ2000
    KeplerElementsObject.Mars -> MarsSimonJ2000
    KeplerElementsObject.Jupiter -> JupiterSimonJ2000
    KeplerElementsObject.Saturn -> SaturnSimonJ2000
    KeplerElementsObject.Uranus -> UranusSimonJ2000
    KeplerElementsObject.Neptune -> NeptuneSimonJ2000
    else -> throw IndexOutOfBoundsException()
}

private interface KeplerElementsData {
    val a: DoubleArray
    val e: DoubleArray
    val i: DoubleArray
    val l: DoubleArray
    val w: DoubleArray
    val o: DoubleArray
}

private class KeplerElementsSimonJ2000(override val a: DoubleArray,
                                       override val e: DoubleArray,
                                       override val i: DoubleArray,
                                       override val l: DoubleArray,
                                       override val w: DoubleArray,
                                       override val o: DoubleArray) : KeplerElementsData, KeplerElements {

    override fun getSemiMajorAxis(jT: JT): Double = a.polynomialSum(jT / 10.0)
    override fun getEccentricity(jT: JT): Radian = e.polynomialSum(jT / 10.0).rad
    override fun getInclination(jT: JT): Radian = i.polynomialSum(jT / 10.0).deg.toRad()
    override fun getLongitude(jT: JT): Radian = l.polynomialSum(jT / 10.0).deg.toRad()
    override fun getPerihelionLongitude(jT: JT): Radian = w.polynomialSum(jT / 10.0).deg.toRad()
    override fun getAscendingNodeLongitude(jT: JT): Radian = o.polynomialSum(jT / 10.0).deg.toRad()
}

private val MercurySimonJ2000 by lazy {
    KeplerElementsSimonJ2000(
        a = doubleArrayOf(0.3870983098),
        l = doubleArrayOf(252.25090552 - 0.047 * ARCSEC_TO_DEG, 5381016286.88982 * ARCSEC_TO_DEG, -1.92789 * ARCSEC_TO_DEG, 0.00639 * ARCSEC_TO_DEG),
        e = doubleArrayOf(0.2056317526, 0.0002040653, -28349e-10, -1805e-10, 23e-10, -2e-10),
        w = doubleArrayOf(77.45611904, 5719.1159 * ARCSEC_TO_DEG, -4.83016 * ARCSEC_TO_DEG, -0.02464 * ARCSEC_TO_DEG, -0.00016 * ARCSEC_TO_DEG, 0.00004 * ARCSEC_TO_DEG),
        i = doubleArrayOf(7.00498625, -214.25629 * ARCSEC_TO_DEG, 0.28977 * ARCSEC_TO_DEG, 0.15421 * ARCSEC_TO_DEG, -0.00169 * ARCSEC_TO_DEG, -0.00002 * ARCSEC_TO_DEG),
        o = doubleArrayOf(48.33089304, -4515.21727 * ARCSEC_TO_DEG, -31.79892 * ARCSEC_TO_DEG, -0.71933 * ARCSEC_TO_DEG, 0.01242 * ARCSEC_TO_DEG))
}

private val VenusSimonJ2000 by lazy {
    KeplerElementsSimonJ2000(
        a = doubleArrayOf(0.7233298200),
        l = doubleArrayOf(181.97980085, 2106641364.33548 * ARCSEC_TO_DEG, 0.59381 * ARCSEC_TO_DEG, -0.00627 * ARCSEC_TO_DEG),
        e = doubleArrayOf(0.0067719164, -0.0004776521, 98127e-10, 4639e-10, 123e-10, -3e-10),
        w = doubleArrayOf(131.56370300, 175.48640 * ARCSEC_TO_DEG, -498.48184 * ARCSEC_TO_DEG, -20.50042 * ARCSEC_TO_DEG, -0.72432 * ARCSEC_TO_DEG, 0.00224 * ARCSEC_TO_DEG),
        i = doubleArrayOf(3.39466189, -30.84437 * ARCSEC_TO_DEG, -11.67836 * ARCSEC_TO_DEG, 0.03338 * ARCSEC_TO_DEG, 0.00269 * ARCSEC_TO_DEG, 0.00004 * ARCSEC_TO_DEG),
        o = doubleArrayOf(76.67992019, -10008.48154 * ARCSEC_TO_DEG, -51.32614 * ARCSEC_TO_DEG, -0.5891 * ARCSEC_TO_DEG, -0.004665 * ARCSEC_TO_DEG))
}

private val EarthSimonJ2000 by lazy {
    KeplerElementsSimonJ2000(
        a = doubleArrayOf(1.0000010178),
        l = doubleArrayOf(100.46645683, 1295977422.83429 * ARCSEC_TO_DEG, -2.04411 * ARCSEC_TO_DEG, -0.00523 * ARCSEC_TO_DEG),
        e = doubleArrayOf(0.0167086342, -0.0004203654, -0.0000126734, 1444e-10, -2e-10, 3e-10),
        w = doubleArrayOf(102.93734808, 11612.35290 * ARCSEC_TO_DEG, 53.27577 * ARCSEC_TO_DEG, -0.14095 * ARCSEC_TO_DEG, 0.11440 * ARCSEC_TO_DEG, 0.00478 * ARCSEC_TO_DEG),
        i = doubleArrayOf(0.0, 469.97289 * ARCSEC_TO_DEG, -3.35053 * ARCSEC_TO_DEG, (-0.12374) * ARCSEC_TO_DEG, 0.00027 * ARCSEC_TO_DEG, -0.00001 * ARCSEC_TO_DEG, 0.00001 * ARCSEC_TO_DEG),
        o = doubleArrayOf(174.87317577, -8679.27034 * ARCSEC_TO_DEG, 15.34191 * ARCSEC_TO_DEG, 0.00532 * ARCSEC_TO_DEG, -0.03734 * ARCSEC_TO_DEG, -0.00073 * ARCSEC_TO_DEG, 0.00004 * ARCSEC_TO_DEG))
}

private val MarsSimonJ2000 by lazy {
    KeplerElementsSimonJ2000(
        a = doubleArrayOf(1.5236793419, 3e-10),
        l = doubleArrayOf(355.43299958, 689050774.93988 * ARCSEC_TO_DEG, 0.94264 * ARCSEC_TO_DEG, -0.01043 * ARCSEC_TO_DEG),
        e = doubleArrayOf(0.0934006477, 0.0009048438, -80641e-10, -2519e-10, 124e-10, -10e-10),
        w = doubleArrayOf(336.06023395, 15980.45908 * ARCSEC_TO_DEG, -62.32800 * ARCSEC_TO_DEG, 1.86464 * ARCSEC_TO_DEG, -0.04603 * ARCSEC_TO_DEG, -0.00164 * ARCSEC_TO_DEG),
        i = doubleArrayOf(1.84972648, -293.31722 * ARCSEC_TO_DEG, -8.11830 * ARCSEC_TO_DEG, -0.10326 * ARCSEC_TO_DEG, -0.00153 * ARCSEC_TO_DEG, 0.00048 * ARCSEC_TO_DEG),
        o = doubleArrayOf(49.55809321, -10620.90088 * ARCSEC_TO_DEG, -230.57416 * ARCSEC_TO_DEG, -7.06942 * ARCSEC_TO_DEG, -0.6892 * ARCSEC_TO_DEG, -0.05829 * ARCSEC_TO_DEG))
}

private val JupiterSimonJ2000 by lazy {
    KeplerElementsSimonJ2000(
        a = doubleArrayOf(5.2026032092, 19132e-10, -39e-10, -60e-10, -10e-10, 1e-10),
        l = doubleArrayOf(34.35151874, 109256603.77991 * ARCSEC_TO_DEG, -30.60378 * ARCSEC_TO_DEG, 0.05706 * ARCSEC_TO_DEG, 0.04667 * ARCSEC_TO_DEG, 0.00591 * ARCSEC_TO_DEG, -0.00034 * ARCSEC_TO_DEG),
        e = doubleArrayOf(0.0484979255, 0.0016322542, -0.0000471366, -20063e-10 * ARCSEC_TO_DEG, 1018e-10 * ARCSEC_TO_DEG, -21e-10 * ARCSEC_TO_DEG, 1e10 * ARCSEC_TO_DEG),
        w = doubleArrayOf(14.33120687, 7758.75163 * ARCSEC_TO_DEG, 259.95938 * ARCSEC_TO_DEG, -16.14731 * ARCSEC_TO_DEG, 0.74704 * ARCSEC_TO_DEG, -0.02087 * ARCSEC_TO_DEG, -0.00016 * ARCSEC_TO_DEG),
        i = doubleArrayOf(1.30326698, -71.55890 * ARCSEC_TO_DEG, 11.95297 * ARCSEC_TO_DEG, 0.340909 * ARCSEC_TO_DEG, -0.02710 * ARCSEC_TO_DEG, -0.00124 * ARCSEC_TO_DEG, 0.00003 * ARCSEC_TO_DEG),
        o = doubleArrayOf(100.46440702, 6362.03561 * ARCSEC_TO_DEG, 326.52178 * ARCSEC_TO_DEG, -26.18091 * ARCSEC_TO_DEG, -2.10322 * ARCSEC_TO_DEG, 0.04453 * ARCSEC_TO_DEG, 0.01154 * ARCSEC_TO_DEG))
}

private val SaturnSimonJ2000 by lazy {
    KeplerElementsSimonJ2000(
        a = doubleArrayOf(9.5549091915, -0.0000213896, 444e-10, 670e-10, 110e-10, -7e-10, -1e-10),
        l = doubleArrayOf(50.07744430, 43996098.55732 * ARCSEC_TO_DEG, 75.61614 * ARCSEC_TO_DEG, -0.16618 * ARCSEC_TO_DEG, -0.11484 * ARCSEC_TO_DEG, -0.01452 * ARCSEC_TO_DEG, 0.00083 * ARCSEC_TO_DEG),
        e = doubleArrayOf(0.0555481426, -0.0034664062, -0.0000643639, 33956e-10, -219e-10, -3e-10, 6e-10),
        w = doubleArrayOf(93.05723748, 20395.49439 * ARCSEC_TO_DEG, 190.25952 * ARCSEC_TO_DEG, 17.68303 * ARCSEC_TO_DEG, 1.23148 * ARCSEC_TO_DEG, 0.10310 * ARCSEC_TO_DEG, 0.00702 * ARCSEC_TO_DEG),
        i = doubleArrayOf(2.48887878, 91.85195 * ARCSEC_TO_DEG, -17.66225 * ARCSEC_TO_DEG, 0.06105 * ARCSEC_TO_DEG, 0.02638 * ARCSEC_TO_DEG, -0.00152 * ARCSEC_TO_DEG, -0.00012 * ARCSEC_TO_DEG),
        o = doubleArrayOf(113.66550252, -9240.19942 * ARCSEC_TO_DEG, -66.23743 * ARCSEC_TO_DEG, 1.72778 * ARCSEC_TO_DEG, 0.2699 * ARCSEC_TO_DEG, 0.03610 * ARCSEC_TO_DEG, -0.00248 * ARCSEC_TO_DEG))
}

private val UranusSimonJ2000 by lazy {
    KeplerElementsSimonJ2000(
        a = doubleArrayOf(19.2184460618, -3716e-10, 979e-10),
        l = doubleArrayOf(314.05500511, 15424811.93933 * ARCSEC_TO_DEG, -1.75083 * ARCSEC_TO_DEG, 0.02156 * ARCSEC_TO_DEG),
        e = doubleArrayOf(0.0463812221, -0.0002729293, 0.0000078913, 2447e-10 * ARCSEC_TO_DEG, -171e-10 * ARCSEC_TO_DEG),
        w = doubleArrayOf(173.00529106, 3215.56238 * ARCSEC_TO_DEG, -34.09288 * ARCSEC_TO_DEG, 1.48909 * ARCSEC_TO_DEG, 0.066 * ARCSEC_TO_DEG),
        i = doubleArrayOf(0.77319689, -60.72723 * ARCSEC_TO_DEG, 1.25759 * ARCSEC_TO_DEG, 0.05808 * ARCSEC_TO_DEG, 0.00031 * ARCSEC_TO_DEG),
        o = doubleArrayOf(74.00595701, 2669.15033 * ARCSEC_TO_DEG, 145.93964 * ARCSEC_TO_DEG, 0.42917 * ARCSEC_TO_DEG, -0.0912 * ARCSEC_TO_DEG))
}

private val NeptuneSimonJ2000 by lazy {
    KeplerElementsSimonJ2000(
        a = doubleArrayOf(30.1103868694, -16635e-10, 686e-10),
        l = doubleArrayOf(304.34866548, 7865503.20744 * ARCSEC_TO_DEG, 0.21103 * ARCSEC_TO_DEG, -0.00895 * ARCSEC_TO_DEG),
        e = doubleArrayOf(0.0094557470, 0.0000603263, 0.0, -483e-10 * ARCSEC_TO_DEG),
        w = doubleArrayOf(48.12027554, 1050.71912 * ARCSEC_TO_DEG, 27.39717 * ARCSEC_TO_DEG),
        i = doubleArrayOf(1.76995259, 8.12333 * ARCSEC_TO_DEG, 0.08135 * ARCSEC_TO_DEG, -0.00046 * ARCSEC_TO_DEG),
        o = doubleArrayOf(131.78405702, -221.94322 * ARCSEC_TO_DEG, -0.78728 * ARCSEC_TO_DEG, -0.28070 * ARCSEC_TO_DEG, 0.00049 * ARCSEC_TO_DEG))
}