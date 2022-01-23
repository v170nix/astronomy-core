package net.arwix.urania.core.ephemeris.calculation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import kotlinx.datetime.Instant
import net.arwix.urania.core.annotation.Apparent
import net.arwix.urania.core.annotation.Ecliptic
import net.arwix.urania.core.annotation.Geocentric
import net.arwix.urania.core.calendar.*
import net.arwix.urania.core.ephemeris.Ephemeris
import net.arwix.urania.core.math.angle.Radian
import net.arwix.urania.core.math.angle.sin
import net.arwix.urania.core.spherical
import kotlin.math.abs

@Suppress("MemberVisibilityCanBePrivate")
object EquinoxesSolsticesCalculation {

    sealed class Result(open val date: Instant) {
        data class SpringEquinox(override val date: Instant) : Result(date)
        data class SummerSolstice(override val date: Instant) : Result(date)
        data class AutumnEquinox(override val date: Instant) : Result(date)
        data class WinterSolstice(override val date: Instant) : Result(date)
    }

    sealed class Request(val year: Int) : Iterator<Request> {

        internal fun getInitMonth() = when (this) {
            is SpringEquinox -> 3
            is SummerSolstice -> 6
            is AutumnEquinox -> 9
            is WinterSolstice -> 12
        }

        internal fun createResult(date: Instant) = when (this) {
            is SpringEquinox -> Result.SpringEquinox(date)
            is SummerSolstice -> Result.SummerSolstice(date)
            is AutumnEquinox -> Result.AutumnEquinox(date)
            is WinterSolstice -> Result.WinterSolstice(date)
        }

        class SpringEquinox(year: Int) : Request(year) {
            override fun hasNext() = true
            override fun next() = SummerSolstice(year)
        }

        class SummerSolstice(year: Int) : Request(year) {
            override fun hasNext() = true
            override fun next() = AutumnEquinox(year)
        }

        class AutumnEquinox(year: Int) : Request(year) {
            override fun hasNext() = true
            override fun next() = WinterSolstice(year)
        }

        class WinterSolstice(year: Int) : Request(year) {
            override fun hasNext() = false
            override fun next() = SpringEquinox(year)
        }

    }

    suspend fun findEvents(
        year: Int,
        @Geocentric @Ecliptic @Apparent getSunEphemeris: (jT0: JT) -> Ephemeris,
        precision: Double = 0.1 / 24.0 / 3600.0
    ) = coroutineScope {
        val requests = listOf(Request.SpringEquinox(year),
            Request.SummerSolstice(year),
            Request.AutumnEquinox(year),
            Request.WinterSolstice(year))
        Array(4) {
            async(Dispatchers.Default) {
                findEvent(requests[it], getSunEphemeris, precision)
            }
        }.map { yield(); it.await() }
    }


    suspend fun findEvent(
        request: Request,
        @Geocentric @Ecliptic @Apparent getSunEphemeris: (jT0: JT) -> Ephemeris,
        precision: Double = 0.1 / 24.0 / 3600.0
    ): Result {

        var jT = MJD(request.year, request.getInitMonth(), 1).toJT()
        var isFirst = true
        var delta = 1.0
        var ephemeris = getSunEphemeris(jT)

        do {
            if (delta >= 0 && !isFirst) {
                ephemeris = getSunEphemeris(jT)
            }
            val body = ephemeris(jT)
            yield()
            delta = getDelta(request, body.spherical.phi)
            jT += (delta / 36525.0).jT
            isFirst = false

        } while (abs(delta) > precision)

        return request.createResult(jT.toInstant())
    }

    private inline fun getDelta(request: Request, longitude: Radian) = when (request) {
        is Request.SpringEquinox -> 58.13 * sin(-longitude)
        is Request.SummerSolstice -> 58.13 * sin(Radian.PI / 2.0 - longitude)
        is Request.AutumnEquinox -> 58.13 * sin(Radian.PI - longitude)
        is Request.WinterSolstice -> 58.13 * sin(-Radian.PI / 2.0 - longitude)
    }

}
