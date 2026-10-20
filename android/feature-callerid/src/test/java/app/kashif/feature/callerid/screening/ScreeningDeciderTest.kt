package app.kashif.feature.callerid.screening

import app.kashif.core.data.BlockRule
import app.kashif.core.data.CallVerdict
import app.kashif.core.data.IncomingNumber
import app.kashif.core.data.NumberHash
import app.kashif.core.data.RuleAction
import app.kashif.core.data.RuleType
import app.kashif.core.data.SpamEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreeningDeciderTest {
    private val e164 = "+201001234567"
    private val stranger = IncomingNumber.of(e164)
    private val contact = IncomingNumber.of(e164, inContacts = true)
    private val h = NumberHash.of(e164)
    private fun spam(score: Float, reports: Int, user: Boolean = false) =
        SpamEntry(h, NumberHash.prefix(h), score, "scam", reports, 0, userReported = user)
    private val blockAll = listOf(BlockRule(type = RuleType.NOT_IN_CONTACTS.name, action = RuleAction.SILENCE.name))

    @Test fun cleanNumberRings() {
        val d = ScreeningDecider.decide(stranger, emptyList(), null)
        assertEquals(CallVerdict.NORMAL, d.verdict); assertFalse(d.blocks)
    }

    @Test fun contactsAlwaysRing() {
        val d = ScreeningDecider.decide(contact, blockAll, spam(1f, 900))
        assertEquals(Reason.CONTACT, d.reason); assertNull(d.action)
    }

    @Test fun userRuleAppliesItsAction() {
        val d = ScreeningDecider.decide(stranger, blockAll, null)
        assertEquals(RuleAction.SILENCE, d.action); assertEquals(Reason.USER_RULE, d.reason)
    }

    @Test fun topSpammerBlocked() {
        val d = ScreeningDecider.decide(stranger, emptyList(), spam(0.95f, 587))
        assertEquals(CallVerdict.SPAM, d.verdict); assertEquals(RuleAction.REJECT, d.action)
        assertEquals(587, d.reports); assertEquals("scam", d.label)
    }

    @Test fun fewReportsOnlyWarns() {
        val d = ScreeningDecider.decide(stranger, emptyList(), spam(0.9f, 1))
        assertEquals(CallVerdict.SPAM, d.verdict); assertFalse(d.blocks); assertEquals(Reason.SUSPECTED_SPAM, d.reason)
    }

    @Test fun autoBlockCanBeOff() {
        val d = ScreeningDecider.decide(stranger, emptyList(), spam(0.95f, 587), ScreeningSettings(blockTopSpammers = false))
        assertTrue(d.verdict == CallVerdict.SPAM && !d.blocks)
    }

    @Test fun lowScoreIsNormal() =
        assertEquals(CallVerdict.NORMAL, ScreeningDecider.decide(stranger, emptyList(), spam(0.2f, 50)).verdict)

    @Test fun personalReportAlwaysBlocks() =
        assertEquals(Reason.USER_REPORTED, ScreeningDecider.decide(stranger, emptyList(), spam(1f, 1, user = true)).reason)
}
