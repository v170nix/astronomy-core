package net.arwix.urania.core.calculation

//import net.arwix.urania.core.ephemeris.calculation.EquinoxesSolsticesCalculation

//class EquinoxesSolsticesTest {
//
//    @Test
//    fun findEquinoxSolsticeTest() = runTest {
//
////        val request = EquinoxSolsticeRequest.SpringEquinox(2022)
//////        val getSunEphemeris = MoshierEphemerisFactory(MJD(2022, 3, 1).toJT()).createGeocentricEquatorialEphemeris(
//////            bodyEphemeris = MoshierSunEphemeris,
//////            epoch = Epoch.Apparent
//////        )
//
//        val result = EquinoxesSolsticesCalculation.findEvents(
//            2022,
//            { jT0 ->
//                MoshierEphemerisFactory(jT0)
//                    .createGeocentricEphemeris(
//                        bodyEphemeris = MoshierSunEphemeris,
//                        epoch = Epoch.Apparent,
//                        plane = Plane.Ecliptic
//                    )
//            }
//        )
//
//        assertEquals("2022-03-20T15:33:23", result[0].date.toString().substring(0..18))
//        assertEquals("2022-06-21T09:13:49", result[1].date.toString().substring(0..18))
//        assertEquals("2022-09-23T01:03:40", result[2].date.toString().substring(0..18))
//        assertEquals("2022-12-21T21:48:11", result[3].date.toString().substring(0..18))
//    }
//}