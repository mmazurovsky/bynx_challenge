package com.bynx.anagrams

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TextNormalizerTest {

    private val textNormalizer: TextNormalizer = TextNormalizerImpl()

    @Test
    fun `drops whitespace`() {
        assertEquals("dirtyroom", textNormalizer.normalizeText(" dirty  room\t"))
    }

    @Test
    fun `drops every character that is not a letter or digit`() {
        // The dash cases below are, in order: hyphen-minus, en dash, em dash,
        // non-breaking hyphen and minus sign.
        val cases = listOf(
            "worm-sandwich!, (#)" to "wormsandwich",
            "a-b" to "ab",
            "a\u2013b" to "ab",
            "a\u2014b" to "ab",
            "a\u2011b" to "ab",
            "a\u2212b" to "ab",
            "mother-in-law" to "motherinlaw",
            "don't" to "dont",
            "don\u2019t" to "dont",
            "it's" to "its",
            "Hello, World!" to "helloworld",
            "a.b?c;" to "abc",
            "(a)[b]{c}" to "abc",
            "\"a\" \u201cb\u201d :c:" to "abc",
            "a_b" to "ab",
            "a+b" to "ab",
            "#tag" to "tag",
            "a=b" to "ab",
            "  -- !! ~~ " to "",
        )

        cases.forEach { (input, expected) ->
            assertEquals(expected, textNormalizer.normalizeText(input), "normalizing <$input>")
        }
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
        assertEquals("eilnst", textNormalizer.signatureOfNormalized(textNormalizer.normalizeText("Listen!")))
    }
}
