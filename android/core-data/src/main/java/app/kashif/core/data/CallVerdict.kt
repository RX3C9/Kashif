package app.kashif.core.data

import java.util.concurrent.ConcurrentHashMap

/** Drives the colour of every caller surface: blue / red / green (docs/DESIGN.md). */
enum class CallVerdict { NORMAL, SPAM, VERIFIED_BUSINESS }

data class CachedVerdict(val verdict: CallVerdict, val label: String?, val reports: Int, val at: Long)

/** Hand-off from CallScreeningService to the in-call UI / overlay (same process). */
object VerdictCache {
    private const val TTL_MS = 10 * 60 * 1000L
    private val map = ConcurrentHashMap<String, CachedVerdict>()

    fun put(key: String, v: CachedVerdict) { map[key] = v }

    fun get(key: String?, now: Long = System.currentTimeMillis()): CachedVerdict? {
        if (key.isNullOrEmpty()) return null
        val v = map[key] ?: return null
        if (now - v.at > TTL_MS) { map.remove(key); return null }
        return v
    }

    fun clear() = map.clear()
}
