package app.kashif.feature.callerid.overlay

import app.kashif.core.data.CallVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayModelTest {
    @Test fun spamIsRed() {
        val m = OverlayModel.of(CallVerdict.SPAM, null, 5, "+201001234567", false)
        assertEquals(OverlayModel.RED, m.colorArgb)
        assertTrue(m.subtitle.contains("5"))
    }
    @Test fun verifiedIsGreenArabic() {
        val m = OverlayModel.of(CallVerdict.VERIFIED_BUSINESS, "بنك", 0, null, true)
        assertEquals(OverlayModel.GREEN, m.colorArgb)
        assertEquals("بنك", m.title)
    }
    @Test fun unknownNormalIsBlue() {
        val m = OverlayModel.of(CallVerdict.NORMAL, null, 0, null, true)
        assertEquals(OverlayModel.BLUE, m.colorArgb)
        assertEquals("رقم مجهول", m.title)
    }
}
