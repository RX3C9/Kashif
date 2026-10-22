package app.kashif.core.data

/** Raw call-log row (from the CallLog provider) before verdict colouring. */
data class RawCall(val number: String, val cachedName: String?, val type: Int, val date: Long, val durationSec: Long)

enum class CallDirection { INCOMING, OUTGOING, MISSED, REJECTED }

data class CallRow(
    val number: String, val title: String, val direction: CallDirection, val date: Long,
    val verdict: CallVerdict, val reports: Int, val category: String?, val count: Int,
)

object CallLogFormat {
    // android.provider.CallLog.Calls type constants (kept literal so this stays plain Kotlin)
    fun direction(type: Int): CallDirection = when (type) {
        1 -> CallDirection.INCOMING
        2 -> CallDirection.OUTGOING
        3 -> CallDirection.MISSED
        5, 6 -> CallDirection.REJECTED
        else -> CallDirection.INCOMING
    }

    fun verdictOf(entry: SpamEntry?): CallVerdict =
        if (entry != null && entry.score >= 0.5f) CallVerdict.SPAM else CallVerdict.NORMAL

    /** Collapses consecutive calls from the same number with the same direction into one row with a count. */
    fun group(calls: List<RawCall>, lookup: (String) -> SpamEntry?): List<CallRow> {
        val out = ArrayList<CallRow>()
        for (c in calls) {
            val dir = direction(c.type)
            val last = out.lastOrNull()
            if (last != null && last.number == c.number && last.direction == dir) {
                out[out.size - 1] = last.copy(count = last.count + 1)
                continue
            }
            val e = lookup(c.number)
            val title = c.cachedName?.takeIf { it.isNotBlank() }
                ?: PhoneNumbers.toE164(c.number)?.let { PhoneNumbers.pretty(it) } ?: c.number.ifBlank { "؟" }
            out.add(CallRow(c.number, title, dir, c.date, verdictOf(e), e?.reports ?: 0, e?.category, 1))
        }
        return out
    }
}
