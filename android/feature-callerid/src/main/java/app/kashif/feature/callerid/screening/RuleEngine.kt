package app.kashif.feature.callerid.screening

/** Placeholder rule engine; real rules (exact/prefix/country/regex/hidden/schedule) live in Room via core-data. */
object RuleEngine {
    var prefixes: Set<String> = emptySet()
    fun shouldBlock(number: String): Boolean = prefixes.any { number.startsWith(it) }
}
