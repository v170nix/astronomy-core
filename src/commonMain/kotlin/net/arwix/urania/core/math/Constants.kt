@file:Suppress("unused")

package net.arwix.urania.core.math

import kotlin.math.PI

const val ARCSEC_2RAD = 2 * PI / (360.0 * 3600.0)

/** Julian century conversion constant = 100 * days per year.  */
const val JULIAN_DAYS_PER_CENTURY = 36525.0
/** The Julian Day which represents noon on 2000-01-01.  */
const val JD_2000 = 2451545.0
const val DELTA_JD_MJD = 2400000.5
/** Length of a tropical year in days for B1950.  */
const val TROPICAL_YEAR = 365.242198781
/** Arc minutes in one degree = 60.  */
const val MINUTES_PER_DEGREE = 60.0
/** Arc seconds in one degree = 3600.  */
const val SECONDS_PER_DEGREE = 60.0 * MINUTES_PER_DEGREE
/** Arc seconds to radians.  */
const val ARCSEC_TO_RAD = PI / (180.0 * 3600.0)

const val SECONDS_PER_DAY = 86400.0

/** Radians to arc seconds.  */
const val RAD_TO_ARCSEC = 1.0 / ARCSEC_TO_RAD
/** Arc seconds to degrees.  */
const val ARCSEC_TO_DEG = 1.0 / 3600.0
/** Radians to hours.  */
const val RAD_TO_HOUR = 180.0 / (15.0 * PI)
/** Radians to days.  */
const val RAD_TO_DAY = RAD_TO_HOUR / 24.0
/** Radians to degrees.  */
const val RAD_TO_DEG = 180.0 / PI
/** Degrees to radians.  */
const val DEG_TO_RAD = 1.0 / RAD_TO_DEG

/** Pi divided by two.  */
const val PI_OVER_TWO = PI / 2.0
/** Pi divided by four.  */
const val PI_OVER_FOUR = PI / 4.0
/** Pi divided by six.  */
const val PI_OVER_SIX = PI / 6.0

/** Speed of light in m/s, exact as it is defined. */
const val SPEED_OF_LIGHT = 299792458.0
/** 1 au in km */
const val AU = 149597870.7

/** Light time in days for 1 AU.  */
const val LIGHT_TIME_DAYS_PER_AU: Double = AU * 1000.0 / (SPEED_OF_LIGHT * SECONDS_PER_DAY)

/** Length of a sidereal day in days according to IERS Conventions.  */
const val SIDEREAL_DAY_LENGTH = 1.0027378119113546

/** Heliocentric gravitational constant, in m^3/s^2 (Pitjeva 2015). */
const val SUN_GRAVITATIONAL_CONSTANT = 1.32712440042e20

const val SUN_RADIUS_IN_ARCS = 959.644 // in arc-seconds at 1ae
const val SUN_RADIUS_IN_METERS = 695508000.0