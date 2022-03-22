package net.arwix.urania.core.physic

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.physic.impl.*


// https://github.com/v170nix/astronomy-java-lib/blob/101c17e62da127a9ee8d5adc5c30e7a4d0737193/src/main/java/net/arwix/astronomy/physic/PhysicOrientation.java
// https://bitbucket.org/v170n1x/sunexplorer.org/src/72854a8bdbcb12741971a24279ea6a560a172bfa/mobile/src/main/kotlin/org/sunexplorer/old/GraticulePath.kt?at=master#GraticulePath.kt-4
// https://web.archive.org/web/20200512151452/http://www.hnsky.org/iau-iag.htm


object Physic {

    sealed class Model {

        enum class SystemType { I, II, III }

        object IAU2000: Model()
        object IAU2006: Model()
        object IAU2009: Model()
        object IAU2015: Model()
        // https://arxiv.org/pdf/1808.01973.pdf
        object IAU2018: Model()
    }

    interface Elements {
        fun Model.getNorthPole(jT: JT): PhysicEphemeris.NorthPole
        fun Model.getMagnitude(distance: Double, distanceFromSun: Double, phaseAngle: Degree): Double

        /**
         * @return rotation in degrees/day
         */
        fun Model.getSpeedRotation(): Double
        fun Model.getLongitudeJ2000(jT: JT): Degree
    }

    sealed class Body(val ellipsoid: Ellipsoid) : Elements {
        object Sun : Body(EllipsoidObject.Sun), Elements by PhysicModelSunImpl
        object Mercury : Body(EllipsoidObject.Mercury), Elements by PhysicModelMercuryImpl
        object Venus : Body(EllipsoidObject.Venus), Elements by PhysicModelVenusImpl
        object Earth : Body(EllipsoidObject.Earth), Elements by PhysicModelEarthImpl
        object Mars : Body(EllipsoidObject.Mars), Elements by PhysicModelMarsImpl
        object Jupiter : Body(EllipsoidObject.Jupiter), Elements by PhysicModelJupiterImpl
        object Saturn : Body(EllipsoidObject.Saturn), Elements by PhysicModelSaturnImpl
        object Uranus : Body(EllipsoidObject.Uranus), Elements by PhysicModelUranusImpl
        object Neptune : Body(EllipsoidObject.Neptune), Elements by PhysicModelNeptuneImpl
        object Pluto : Body(EllipsoidObject.Pluto), Elements by PhysicModelPlutoImpl

        object Moon : Body(EllipsoidObject.Moon), Elements by PhysicModelMoonImpl
//
//        object Ida : Body(EllipsoidObject.None)
//        object Ceres : Body(EllipsoidObject.None)
//        object Pallas : Body(EllipsoidObject.None)
//        object Lutetia : Body(EllipsoidObject.None)
//        object Davida : Body(EllipsoidObject.None)
//        object Gaspra : Body(EllipsoidObject.None)
//        object Vesta : Body(EllipsoidObject.None)
//        object Eros : Body(EllipsoidObject.None)
//        object Steins : Body(EllipsoidObject.None)
//        object Itokawa : Body(EllipsoidObject.None)
//        object P9Tempel1 : Body(EllipsoidObject.None)
//        object P19Borrelly : Body(EllipsoidObject.None)
    }

}

//private fun createIAU2015Elements(body: Physic.Body, jT: JT = JT.None): PhysicRotationElements {
//    return when (body) {
//        is Physic.Body.Mars -> object : PhysicRotationElements {
//            override val northPole: NorthPole by lazy {
//                NorthPole(
//                    rightAscension =
//                    (317.269202 - 0.10927547 * jT +
//                            0.000068 * sin((198.991226 + 19139.4819985 * jT).deg.toRad()) +
//                            0.000238 * sin((226.292679 + 38280.8511281 * jT).deg.toRad()) +
//                            0.000052 * sin((249.663391 + 57420.7251593 * jT).deg.toRad()) +
//                            0.000009 * sin((266.183510 + 76560.6367950 * jT).deg.toRad()) +
//                            0.419057 * sin((79.398797 + 0.5042615 * jT).deg.toRad())).deg.toRad(),
//                    declination =
//                    (54.432516 - 0.05827105 * jT +
//                            0.000051 * cos((122.433576 + 19139.9407476 * jT).deg.toRad()) +
//                            0.000141 * cos((43.058401 + 38280.8753272 * jT).deg.toRad()) +
//                            0.000031 * cos((57.663379 + 57420.7517205 * jT).deg.toRad()) +
//                            0.000005 * cos((79.476401 + 76560.6495004 * jT).deg.toRad()) +
//                            1.591274 * cos((166.325722 + 0.5042615 * jT).deg.toRad())).deg.toRad()
//                )
//            }
//            override val longitudeJ2000: Degree by lazy {
//                (176.049863 +
//                        0.000145 * sin((129.071773 + 19140.0328244 * jT).deg.toRad()) +
//                        0.000157 * sin((36.352167 + 38281.0473591 * jT).deg.toRad()) +
//                        0.000040 * sin((56.668646 + 57420.9295360 * jT).deg.toRad()) +
//                        0.000001 * sin((67.364003 + 76560.2552215 * jT).deg.toRad()) +
//                        0.000001 * sin((104.792680 + 95700.4387578 * jT).deg.toRad()) +
//                        0.584542 * sin((95.391654 + 0.5042615 * jT).deg.toRad())).deg
//            }
//            override val speedRotation: Double = 350.891982443297
//
//        }
//        else -> TODO()
//    }
//}

