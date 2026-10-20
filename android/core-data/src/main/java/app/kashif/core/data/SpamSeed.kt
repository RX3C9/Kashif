package app.kashif.core.data

/**
 * Offline spam DB import format (one row per line, '#' comments allowed):
 *   hash,category,score,reports
 * where hash = [NumberHash.of] of the E.164 number. Files are shipped in assets or
 * downloaded as signed delta packs (M2) — never contain raw numbers.
 */
object SpamSeed {
    private val HEX64 = Regex("^[0-9a-f]{64}$")

    fun parse(text: String, now: Long): List<SpamEntry> = text.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val p = line.split(',').map { it.trim() }
            if (p.size < 4) return@mapNotNull null
            val hash = p[0].lowercase()
            if (!HEX64.matches(hash)) return@mapNotNull null
            val score = p[2].toFloatOrNull()?.coerceIn(0f, 1f) ?: return@mapNotNull null
            val reports = p[3].toIntOrNull()?.coerceAtLeast(0) ?: return@mapNotNull null
            SpamEntry(hash, NumberHash.prefix(hash), score, p[1].ifEmpty { "spam" }, reports, now)
        }
        .toList()
}
