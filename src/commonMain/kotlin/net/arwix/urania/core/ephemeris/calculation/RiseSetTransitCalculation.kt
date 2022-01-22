@file:Suppress("unused")

package net.arwix.urania.core.ephemeris.calculation

import kotlinx.datetime.Instant
import net.arwix.urania.core.calendar.*
import net.arwix.urania.core.ephemeris.Ephemeris
import net.arwix.urania.core.ephemeris.Epoch
import net.arwix.urania.core.ephemeris.Orbit
import net.arwix.urania.core.ephemeris.Plane
import net.arwix.urania.core.math.RAD_TO_DAY
import net.arwix.urania.core.math.SECONDS_PER_DAY
import net.arwix.urania.core.math.SIDEREAL_DAY_LENGTH
import net.arwix.urania.core.math.angle.*
import net.arwix.urania.core.observer.Observer
import net.arwix.urania.core.spherical
import net.arwix.urania.core.toDeg
import net.arwix.urania.core.toRad
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin

class RiseSetTransitCalculation {

    sealed class Request(open val isStar: Boolean) {
        sealed class RiseSet(open val elevation: Radian, isStar: Boolean = false) : Request(isStar) {
            class TwilightAstronomical(isStar: Boolean = false) : RiseSet(-18.deg.toRad(), isStar)
            class TwilightNautical(isStar: Boolean = false) : RiseSet(-12.deg.toRad(), isStar)
            class TwilightCivil(isStar: Boolean = false) : RiseSet(-6.0.deg.toRad(), isStar)
            data class HorizonAstronomical(
                val angularRadius: Radian,
                val horizonDepression: Radian,
                val motherBodyIsEarth: Boolean = true,
                override val isStar: Boolean = false,
            ) : RiseSet(
                -angularRadius - horizonDepression +
                        if (motherBodyIsEarth) -(32.67 / 60.0).deg.toRad() else Radian.Zero,
                isStar
            )

            data class HorizonAstronomical34arcMin(
                val angularRadius: Radian,
                val motherBodyIsEarth: Boolean = true,
                override val isStar: Boolean = false,
            ) : RiseSet(
                -angularRadius +
                        if (motherBodyIsEarth) -(34.67 / 60.0).deg.toRad() else Radian.Zero,
                isStar
            )

            data class Custom(
                override val elevation: Radian = Radian.Zero,
                val angularRadius: Radian = Radian.Zero,
                override val isStar: Boolean = false,
            ) : RiseSet(elevation - angularRadius, isStar)

        }

        class UpperTransit(isStar: Boolean = false) : Request(isStar)
        class DownTransit(isStar: Boolean = false) : Request(isStar)
    }

    sealed class Result(open val request: Request) {
        data class UpperTransit(override val request: Request, val time: Instant, val altitude: Degree) :
            Result(request)

        data class DownTransit(override val request: Request, val time: Instant, val altitude: Degree) : Result(request)
        sealed class Rise(request: Request) : Result(request) {
            data class Value(override val request: Request, val time: Instant) : Rise(request)
            data class AlwaysBelowHorizon(override val request: Request) : Rise(request)
            data class Circumpolar(override val request: Request) : Set(request)
        }

        sealed class Set(request: Request) : Result(request) {
            data class Value(override val request: Request, val time: Instant) : Set(request)
            data class AlwaysBelowHorizon(override val request: Request) : Set(request)
            data class Circumpolar(override val request: Request) : Set(request)
        }

        data class Error(override val request: Request) : Result(request)
    }

    private sealed class InnerResult {
        data class UpperTransit(val mjd: MJD, val geometricElevation: Radian) : InnerResult()
        data class DownTransit(val mjd: MJD, val geometricElevation: Radian) : InnerResult()
        sealed class Rise : InnerResult() {
            data class Value(val mjd: MJD) : Rise()
            object AlwaysBelowHorizon : Rise()
            object Circumpolar : Rise()
        }

        sealed class Set : InnerResult() {
            data class Value(val mjd: MJD) : Set()
            object AlwaysBelowHorizon : Set()
            object Circumpolar : Set()
        }

        object None : InnerResult()
    }

    private sealed class InnerRequest {
        object UpperTransit : InnerRequest()
        object DownTransit : InnerRequest()
        data class Rise(val e: Radian) : InnerRequest()
        data class Set(val e: Radian) : InnerRequest()

        fun getElevation() = when (this) {
            is Rise -> e
            is Set -> e
            UpperTransit -> null
            DownTransit -> null
        }
    }

