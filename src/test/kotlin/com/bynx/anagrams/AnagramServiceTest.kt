package com.bynx.anagrams

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnagramServiceTest {

    // --- check: the anagram comparison -------------------------------------

    @Test
    fun `recognises single word anagrams`() {
        assertTrue(AnagramService().checkAnagrams("listen", "silent"))
    }

    @Test
    fun `recognises multi word anagrams from the Wikipedia article`() {
        assertTrue(AnagramService().checkAnagrams("dormitory", "dirty room"))
        assertTrue(AnagramService().checkAnagrams("the eyes", "they see"))
    }

    @Test
    fun `ignores case and punctuation`() {
        assertTrue(AnagramService().checkAnagrams("A decimal point!", "I'm a dot in place."))
    }

    @Test
    fun `rejects texts with different letters`() {
        assertFalse(AnagramService().checkAnagrams("listen", "listens"))
        assertFalse(AnagramService().checkAnagrams("hello", "world"))
    }

    @Test
    fun `a text is not an anagram of itself`() {
        assertFalse(AnagramService().checkAnagrams("listen", "listen"))
    }

    @Test
    fun `a text is not an anagram of a differently cased or punctuated copy`() {
        assertFalse(AnagramService().checkAnagrams("Listen", "listen!"))
    }

    @Test
    fun `texts that normalize to nothing are not anagrams of each other`() {
        assertFalse(AnagramService().checkAnagrams("!!!", "???"))
    }

    @Test
    fun `digits participate in the comparison`() {
        assertTrue(AnagramService().checkAnagrams("a1", "1a"))
        assertFalse(AnagramService().checkAnagrams("a1", "a2"))
    }

    // --- check feeds the history -------------------------------------------

    @Test
    fun `check records both of its texts`() {
        val anagramService = AnagramService()
        anagramService.checkAnagrams("silent", "hello")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("listen"))
        assertEquals(listOf("hello"), anagramService.findRecordedAnagramsOf("olleh"))
    }

    @Test
    fun `check records texts that are not anagrams of each other`() {
        val anagramService = AnagramService()
        anagramService.checkAnagrams("listen", "world")
        assertEquals(listOf("listen"), anagramService.findRecordedAnagramsOf("silent"))
    }

    // --- findRecordedAnagramsOf: the history query ---------------------------------

    @Test
    fun `returns nothing when no texts were recorded`() {
        assertEquals(emptyList(), AnagramService().findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `returns nothing when no recorded text is an anagram`() {
        val anagramService = AnagramService()
        anagramService.recordText("hello")
        anagramService.recordText("world")
        assertEquals(emptyList(), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `returns recorded anagrams in insertion order`() {
        val anagramService = AnagramService()
        anagramService.recordText("silent")
        anagramService.recordText("enlist")
        anagramService.recordText("tinsel")
        assertEquals(listOf("silent", "enlist", "tinsel"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `excludes the query text itself`() {
        val anagramService = AnagramService()
        anagramService.recordText("listen")
        anagramService.recordText("silent")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `excludes recorded texts that differ only by case or punctuation`() {
        val anagramService = AnagramService()
        anagramService.recordText("Listen!")
        anagramService.recordText("silent")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `records the original text rather than its normalized form`() {
        val anagramService = AnagramService()
        anagramService.recordText("Dirty Room")
        assertEquals(listOf("Dirty Room"), anagramService.findRecordedAnagramsOf("dormitory"))
    }

    @Test
    fun `recording the same text twice yields one entry`() {
        val anagramService = AnagramService()
        anagramService.recordText("silent")
        anagramService.recordText("silent")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `querying does not record the query`() {
        val anagramService = AnagramService()
        anagramService.recordText("silent")
        anagramService.findRecordedAnagramsOf("listen")
        assertEquals(listOf("silent"), anagramService.findRecordedAnagramsOf("enlist"))
    }

    @Test
    fun `lists every distinct spelling of the same text`() {
        val anagramService = AnagramService()
        anagramService.recordText("silent")
        anagramService.recordText("Silent?")
        anagramService.recordText("enlist")
        assertEquals(listOf("silent", "Silent?", "enlist"), anagramService.findRecordedAnagramsOf("listen"))
    }

    @Test
    fun `matches the scenario from the brief`() {
        val a = "listen"
        val b = "silent"
        val c = "hello"
        val d = "enlist"

        val anagramService = AnagramService()
        listOf(a, b, a, c, a, d).forEach(anagramService::recordText)

        assertEquals(listOf(b, d), anagramService.findRecordedAnagramsOf(a))
        assertEquals(listOf(a, d), anagramService.findRecordedAnagramsOf(b))
        assertEquals(emptyList(), anagramService.findRecordedAnagramsOf(c))
    }
}
