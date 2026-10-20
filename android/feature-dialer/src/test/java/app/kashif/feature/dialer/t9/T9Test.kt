package app.kashif.feature.dialer.t9

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class T9Test {
    @Test fun englishEncoding() = assertEquals("6627", T9.encode("Omar"))
    @Test fun arabicEncoding() = assertEquals("3685", T9.encode("احمد"))
    @Test fun arabicKeys() {
        assertEquals('3', T9.keyFor('ا')); assertEquals('6', T9.keyFor('ح'))
        assertEquals('8', T9.keyFor('م')); assertEquals('5', T9.keyFor('د'))
        assertEquals('9', T9.keyFor('ع')); assertEquals('2', T9.keyFor('ة'))
    }
    @Test fun tashkeelIgnored() = assertEquals(T9.encode("محمد"), T9.encode("مُحَمَّد"))

    @Test fun namePrefix() = assertEquals(T9.Kind.NAME_PREFIX, T9.match("662", "Omar Khaled", "0100")?.kind)
    @Test fun secondWordPrefix() = assertEquals(T9.Kind.NAME_PREFIX, T9.match(T9.encode("علي"), "احمد علي", "")?.kind)
    @Test fun initials() = assertEquals(T9.Kind.INITIALS, T9.match("65", "Omar Khaled", "")?.kind)
    @Test fun numberSubstring() = assertEquals(T9.Kind.NUMBER, T9.match("1234", "", "+20 100 123 4567")?.kind)
    @Test fun localZeroAgainstInternational() = assertEquals(T9.Kind.NUMBER, T9.match("0100123", "", "+201001234567")?.kind)
    @Test fun arabicIndicQuery() = assertEquals(T9.Kind.NUMBER, T9.match("١٢٣", "", "0123")?.kind)
    @Test fun noMatch() = assertNull(T9.match("999", "Omar", "0100"))
    @Test fun emptyQuery() = assertNull(T9.match("", "Omar", "0100"))

    @Test fun rankingPrefersNames() {
        data class C(val n: String, val p: String)
        val list = listOf(C("Zed", "0662"), C("Omar", "0100"), C("Mona", "0111"))
        assertEquals(listOf("Omar", "Mona", "Zed"), T9.rank("66", list, { it.n }, { it.p }).map { it.n })
    }
}
