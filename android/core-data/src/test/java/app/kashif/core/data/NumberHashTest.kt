package app.kashif.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberHashTest {
    @Test fun stableVector() = assertEquals(
        "173105e8f158ef8f658dffa52ca537282b362db60a83e99845c907b6b9d45df4", NumberHash.of("+201001234567"))

    @Test fun prefixIsFiveHex() = assertEquals("17310", NumberHash.prefix(NumberHash.of("+201001234567")))
}
