package app.kashif.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CallLogFormatTest {
    @Test fun directions() {
        assertEquals(CallDirection.INCOMING, CallLogFormat.direction(1))
        assertEquals(CallDirection.OUTGOING, CallLogFormat.direction(2))
        assertEquals(CallDirection.MISSED, CallLogFormat.direction(3))
        assertEquals(CallDirection.REJECTED, CallLogFormat.direction(5))
    }

    @Test fun groupsConsecutiveSameNumberAndDirection() {
        val calls = listOf(RawCall("0100", "Ali", 3, 3, 0), RawCall("0100", "Ali", 3, 2, 0),
            RawCall("0100", "Ali", 1, 1, 5))
        val rows = CallLogFormat.group(calls) { null }
        assertEquals(2, rows.size)
        assertEquals(2, rows[0].count)
        assertEquals("Ali", rows[0].title)
    }

    @Test fun spamEntryColoursRow() {
        val e = SpamEntry("h", "p", 0.9f, "scam", 12, 0L)
        val rows = CallLogFormat.group(listOf(RawCall("123", null, 1, 1, 0))) { e }
        assertEquals(CallVerdict.SPAM, rows[0].verdict)
        assertEquals(12, rows[0].reports)
    }
}
