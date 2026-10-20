package app.kashif.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNumbersTest {
    @Test fun egyptianMobileLocalFormat() = assertEquals("+201001234567", PhoneNumbers.toE164("010 0123 4567"))
    @Test fun internationalPrefix00() = assertEquals("+201001234567", PhoneNumbers.toE164("00201001234567"))
    @Test fun arabicIndicDigits() = assertEquals("+201001234567", PhoneNumbers.toE164("٠١٠٠١٢٣٤٥٦٧"))
    @Test fun foreignNumberKeepsCountry() = assertEquals("+919876543210", PhoneNumbers.toE164("+91 98765 43210"))
    @Test fun shortCodeIsNotE164() = assertNull(PhoneNumbers.toE164("19999"))
    @Test fun emptyIsNull() = assertNull(PhoneNumbers.toE164(""))
    @Test fun countryCode() = assertEquals(234, PhoneNumbers.countryCode("+2348031234567"))
    @Test fun region() = assertEquals("EG", PhoneNumbers.regionOf("+201001234567"))
    @Test fun digitsNormalises() = assertEquals("0123", PhoneNumbers.digits("+٠١-2(3)"))
}
