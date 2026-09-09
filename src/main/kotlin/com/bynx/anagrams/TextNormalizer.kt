package com.bynx.anagrams

import java.text.Normalizer as UnicodeNormalizer
import java.util.Locale

/**
 * Reduces a text to the form the anagram comparison is defined on:
 * Unicode-normalized, lowercase, letters and digits only.
 *
 * Stateless, so one instance can be shared by every collaborator.
 */
class TextNormalizer {

    /**
     * [text] with case, spacing and punctuation removed.
     *
     * Case, spacing and punctuation are irrelevant to the Wikipedia definition
     * of an anagram ("dormitory" is an anagram of "dirty room"), so they are
     * removed here rather than being special-cased at every call site.
     *
     * Diacritics are deliberately *not* folded: "café" and "cafe" are
     * different texts. NFC composition only ensures the two spellings of a
     * single accented character compare equal.
     *
     * Lowercasing uses [Locale.ROOT] so the program's answers do not depend on
     * the machine's locale (a Turkish default locale would otherwise map 'I'
     * to 'ı').
     */
    fun normalizeText(text: String): String =
        UnicodeNormalizer.normalize(text, UnicodeNormalizer.Form.NFC)
            .lowercase(Locale.ROOT)
            .filter(Char::isLetterOrDigit)

    /**
     * The canonical form two anagrams share: the normalized characters, sorted.
     *
     * Sorting costs O(k log k) in the length of the text and yields a plain
     * string, which makes it usable directly as a map key when indexing past
     * inputs — and readable in a debugger, unlike a frequency map.
     */
    fun computeAnagramSignature(text: String): String =
        normalizeText(text).toCharArray().sorted().joinToString("")
}
