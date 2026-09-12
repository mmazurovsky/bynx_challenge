package com.bynx.anagrams

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnagramServiceTest {

    /** Records [texts] so they can be found later. */
    private fun AnagramService.record(vararg texts: String) = texts.forEach { checkAnagrams(it, it) }

    @Test
    fun `recognises single word anagrams`() {
        assertTrue(AnagramServiceImpl().checkAnagrams("listen", "silent"))
    }

    @Test
    fun `recognises multi word anagrams from the Wikipedia article`() {
        assertTrue(AnagramServiceImpl().checkAnagrams("dormitory", "dirty room"))
        assertTrue(AnagramServiceImpl().checkAnagrams("the eyes", "they see"))
    }

    @Test
    fun `ignores case and punctuation`() {
        assertTrue(AnagramServiceImpl().checkAnagrams("A decimal point!", "I'm a dot in place."))
    }

    @Test
    fun `rejects texts with different letters`() {
        assertFalse(AnagramServiceImpl().checkAnagrams("listen", "listens"))
        assertFalse(AnagramServiceImpl().checkAnagrams("hello", "world"))
    }

    @Test
    fun `a text is not an anagram of itself`() {
        assertFalse(AnagramServiceImpl().checkAnagrams("listen", "listen"))
    }

    @Test
    fun `a text is not an anagram of a differently cased or punctuated copy`() {
        assertFalse(AnagramServiceImpl().checkAnagrams("Listen", "listen!"))
    }

    @Test
    fun `texts that normalize to nothing are not anagrams of each other`() {
        assertFalse(AnagramServiceImpl().checkAnagrams("!!!", "???"))
    }

    @Test
    fun `digits participate in the comparison`() {
        assertTrue(AnagramServiceImpl().checkAnagrams("a1", "1a"))
        assertFalse(AnagramServiceImpl().checkAnagrams("a1", "a2"))
    }

    @Test
    fun `check records both of its texts`() {
        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.checkAnagrams("silent", "hello")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("listen"))
        assertEquals(listOf("hello"), anagramService.findRecordedAnagramsOf("olleh"))
    }

    @Test
    fun `returns nothing when no texts were recorded`() {
        assertEquals(emptyList(), AnagramServiceImpl().findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `returns nothing when no recorded text is an anagram`() {
        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.record("hello", "world")
        assertEquals(emptyList(), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `returns recorded anagrams in insertion order`() {
        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.record("silent", "enlist", "tinsel")
        assertEquals(listOf("silent", "enlist", "tinsel"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `excludes the query text itself`() {
        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.record("listen", "silent")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `excludes recorded texts that differ only by case or punctuation`() {
        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.record("Listen!", "silent")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `recording the same text twice yields one entry`() {
        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.checkAnagrams("silent", "hello")
        anagramService.checkAnagrams("silent", "world")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `querying does not record the query`() {
        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.record("silent")
        anagramService.findRecordedAnagramsOf("listen")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("enlist"))
    }

    @Test
    fun `lists every distinct spelling of the same text`() {
        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.record("silent", "Silent?", "enlist")
        assertEquals(listOf("silent", "Silent?", "enlist"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `matches the scenario from the brief`() {
        val a = "listen"
        val b = "silent"
        val c = "hello"
        val d = "enlist"

        val anagramService: AnagramService = AnagramServiceImpl()
        anagramService.record(a, b, a, c, a, d)

        assertEquals(listOf(b, d), anagramService.findRecordedAnagramsOf(a))
        assertEquals(listOf(a, d), anagramService.findRecordedAnagramsOf(b))
        assertEquals(emptyList(), anagramService.findRecordedAnagramsOf(c))
    }
}
