package app.kashif.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockRuleMatcherTest {
    private fun rule(t: RuleType, p: String = "", enabled: Boolean = true) =
        BlockRule(type = t.name, pattern = p, enabled = enabled)

    private val mobile = IncomingNumber.of("+201001234567")
    private val nigeria = IncomingNumber.of("+2348031234567")
    private val hidden = IncomingNumber.of(null)

    @Test fun exactAcrossFormats() {
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.EXACT, "01001234567"), mobile))
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.EXACT, "+20 100 123 4567"), mobile))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.EXACT, "01001234568"), mobile))
    }

    @Test fun exactShortCode() =
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.EXACT, "19999"), IncomingNumber.of("19999")))

    @Test fun prefixLocalAndInternational() {
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.PREFIX, "0100"), mobile))
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.PREFIX, "+20100"), mobile))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.PREFIX, "0155"), mobile))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.PREFIX, ""), mobile))
    }

    @Test fun country() {
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.COUNTRY, "+234"), nigeria))
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.COUNTRY, "234"), nigeria))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.COUNTRY, "234"), mobile))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.COUNTRY, "23"), nigeria))
    }

    @Test fun regex() {
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.REGEX, "^\\+20100"), mobile))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.REGEX, "[invalid"), mobile))
    }

    @Test fun hiddenAndContacts() {
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.HIDDEN), hidden))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.HIDDEN), mobile))
        assertTrue(BlockRuleMatcher.matches(rule(RuleType.NOT_IN_CONTACTS), mobile))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.NOT_IN_CONTACTS), IncomingNumber.of("+201001234567", inContacts = true)))
        assertFalse(BlockRuleMatcher.matches(rule(RuleType.PREFIX, "0"), hidden))
    }

    @Test fun firstMatchSkipsDisabled() {
        val r = listOf(rule(RuleType.PREFIX, "0100", enabled = false), rule(RuleType.COUNTRY, "20"))
        assertEquals(RuleType.COUNTRY, BlockRuleMatcher.firstMatch(r, mobile)?.ruleType)
        assertNull(BlockRuleMatcher.firstMatch(r.take(1), mobile))
    }

    @Test fun unknownEnumFallsBack() {
        assertEquals(RuleType.EXACT, RuleType.parse("NOPE"))
        assertEquals(RuleAction.REJECT, RuleAction.parse(""))
    }
}
