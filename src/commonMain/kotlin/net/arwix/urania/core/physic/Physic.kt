package net.arwix.urania.core.physic

import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.math.angle.Degree
import net.arwix.urania.core.physic.impl.*


// https://github.com/v170nix/astronomy-java-lib/blob/101c17e62da127a9ee8d5adc5c30e7a4d0737193/src/main/java/net/arwix/astronomy/physic/PhysicOrientation.java
// https://bitbucket.org/v170n1x/sunexplorer.org/src/72854a8bdbcb12741971a24279ea6a560a172bfa/mobile/src/main/kotlin/org/sunexplorer/old/GraticulePath.kt?at=master#GraticulePath.kt-4
// https://web.archive.org/web/20200512151452/http://www.hnsky.org/iau-iag.htm

object Physic {

    sealed class Model {
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