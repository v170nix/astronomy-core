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
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin


object RiseSetTransitCalculation {

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

    private sealed class InnerRequest {
        object UpperTransit : InnerRequest()
        object DownTransit : InnerRequest()

        data class Rise(val e: Radian) : InnerRequest() {

            internal companion object {
                inline fun toInnerResult(
                    cosAngle: Double,
                    getRiseDate: (hour: Radian) -> MJD
                ) = if (cosAngle > 1.0) {
                    InnerResult.Rise.AlwaysBelowHorizon
                } else if (cosAngle < -1.0) {
                    InnerResult.Rise.Circumpolar
                } else {
                    InnerResult.Rise.Value(getRiseDate(acos(cosAngle).rad))
                }
            }
        }

        data class Set(val e: Radian) : InnerRequest() {

            internal companion object {
                inline fun toInnerResult(
                    cosAngle: Double,
                    getSetDate: (hour: Radian) -> MJD
                ) = if (cosAngle > 1.0) {
                    InnerResult.Set.AlwaysBelowHorizon
                } else if (cosAngle < -1.0) {
                    InnerResult.Set.Circumpolar
                } else {
                    InnerResult.Set.Value(getSetDate(acos(cosAngle).rad))
                }
            }
        }

        fun getElevation() = when (this) {
            is Rise -> e
            is Set -> e
            UpperTransit -> null
            DownTransit -> null
        }
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

    private suspend inline fun stepEvent(
        time: Instant,
        position: Observer.Position,
        eph: Ephemeris,
        innerRequest: InnerRequest,
        isNextStep: Boolean = true,
        siderealTimeMethod: SiderealTimeMethod = SiderealTimeMethod.Williams1994,
        getEquationOfEquinoxes: () -> Radian
    ): InnerResult {

        val body = eph.invoke(time.toJT(true)).spherical
        val mJDUT = time.toMJD(false)
        val siderealTime =
            mJDUT.getLocalApparentSiderealTime(siderealTimeMethod, position, getEquationOfEquinoxes)
        val delta = if (isNextStep) Radian.Zero else -Radian.PI2

        if (innerRequest == InnerRequest.UpperTransit) {
            val dTransitTime = celestialHoursToEarthTime * ((body.phi - siderealTime).normalize() + delta)
            val transitAltitude =
                asin(sin(body.theta) * sin(position.latitude) + cos(body.theta) * cos(position.latitude)).rad

            return InnerResult.UpperTransit(mJDUT + dTransitTime.mJD, transitAltitude)
        }

        if (innerRequest == InnerRequest.DownTransit) {
            val dTransitTime = celestialHoursToEarthTime * ((body.phi - siderealTime).normalize() + Radian.PI + delta)
            val transitAltitude =
                asin(sin(body.theta) * sin(position.latitude) + cos(body.theta) * cos(position.latitude)).rad

            return InnerResult.DownTransit(mJDUT + dTransitTime.mJD, transitAltitude)
        }

        val elevation = innerRequest.getElevation()!!

        val cAngle = (sin(elevation) - sin(position.latitude) * sin(body.theta)) /
                (cos(position.latitude) * cos(body.theta))

        return when (innerRequest) {
            is InnerRequest.Rise -> {
                InnerRequest.Rise.toInnerResult(cAngle) { hour ->
                    val riseTime = celestialHoursToEarthTime * ((body.phi - hour - siderealTime).normalize() + delta)
                    mJDUT + riseTime.mJD
                }
            }
            is InnerRequest.Set -> {
                InnerRequest.Set.toInnerResult(cAngle) { hour ->
                    val setTime = celestialHoursToEarthTime * ((body.phi + hour - siderealTime).normalize() + delta)
                    InnerResult.Set.Value(mJDUT + setTime.mJD)
                    mJDUT + setTime.mJD
                }
            }
            else -> throw IllegalStateException()
        }
    }