    private suspend fun nextRiseSetTransit(
        time: MJD,
        position: Observer.Position,
        eph: Ephemeris,
        innerRequest: InnerRequest,
    ): InnerResult {

        val body = eph.invoke(time.toJT()).spherical
        val siderealTime = time.getLocalApparentSiderealTime(SiderealTimeMethod.Williams1994, position)

        //TODO if (obs != EARTH && obs != NOT_A_PLANET)

        if (innerRequest == InnerRequest.UpperTransit) {

            val dTransitTime = celestialHoursToEarthTime * (body.phi - siderealTime).normalize()

            val transitAltitude =
                asin(sin(body.theta) * sin(position.latitude) + cos(body.theta) * cos(position.latitude)).rad

            return InnerResult.UpperTransit(time + dTransitTime.mJD, transitAltitude)
        }

        if (innerRequest == InnerRequest.DownTransit) {

            val dTransitTime = celestialHoursToEarthTime * ((body.phi - siderealTime).normalize() - PI.rad)

            val transitAltitude =
                asin(sin(body.theta) * sin(position.latitude) + cos(body.theta) * cos(position.latitude)).rad

            return InnerResult.DownTransit(time + dTransitTime.mJD, transitAltitude)
        }

        val elevation = innerRequest.getElevation()!!

        val cAngle = (sin(elevation) - sin(position.latitude) * sin(body.theta)) /
                (cos(position.latitude) * cos(body.theta))

        return when (innerRequest) {
            is InnerRequest.Rise -> {
                if (cAngle > 1.0) {
                    InnerResult.Rise.AlwaysBelowHorizon
                } else if (cAngle < -1.0) {
                    InnerResult.Rise.Circumpolar
                } else {
                    val hour = acos(cAngle).rad
                    val riseTime = celestialHoursToEarthTime * (body.phi - hour - siderealTime).normalize()
                    InnerResult.Rise.Value(time + riseTime.mJD)
                }
            }
            is InnerRequest.Set -> {
                if (cAngle > 1.0) {
                    InnerResult.Set.AlwaysBelowHorizon
                } else if (cAngle < -1.0) {
                    InnerResult.Set.Circumpolar
                } else {
                    val hour = acos(cAngle).rad
                    val setTime = celestialHoursToEarthTime * (body.phi + hour - siderealTime).normalize()
                    InnerResult.Set.Value(time + setTime.mJD)
                }
            }
            else -> throw IllegalStateException()
        }

    }

    suspend fun obtainNextResults(
        time: MJD,
        observer: Observer,
        ephemeris: Ephemeris,
        request: Set<Request>,
    ): Set<Result> {
        if (ephemeris.metadata.orbit != Orbit.Geocentric ||
            ephemeris.metadata.epoch != Epoch.Apparent ||
            ephemeris.metadata.plane != Plane.Equatorial
        ) throw IllegalArgumentException()
        // Obtain event to better than 0.5 seconds of precision
        return request.flatMap {
            when (it) {
                is Request.RiseSet -> {
                    listOf(it to InnerRequest.Rise(it.elevation), it to InnerRequest.Set(it.elevation))
                }
                is Request.UpperTransit -> {
                    listOf(it to InnerRequest.UpperTransit)
                }
                is Request.DownTransit -> {
                    listOf(it to InnerRequest.DownTransit)
                }
            }
        }.map { (request, event) ->
            request to obtainNextRiseEvents(
                time,
                observer.position,
                ephemeris,
                event
            )
        }.map { (request, computed) ->
            when (computed) {
                InnerResult.None -> Result.Error(request)
                InnerResult.Rise.AlwaysBelowHorizon -> Result.Rise.AlwaysBelowHorizon(request)
                InnerResult.Rise.Circumpolar -> Result.Rise.Circumpolar(request)
                is InnerResult.Rise.Value -> Result.Rise.Value(request, computed.mjd.toInstant())
                InnerResult.Set.AlwaysBelowHorizon -> Result.Set.AlwaysBelowHorizon(request)
                InnerResult.Set.Circumpolar -> Result.Set.Circumpolar(request)
                is InnerResult.Set.Value -> Result.Set.Value(request, computed.mjd.toInstant())
                is InnerResult.UpperTransit -> Result.UpperTransit(
                    request,
                    computed.mjd.toInstant(),
                    computed.geometricElevation.toDeg()
                )
                is InnerResult.DownTransit -> Result.DownTransit(
                    request,
                    computed.mjd.toInstant(),
                    computed.geometricElevation.toDeg()
                )
            }
        }.toSet()
    }

    private suspend fun obtainNextRiseEvents(
        time: MJD,
        position: Observer.Position,
        ephemeris: Ephemeris,
        innerRequest: InnerRequest,
        isStar: Boolean = false
    ): InnerResult {
        val notYetCalculated = -1.0
        val precisionInSeconds = 0.5
        var n = 0
        val nMax = 20
        var lastTimeEvent = notYetCalculated
        var dt = notYetCalculated
        var timeEvent: Double = time.value
        var result: InnerResult
        var triedBefore = false
        do {
            n++
            result = nextRiseSetTransit(timeEvent.mJD, position, ephemeris, innerRequest)
            val resultTime = when (result) {
                is InnerResult.Rise.Value -> result.mjd
                is InnerResult.Set.Value -> result.mjd
                is InnerResult.UpperTransit -> result.mjd
                is InnerResult.DownTransit -> result.mjd
                else -> null
            }

            if (resultTime != null) {
                timeEvent = resultTime.value
                dt = timeEvent - lastTimeEvent
            } else {
                @Suppress("NON_EXHAUSTIVE_WHEN_STATEMENT")
                when (result) {
                    InnerResult.Rise.AlwaysBelowHorizon,
                    InnerResult.Rise.Circumpolar,
                    InnerResult.Set.AlwaysBelowHorizon,
                    InnerResult.Set.Circumpolar -> {
                        if (!isStar) {
                            if (triedBefore) {
                                break
                            } else {
                                triedBefore = true
                                val c = nextRiseSetTransit(
                                    time,
                                    position,
                                    ephemeris,
                                    InnerRequest.UpperTransit
                                ) as InnerResult.UpperTransit
                                timeEvent = c.mjd.value
                                dt = 2.0 * precisionInSeconds / SECONDS_PER_DAY
                            }
                        } else break
                    }
                }
            }
            lastTimeEvent = timeEvent
        } while (abs(dt) > precisionInSeconds / SECONDS_PER_DAY && n < nMax)

        return if (n == nMax) {
            InnerResult.None
        } else {
            result
        }
    }

    private companion object {
        private const val celestialHoursToEarthTime: Double = RAD_TO_DAY / SIDEREAL_DAY_LENGTH
    }


}