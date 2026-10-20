package app.kashif.feature.dialer.t9

/**
 * T9 smart-dial matching for Arabic + English names.
 * Arabic layout follows the common Arabic phone keypad (E.161-style):
 *   2 ب ت ث ة · 3 ا أ إ آ ء ؤ ئ ى · 4 س ش ص ض · 5 د ذ ر ز · 6 ج ح خ
 *   7 ن ه و ي · 8 ف ق ك ل م · 9 ط ظ ع غ
 */
object T9 {
    private val keys: Map<Char, Char> = buildMap {
        fun bind(d: Char, chars: String) = chars.forEach { c -> this[c] = d }
        bind('2', "abc"); bind('3', "def"); bind('4', "ghi"); bind('5', "jkl")
        bind('6', "mno"); bind('7', "pqrs"); bind('8', "tuv"); bind('9', "wxyz")
        bind('2', "بتثة"); bind('3', "اأإآءؤئى"); bind('4', "سشصض"); bind('5', "دذرز")
        bind('6', "جحخ"); bind('7', "نهوي"); bind('8', "فقكلم"); bind('9', "طظعغ")
        for (c in '0'..'9') put(c, c)
    }

    /** Arabic diacritics (tashkeel) + tatweel are ignored. */
    private fun isIgnorable(c: Char) = c in '\u064B'..'\u065F' || c == '\u0670' || c == '\u0640'

    private fun normalizeDigit(c: Char): Char = when (c) {
        in '\u0660'..'\u0669' -> '0' + (c - '\u0660')
        in '\u06F0'..'\u06F9' -> '0' + (c - '\u06F0')
        else -> c
    }

    fun keyFor(c: Char): Char? = keys[normalizeDigit(c).lowercaseChar()]

    /** T9 digits of one word, skipping characters with no key. */
    fun encode(word: String): String = buildString {
        for (c in word) { if (!isIgnorable(c)) keyFor(c)?.let { append(it) } }
    }

    fun digitsOnly(s: String): String = buildString { for (c in s) { val d = normalizeDigit(c); if (d in '0'..'9') append(d) } }

    enum class Kind { NAME_PREFIX, INITIALS, NUMBER }
    data class Match(val kind: Kind, val score: Int)

    /**
     * @param query digits typed on the dialpad.
     * Scores: name-word prefix (best, earlier word wins) > initials > number substring.
     */
    fun match(query: String, name: String, number: String): Match? {
        val q = digitsOnly(query)
        if (q.isEmpty()) return null
        val words = name.split(' ', '-', '_', '.').filter { it.isNotBlank() }.map { encode(it) }.filter { it.isNotEmpty() }
        words.forEachIndexed { i, w ->
            if (w.startsWith(q)) return Match(Kind.NAME_PREFIX, 300 - i * 10)
        }
        // Joined name lets "احمدعلي"-style continuous typing match across words.
        val joined = words.joinToString("")
        if (joined.startsWith(q)) return Match(Kind.NAME_PREFIX, 250)
        if (words.size > 1 && words.joinToString("") { it.take(1) }.startsWith(q)) return Match(Kind.INITIALS, 200)
        val num = digitsOnly(number)
        val idx = num.indexOf(q)
        if (idx >= 0) return Match(Kind.NUMBER, 100 - idx.coerceAtMost(50))
        // Local 0-prefixed query against an international number (+20 100… vs 0100…).
        if (q.startsWith("0") && q.length > 1) {
            val i2 = num.indexOf(q.substring(1))
            if (i2 >= 0) return Match(Kind.NUMBER, 90 - i2.coerceAtMost(50))
        }
        return null
    }

    fun <T> rank(query: String, items: List<T>, name: (T) -> String, number: (T) -> String): List<T> =
        items.mapNotNull { item -> match(query, name(item), number(item))?.let { item to it.score } }
            .sortedByDescending { it.second }
            .map { it.first }
}
