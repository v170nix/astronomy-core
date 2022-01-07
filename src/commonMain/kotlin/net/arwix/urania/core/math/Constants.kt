@file:Suppress("unused")

package net.arwix.urania.core.math

import kotlin.math.PI

const val ARCSEC_2RAD = 2 * PI / (360.0 * 3600.0)

/** Julian century conversion constant = 100 * days per year.  */
const val JULIAN_DAYS_PER_CENTURY = 36525.0
/** The Julian Day which represents noon on 2000-01-01.  */
const val JD_2000 = 2451545.0
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