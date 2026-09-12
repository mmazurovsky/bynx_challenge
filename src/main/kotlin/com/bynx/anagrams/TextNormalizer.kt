package com.bynx.anagrams

import java.text.Normalizer as UnicodeNormalizer
import java.util.Locale

/** Reduces a text to the form the anagram comparison is defined on. Stateless. */
interface TextNormalizer {

    /** [text] with case, spacing and punctuation removed; diacritics are kept. */
    fun normalizeText(text: String): String

    /**
     * The canonical form two anagrams share: the characters of [normalizedText], sorted.
     * [normalizedText] must already have come from [normalizeText].
     */
    fun signatureOfNormalized(normalizedText: String): String
}

/** [TextNormalizer] over NFC normalization and `Locale.ROOT` lowercasing. */
class TextNormalizerImpl : TextNormalizer {

    override fun normalizeText(text: String): String =
        UnicodeNormalizer.normalize(text, UnicodeNormalizer.Form.NFC)
            .lowercase(Locale.ROOT)
            .filter(Char::isLetterOrDigit)

    override fun signatureOfNormalized(normalizedText: String): String =
        normalizedText.toCharArray().sorted().joinToString("")
}