//private fun createIAU2000Elements(body: Physic.Body, jT: JT = JT.None): PhysicRotationElements {
//
//    return when (body) {
//        is Physic.Body.Sun -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole(286.13.deg.toRad(), 63.87.deg.toRad()) }
//            override val longitudeJ2000: Degree = 84.1.deg
//            override val speedRotation: Double = 14.1844
//        }
//        Physic.Body.Mercury -> object : PhysicRotationElements {
//            override val northPole by lazy {
//                NorthPole(
//                    (281.01 - 0.033 * jT).deg.toRad(),
//                    (61.45 - 0.005 * jT).deg.toRad()
//                )
//            }
//            override val longitudeJ2000: Degree = 329.548.deg
//            override val speedRotation: Double = 6.1385025
//        }
//        Physic.Body.Venus -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole(272.76.deg.toRad(), 67.16.deg.toRad()) }
//            override val longitudeJ2000: Degree = 160.20.deg
//            override val speedRotation: Double = -1.4813688
//        }
//        Physic.Body.Earth -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole((-0.641 * jT).deg.toRad(), (90.0 - 0.557 * jT).deg.toRad()) }
//            override val longitudeJ2000: Degree = 190.147.deg
//            override val speedRotation: Double = 360.9856235
//        }
//        Physic.Body.Mars -> object : PhysicRotationElements {
//            override val northPole by lazy {
//                NorthPole(
//                    (317.68143 - 0.1061 * jT).deg.toRad(),
//                    (52.8865 - 0.0609 * jT).deg.toRad()
//                )
//            }
//            override val longitudeJ2000: Degree = 176.630.deg // see IAU 2002
//            override val speedRotation: Double = 350.89198226
//        }
//        Physic.Body.Jupiter -> object : PhysicRotationElements {
//            override val northPole by lazy {
//                NorthPole(
//                    (268.05 - 0.009 * jT).deg.toRad(),
//                    (64.49 + 0.003 * jT).deg.toRad()
//                )
//            }
//            override val longitudeJ2000: Degree = 284.95.deg // for System III
//            override val speedRotation: Double = 870.536642
//        }
//        Physic.Body.Saturn -> object : PhysicRotationElements {
//            override val northPole by lazy {
//                NorthPole(
//                    (40.589 - 0.036 * jT).deg.toRad(),
//                    (83.537 - 0.004 * jT).deg.toRad()
//                )
//            }
//            override val longitudeJ2000: Degree = 38.9.deg // for System III
//            override val speedRotation: Double = 810.7939024
//        }
//        Physic.Body.Uranus -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole(257.311.deg.toRad(), -15.175.deg.toRad()) }
//            override val longitudeJ2000: Degree = 203.81.deg
//            override val speedRotation: Double = -501.1600928
//        }
//        Physic.Body.Neptune -> {
//            val a = (357.85 + 52.316 * jT).deg.toRad()
//            val sinA = sin(a)
//            object : PhysicRotationElements {
//                override val northPole by lazy {
//                    NorthPole(
//                        (299.36 + 0.70 * sinA).deg.toRad(),
//                        (43.46 - 0.51 * cos(a)).deg.toRad()
//                    )
//                }
//                override val longitudeJ2000: Degree = (253.18 - 0.48 * sinA).deg
//                override val speedRotation: Double = -501.1600928
//            }
//        }
//        Physic.Body.Pluto -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole(313.02.deg.toRad(), 9.09.deg.toRad()) }
//            override val longitudeJ2000: Degree = 236.77.deg
//            override val speedRotation: Double = -56.3623195
//        }
//        Physic.Body.Moon -> {
//            TODO()
//            // val data =  moonAxis(JD)
////            object : RotationElements {
////                override val northPoleRightAscension = data[0]
////                override val northPoleDeclination = data[1]
////                override val longitudeJ2000: Degree = data[2]
////                override val speedRotation: Double = data[3]
////            }
//        }
//        Physic.Body.Ida -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole(348.76.deg.toRad(), 87.12.deg.toRad()) }
//            override val longitudeJ2000: Degree = 265.95.deg
//            override val speedRotation: Double = -1864.628007
//        }
//        Physic.Body.Gaspra -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole(9.47.deg.toRad(), 26.7.deg.toRad()) }
//            override val longitudeJ2000: Degree = 83.67.deg
//            override val speedRotation: Double = 1226.9114850
//        }
//        Physic.Body.Vesta -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole(301.0.deg.toRad(), 41.0.deg.toRad()) }
//            override val longitudeJ2000: Degree = 292.0.deg
//            override val speedRotation: Double = 1617.332776
//        }
//        Physic.Body.Eros -> object : PhysicRotationElements {
//            override val northPole by lazy { NorthPole(11.35.deg.toRad(), 17.22.deg.toRad()) }
//            override val longitudeJ2000: Degree = 326.07.deg
//            override val speedRotation: Double = 1639.38864745
//        }
//        else -> throw IllegalArgumentException()
//    }
//
//}