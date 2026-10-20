package app.kashif.core.data

import java.security.MessageDigest

/**
 * Privacy-preserving number identifiers. Spam DB rows and reports carry only
 * SHA-256(PEPPER ‖ E.164); online lookups (M2) send only the first [PREFIX_LEN]
 * hex chars (k-anonymity) and match the full hash locally.
 */
object NumberHash {
    const val PEPPER = "kashif.v1:"
    const val PREFIX_LEN = 5

    fun of(e164: String): String {
        val md = MessageDigest.getInstance("SHA-256").digest((PEPPER + e164).toByteArray(Charsets.UTF_8))
        return md.joinToString("") { "%02x".format(it) }
    }

    fun prefix(hash: String): String = hash.take(PREFIX_LEN)
}
