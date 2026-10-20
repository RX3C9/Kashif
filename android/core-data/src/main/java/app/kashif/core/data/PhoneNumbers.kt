package app.kashif.core.data

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

/** Thin libphonenumber wrapper. Egypt is the default region; works fully offline. */
object PhoneNumbers {
    const val DEFAULT_REGION = "EG"
    private val util: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    /** Maps Arabic-Indic (٠-٩) and Persian (۰-۹) digits to ASCII. */
    fun normalizeDigits(s: String): String = buildString(s.length) {
        for (ch in s) append(
            when (ch) {
                in '\u0660'..'\u0669' -> '0' + (ch - '\u0660')
                in '\u06F0'..'\u06F9' -> '0' + (ch - '\u06F0')
                else -> ch
            }
        )
    }

    fun digits(s: String): String = normalizeDigits(s).filter { it in '0'..'9' }

    private fun parse(raw: String, region: String): Phonenumber.PhoneNumber? {
        val s = normalizeDigits(raw).trim()
        if (digits(s).length < 3) return null
        return runCatching { util.parse(s, region) }.getOrNull()
    }

    fun toE164(raw: String, region: String = DEFAULT_REGION): String? {
        val p = parse(raw, region) ?: return null
        if (!util.isPossibleNumber(p)) return null
        return util.format(p, PhoneNumberUtil.PhoneNumberFormat.E164)
    }

    fun nationalSignificant(raw: String, region: String = DEFAULT_REGION): String? {
        val p = parse(raw, region) ?: return null
        return util.getNationalSignificantNumber(p).ifEmpty { null }
    }

    fun countryCode(e164: String): Int? = parse(e164, DEFAULT_REGION)?.countryCode

    /** ISO region (e.g. "EG") for display: "Call from +20 … · Egypt". */
    fun regionOf(e164: String): String? = parse(e164, DEFAULT_REGION)?.let { util.getRegionCodeForNumber(it) }

    fun pretty(e164: String): String = parse(e164, DEFAULT_REGION)
        ?.let { util.format(it, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL) } ?: e164
}
