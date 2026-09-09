package com.bynx.anagrams

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TextNormalizerTest {

    private val textNormalizer = TextNormalizer()

    @Test
    fun `lowercases letters`() {
        assertEquals("dormitory", textNormalizer.normalizeText("DoRmiToRy"))
    }

    @Test
    fun `drops whitespace`() {
        assertEquals("dirtyroom", textNormalizer.normalizeText(" dirty  room\t"))
    }

    @Test
    fun `drops punctuation and symbols`() {
        assertEquals("wormsandwich", textNormalizer.normalizeText("worm-sandwich!, (#)"))
    }

    @Test
    fun `drops every dash variant`() {
        // Hyphen-minus, en dash, em dash, non-breaking hyphen, minus sign.
        assertEquals("ab", textNormalizer.normalizeText("a-b"))
        assertEquals("ab", textNormalizer.normalizeText("a\u2013b"))
        assertEquals("ab", textNormalizer.normalizeText("a\u2014b"))
        assertEquals("ab", textNormalizer.normalizeText("a\u2011b"))
        assertEquals("ab", textNormalizer.normalizeText("a\u2212b"))
        assertEquals("motherinlaw", textNormalizer.normalizeText("mother-in-law"))
    }

    @Test
    fun `drops both apostrophe forms`() {
        assertEquals("dont", textNormalizer.normalizeText("don't"))
        assertEquals("dont", textNormalizer.normalizeText("don\u2019t"))
        assertEquals("its", textNormalizer.normalizeText("it's"))
    }

    @Test
    fun `drops sentence punctuation and brackets`() {
        assertEquals("helloworld", textNormalizer.normalizeText("Hello, World!"))
        assertEquals("abc", textNormalizer.normalizeText("a.b?c;"))
        assertEquals("abc", textNormalizer.normalizeText("(a)[b]{c}"))
        assertEquals("abc", textNormalizer.normalizeText("\"a\" \u201cb\u201d :c:"))
    }

    @Test
    fun `drops connectors and maths symbols`() {
        assertEquals("ab", textNormalizer.normalizeText("a_b"))
        assertEquals("ab", textNormalizer.normalizeText("a+b"))
        assertEquals("tag", textNormalizer.normalizeText("#tag"))
        assertEquals("ab", textNormalizer.normalizeText("a=b"))
    }

    @Test
    fun `treats upper and lower case as the same text`() {
        assertEquals(textNormalizer.normalizeText("LISTEN"), textNormalizer.normalizeText("listen"))
        assertEquals(textNormalizer.normalizeText("Dirty Room"), textNormalizer.normalizeText("dIRTY rOOM"))
        assertEquals("caf\u00e9", textNormalizer.normalizeText("CAF\u00c9"))
    }

    @Test
    fun `handles spaces, dashes, apostrophes and case together`() {
        assertEquals("motherinlaw", textNormalizer.normalizeText("  Mother - In - Law!  "))
        assertEquals(textNormalizer.normalizeText("Eleven plus two"), textNormalizer.normalizeText("eleven-plus-two"))
    }

    @Test
    fun `keeps digits`() {
        assertEquals("a1b2", textNormalizer.normalizeText("A 1 b-2"))
    }

    @Test
    fun `returns empty string for input with no letters or digits`() {
        assertEquals("", textNormalizer.normalizeText("  -- !! ~~ "))
    }

    @Test
    fun `returns empty string for empty input`() {
        assertEquals("", textNormalizer.normalizeText(""))
    }

    @Test
    fun `normalizes decomposed and precomposed forms identically`() {
        val precomposed = "café"       // e-acute as a single code point
        val decomposed = "café"      // plain e followed by a combining acute
        assertEquals(textNormalizer.normalizeText(precomposed), textNormalizer.normalizeText(decomposed))
    }

    @Test
    fun `preserves diacritics rather than folding them`() {
        assertEquals("café", textNormalizer.normalizeText("Café"))
    }

    @Test
    fun `does not expand ligatures`() {
        // Typographic and linguistic ligatures are single letters to the filter,
        // so they never match their multi-letter spellings.
        assertNotEquals(textNormalizer.normalizeText("\ufb01le"), textNormalizer.normalizeText("file"))
        assertNotEquals(textNormalizer.normalizeText("\u00e6on"), textNormalizer.normalizeText("aeon"))
        assertNotEquals(textNormalizer.normalizeText("\u00df"), textNormalizer.normalizeText("ss"))
    }

    @Test
    fun `lowercases with the root locale regardless of default locale`() {
        val previous = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr"))
            // Turkish locale lowercases 'I' to dotless 'ı'; ROOT gives 'i'.
            assertEquals("i", textNormalizer.normalizeText("I"))
        } finally {
            java.util.Locale.setDefault(previous)
        }
    }

    @Test
    fun `signature sorts the normalized characters`() {
        assertEquals("eilnst", textNormalizer.computeAnagramSignature("Listen!"))
    }

    @Test
    fun `signatures of anagrams are equal`() {
        assertEquals(textNormalizer.computeAnagramSignature("dormitory"), textNormalizer.computeAnagramSignature("dirty room"))
    }
}
