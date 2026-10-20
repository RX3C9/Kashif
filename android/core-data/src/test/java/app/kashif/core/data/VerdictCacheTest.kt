package app.kashif.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerdictCacheTest {
    @Test fun expires() {
        VerdictCache.clear()
        VerdictCache.put("+20", CachedVerdict(CallVerdict.SPAM, "scam", 3, at = 0))
        assertEquals(CallVerdict.SPAM, VerdictCache.get("+20", now = 1000)?.verdict)
        assertNull(VerdictCache.get("+20", now = 11 * 60 * 1000L))
        assertNull(VerdictCache.get(null))
    }
}