    private suspend inline fun nearestEvent(
        time: Instant,
        position: Observer.Position,
        eph: Ephemeris,
        innerRequest: InnerRequest,
        siderealTimeMethod: SiderealTimeMethod = SiderealTimeMethod.Williams1994,
        getEquationOfEquinoxes: () -> Radian
    ): InnerResult {

        val body = eph.invoke(time.toJT()).spherical
        val mJDUT = time.toMJD(false)
        val siderealTime =
            mJDUT.getLocalApparentSiderealTime(siderealTimeMethod, position, getEquationOfEquinoxes)

        if (innerRequest == InnerRequest.UpperTransit) {

            val dTransitTime1 = celestialHoursToEarthTime * (body.phi - siderealTime).normalize()
            val dTransitTime2 = celestialHoursToEarthTime * ((body.phi - siderealTime).normalize() - Radian.PI2)
            val dTransitTime = if (abs(dTransitTime2) < abs(dTransitTime1)) dTransitTime2 else dTransitTime1

            val transitAltitude =
                asin(sin(body.theta) * sin(position.latitude) + cos(body.theta) * cos(position.latitude)).rad

            return InnerResult.UpperTransit(mJDUT + dTransitTime.mJD, transitAltitude)
        }

        if (innerRequest == InnerRequest.DownTransit) {

            val dTransitTime1 = celestialHoursToEarthTime * ((body.phi - siderealTime).normalize() + Radian.PI)
            val dTransitTime2 = celestialHoursToEarthTime * ((body.phi - siderealTime).normalize() - Radian.PI)
            val dTransitTime = if (abs(dTransitTime2) < abs(dTransitTime1)) dTransitTime2 else dTransitTime1

            val transitAltitude =
                asin(sin(body.theta) * sin(position.latitude) + cos(body.theta) * cos(position.latitude)).rad

            return InnerResult.DownTransit(mJDUT + dTransitTime.mJD, transitAltitude)
        }

        val elevation = innerRequest.getElevation()!!

        val cAngle = (sin(elevation) - sin(position.latitude) * sin(body.theta)) /
                (cos(position.latitude) * cos(body.theta))


        return when (innerRequest) {
            is InnerRequest.Rise -> {
                InnerRequest.Rise.toInnerResult(cAngle) { hour ->
                    val riseTime1 = celestialHoursToEarthTime * (body.phi - hour - siderealTime).normalize()
                    val riseTime2 =
                        celestialHoursToEarthTime * ((body.phi - hour - siderealTime).normalize() - Radian.PI2)
                    mJDUT + (if (abs(riseTime2) < abs(riseTime1)) riseTime2 else riseTime1).mJD
                }
            }
            is InnerRequest.Set -> {
                InnerRequest.Set.toInnerResult(cAngle) { hour ->
                    val setTime1 = celestialHoursToEarthTime * (body.phi + hour - siderealTime).normalize()
                    val setTime2 =
                        celestialHoursToEarthTime * ((body.phi + hour - siderealTime).normalize() - Radian.PI2)
                    mJDUT + (if (abs(setTime2) < abs(setTime1)) setTime2 else setTime1).mJD
                }
            }
            else -> throw IllegalStateException()
        }

    }

    suspend fun obtainNextResults(
        time: Instant,
        observer: Observer,
        ephemeris: Ephemeris,
        request: Set<Request>,
        siderealTimeMethod: SiderealTimeMethod = SiderealTimeMethod.Williams1994,
        getEquationOfEquinoxes: () -> Radian = {
            getEquationOfEquinoxes(time.toJT())
        }
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
            request to obtainNextEvents(
                time,
                observer.position,
                ephemeris,
                event,
                true,
                siderealTimeMethod,
                getEquationOfEquinoxes
            )
        }.map { (request, computed) ->
            when (computed) {
                InnerResult.None -> Result.Error(request)
                InnerResult.Rise.AlwaysBelowHorizon -> Result.Rise.AlwaysBelowHorizon(request)
                InnerResult.Rise.Circumpolar -> Result.Rise.Circumpolar(request)
                is InnerResult.Rise.Value -> Result.Rise.Value(request, computed.mjd.toInstant(false))
                InnerResult.Set.AlwaysBelowHorizon -> Result.Set.AlwaysBelowHorizon(request)
                InnerResult.Set.Circumpolar -> Result.Set.Circumpolar(request)
                is InnerResult.Set.Value -> Result.Set.Value(request, computed.mjd.toInstant(false))
                is InnerResult.UpperTransit -> Result.UpperTransit(
                    request,
                    computed.mjd.toInstant(false),
                    computed.geometricElevation.toDeg()
                )
                is InnerResult.DownTransit -> Result.DownTransit(
                    request,
                    computed.mjd.toInstant(false),
                    computed.geometricElevation.toDeg()
                )
            }
        }.toSet()
    }

    private suspend inline fun obtainNextEvents(
        time: Instant,
        position: Observer.Position,
        ephemeris: Ephemeris,
        innerRequest: InnerRequest,
        isStar: Boolean = false,
        siderealTimeMethod: SiderealTimeMethod,
        getEquationOfEquinoxes: () -> Radian
    ): InnerResult {
        val notYetCalculated = -1.0
        val precisionInSeconds = 0.5
        var n = 0
        val nMax = 20
        var lastTimeEvent = notYetCalculated
        var dt = notYetCalculated
        var timeEvent: Double = time.toMJD(false).value
        var result: InnerResult
        var triedBefore = false
        do {
            n++
            result = if (n == 1)
                stepEvent(
                    timeEvent.mJD.toInstant(false),
                    position,
                    ephemeris,
                    innerRequest,
                    true,
                    siderealTimeMethod,
                    getEquationOfEquinoxes
                )
            else
                nearestEvent(
                    timeEvent.mJD.toInstant(false),
                    position,
                    ephemeris,
                    innerRequest,
                    siderealTimeMethod,
                    getEquationOfEquinoxes
                )

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
                                val c = stepEvent(
                                    time,
                                    position,
                                    ephemeris,
                                    InnerRequest.UpperTransit,
                                    true,
                                    siderealTimeMethod,
                                    getEquationOfEquinoxes
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

    private const val celestialHoursToEarthTime: Double = RAD_TO_DAY / SIDEREAL_DAY_LENGTH

}