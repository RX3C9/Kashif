package app.kashif.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpamSeedTest {
    private val h = NumberHash.of("+201001234567")

    @Test fun parsesValidSkipsJunk() {
        val text = """
            # header comment
            $h,scam,0.95,587
            nothex,scam,0.9,1
            ${h.uppercase()},sales,1.7,-4
            short,line
        """.trimIndent()
        val rows = SpamSeed.parse(text, now = 42)
        assertEquals(2, rows.size)
        assertEquals("scam", rows[0].category)
        assertEquals(587, rows[0].reports)
        assertEquals(NumberHash.prefix(h), rows[0].hashPrefix)
        assertEquals(1f, rows[1].score)
        assertEquals(0, rows[1].reports)
        assertEquals(42L, rows[1].updatedAt)
    }

    @Test fun userReportCreatesOrBumps() {
        val fresh = SpamReports.applyUserReport(null, h, "scam", 1)
        assertEquals(1, fresh.reports); assertTrue(fresh.userReported); assertEquals(1f, fresh.score)
        val bumped = SpamReports.applyUserReport(fresh.copy(score = 0.3f, reports = 9, userReported = false), h, "sales", 2)
        assertEquals(10, bumped.reports); assertEquals("sales", bumped.category); assertTrue(bumped.userReported)
    }
}
