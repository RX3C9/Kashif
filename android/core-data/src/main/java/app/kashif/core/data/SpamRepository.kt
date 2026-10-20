package app.kashif.core.data

/** One-tap report + offline lookup. Everything here is local; nothing leaves the device. */
class SpamRepository(private val db: KashifDatabase) {

    suspend fun lookup(e164: String?): SpamEntry? = e164?.let { db.spam().byHash(NumberHash.of(it)) }

    /** One-tap "report spam": records the report, marks the number as spam locally, optionally blocks it. */
    suspend fun reportSpam(e164: String, category: String, alsoBlock: Boolean, now: Long = System.currentTimeMillis()) {
        val hash = NumberHash.of(e164)
        db.reports().insert(LocalReport(numberHash = hash, category = category, createdAt = now))
        val old = db.spam().byHash(hash)
        db.spam().upsert(SpamReports.applyUserReport(old, hash, category, now))
        if (alsoBlock) db.blockRules().insert(
            BlockRule(type = RuleType.EXACT.name, pattern = e164, label = category, createdAt = now))
    }

    suspend fun importSeed(text: String, now: Long = System.currentTimeMillis()): Int {
        val rows = SpamSeed.parse(text, now)
        if (rows.isNotEmpty()) db.spam().upsertAll(rows)
        return rows.size
    }
}

object SpamReports {
    /** A personal report always pushes the number to spam for this user. */
    fun applyUserReport(old: SpamEntry?, hash: String, category: String, now: Long): SpamEntry =
        old?.copy(score = maxOf(old.score, 1f), reports = old.reports + 1, category = category,
            updatedAt = now, userReported = true)
            ?: SpamEntry(hash, NumberHash.prefix(hash), 1f, category, 1, now, userReported = true)
}
