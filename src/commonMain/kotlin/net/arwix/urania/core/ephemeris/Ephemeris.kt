@file:Suppress("unused")

package net.arwix.urania.core.ephemeris

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.arwix.urania.core.annotation.ExperimentalUrania
import net.arwix.urania.core.calendar.JT
import net.arwix.urania.core.calendar.jT
import net.arwix.urania.core.math.JULIAN_DAYS_PER_CENTURY
import net.arwix.urania.core.math.vector.Vector

interface Ephemeris {
    val metadata: Metadata
    suspend operator fun invoke(jT: JT): Vector
    suspend fun getEphemerisVector(jT: JT): EphemerisVector {
        return EphemerisVector(invoke(jT), metadata)
    }

    suspend fun getVelocity(vectorAtJTTime: Vector, jT: JT, deltaTimeInDay: Double = 0.01): Vector {
        val bodyPlus = invoke(jT + (deltaTimeInDay / JULIAN_DAYS_PER_CENTURY).jT)
        return (bodyPlus - vectorAtJTTime) / deltaTimeInDay
    }
}

@ExperimentalUrania
class WrapperCacheEphemeris(private val ephemeris: Ephemeris): Ephemeris {
    override val metadata: Metadata = ephemeris.metadata
    private val cache = mutableMapOf<JT, Vector>()
    private val mutex = Mutex()
    private val computationMap = mutableMapOf<JT, Deferred<Vector>>()

    override suspend fun invoke(jT: JT): Vector = coroutineScope {
        val cacheElement: Vector? = cache[jT]
        if (cacheElement == null) {
            val currentDeferred = mutex.withLock { computationMap[jT] }
            if (currentDeferred != null) return@coroutineScope currentDeferred.await()

            val deferred = async(Dispatchers.Default, start = CoroutineStart.LAZY) { ephemeris(jT) }
            val newResult = mutex.withLock {
                val currentDeferred2 = computationMap[jT]
                if (currentDeferred2 == null) {
                    computationMap[jT] = deferred
                    deferred.start()
                    deferred
                } else currentDeferred2
            }.await()
            cache[jT] = newResult
            @Suppress("DeferredResultUnused")
            mutex.withLock {
                computationMap.remove(jT)
            }
            newResult
        } else cacheElement
    }

}