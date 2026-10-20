package app.kashif.core.data

enum class RuleType {
    /** Exact number (any format; compared after normalisation). */
    EXACT,
    /** Number starts with the pattern, e.g. "+2015", "015" or "0900". */
    PREFIX,
    /** Country calling code, e.g. "91" or "+234". */
    COUNTRY,
    /** Kotlin regex against the E.164 form (falls back to raw digits). */
    REGEX,
    /** Private / withheld / unavailable numbers. */
    HIDDEN,
    /** Any number not in the user's contacts. */
    NOT_IN_CONTACTS;

    companion object { fun parse(s: String) = entries.firstOrNull { it.name == s } ?: EXACT }
}

enum class RuleAction {
    REJECT, SILENCE;
    companion object { fun parse(s: String) = entries.firstOrNull { it.name == s } ?: REJECT }
}

/** What the screening layer knows about the incoming call. */
data class IncomingNumber(
    val raw: String,
    val e164: String?,
    val nationalSignificant: String?,
    val hidden: Boolean,
    val inContacts: Boolean,
) {
    companion object {
        fun of(raw: String?, inContacts: Boolean = false, defaultRegion: String = PhoneNumbers.DEFAULT_REGION): IncomingNumber {
            val r = raw.orEmpty().trim()
            val hidden = PhoneNumbers.digits(r).isEmpty()
            return IncomingNumber(r, PhoneNumbers.toE164(r, defaultRegion),
                PhoneNumbers.nationalSignificant(r, defaultRegion), hidden, inContacts)
        }
    }

    /** Every textual form a user-typed pattern might be written in. */
    internal val forms: List<String> by lazy {
        buildList {
            val d = PhoneNumbers.digits(raw)
            if (d.isNotEmpty()) add(d)
            if (raw.trim().startsWith("+") && d.isNotEmpty()) add("+$d")
            e164?.let { add(it); add(it.removePrefix("+")) }
            nationalSignificant?.let { add(it); add("0$it") }
        }.distinct()
    }
}

/** Pure, side-effect-free rule matching (unit-tested; no Android deps). */
object BlockRuleMatcher {
    fun firstMatch(rules: List<BlockRule>, n: IncomingNumber): BlockRule? =
        rules.firstOrNull { it.enabled && matches(it, n) }

    fun matches(rule: BlockRule, n: IncomingNumber): Boolean = when (rule.ruleType) {
        RuleType.HIDDEN -> n.hidden
        RuleType.NOT_IN_CONTACTS -> !n.hidden && !n.inContacts
        RuleType.EXACT -> !n.hidden && exact(rule.pattern, n)
        RuleType.PREFIX -> !n.hidden && prefix(rule.pattern, n)
        RuleType.COUNTRY -> !n.hidden && country(rule.pattern, n)
        RuleType.REGEX -> !n.hidden && regex(rule.pattern, n)
    }

    private fun cleanPattern(p: String): String {
        val t = PhoneNumbers.normalizeDigits(p.trim())
        val d = t.filter { it.isDigit() }
        return if (t.startsWith("+")) "+$d" else d
    }

    private fun exact(p: String, n: IncomingNumber): Boolean {
        val pe = PhoneNumbers.toE164(p)
        if (pe != null && n.e164 != null) return pe == n.e164
        val c = cleanPattern(p)
        return c.isNotEmpty() && n.forms.any { it == c }
    }

    private fun prefix(p: String, n: IncomingNumber): Boolean {
        val c = cleanPattern(p)
        if (c.isEmpty() || c == "+") return false
        return n.forms.any { it.startsWith(c) }
    }

    private fun country(p: String, n: IncomingNumber): Boolean {
        val cc = cleanPattern(p).removePrefix("+")
        if (cc.isEmpty()) return false
        val e = n.e164 ?: return false
        return PhoneNumbers.countryCode(e)?.toString() == cc
    }

    private fun regex(p: String, n: IncomingNumber): Boolean {
        val re = runCatching { Regex(p) }.getOrNull() ?: return false
        val target = n.e164 ?: PhoneNumbers.digits(n.raw)
        return re.containsMatchIn(target)
    }
}
