package app.kashif.feature.callerid.screening

import app.kashif.core.data.BlockRule
import app.kashif.core.data.BlockRuleMatcher
import app.kashif.core.data.CallVerdict
import app.kashif.core.data.IncomingNumber
import app.kashif.core.data.RuleAction
import app.kashif.core.data.SpamEntry
import app.kashif.core.data.ruleAction

data class ScreeningSettings(
    /** Auto-block numbers the community DB marks as top spammers. */
    val blockTopSpammers: Boolean = true,
    val spamScoreThreshold: Float = 0.7f,
    val minReportsToBlock: Int = 3,
    /** Contacts are never blocked by pattern rules unless the user turns this off. */
    val neverBlockContacts: Boolean = true,
)

enum class Reason { CONTACT, USER_RULE, SPAM_DB, USER_REPORTED, SUSPECTED_SPAM, NONE }

data class Decision(
    val verdict: CallVerdict,
    /** null = let it ring. */
    val action: RuleAction?,
    val reason: Reason,
    val label: String? = null,
    val reports: Int = 0,
) {
    val blocks: Boolean get() = action != null
}

/** Pure decision function used by [KashifScreeningService]; unit-tested without Android. */
object ScreeningDecider {
    fun decide(n: IncomingNumber, rules: List<BlockRule>, spam: SpamEntry?, s: ScreeningSettings = ScreeningSettings()): Decision {
        if (n.inContacts && s.neverBlockContacts) return Decision(CallVerdict.NORMAL, null, Reason.CONTACT)

        BlockRuleMatcher.firstMatch(rules, n)?.let { r ->
            val verdict = if (spam != null && spam.score >= s.spamScoreThreshold) CallVerdict.SPAM else CallVerdict.NORMAL
            return Decision(verdict, r.ruleAction, Reason.USER_RULE, r.label.ifBlank { null }, spam?.reports ?: 0)
        }

        if (spam != null) {
            if (spam.userReported) return Decision(CallVerdict.SPAM, RuleAction.REJECT, Reason.USER_REPORTED, spam.category, spam.reports)
            if (spam.score >= s.spamScoreThreshold) {
                val block = s.blockTopSpammers && spam.reports >= s.minReportsToBlock
                return Decision(CallVerdict.SPAM, if (block) RuleAction.REJECT else null,
                    if (block) Reason.SPAM_DB else Reason.SUSPECTED_SPAM, spam.category, spam.reports)
            }
        }
        return Decision(CallVerdict.NORMAL, null, Reason.NONE)
    }
}
