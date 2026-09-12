package com.bynx.anagrams

/** Checks anagrams and remembers every text it is asked about. */
interface AnagramService {

    /** True when [first] and [second] use the same letters but are not the same text; records both. */
    fun checkAnagrams(first: String, second: String): Boolean

    /** The recorded texts that are anagrams of [text], oldest first; does not record [text]. */
    fun findRecordedAnagramsOf(text: String): List<String>
}

/** In-memory [AnagramService], bucketing texts by anagram signature. Not thread-safe. */
class AnagramServiceImpl(
    private val textNormalizer: TextNormalizer = TextNormalizerImpl(),
) : AnagramService {

    private val textsBySignature = mutableMapOf<String, MutableSet<String>>()

    private fun record(text: String, normalizedText: String) {
        textsBySignature
            .getOrPut(textNormalizer.signatureOfNormalized(normalizedText), ::linkedSetOf)
            .add(text)
    }

    override fun checkAnagrams(first: String, second: String): Boolean {
        val normalizedFirst = textNormalizer.normalizeText(first)
        val normalizedSecond = textNormalizer.normalizeText(second)
        record(first, normalizedFirst)
        record(second, normalizedSecond)
        return normalizedFirst != normalizedSecond &&
            textNormalizer.signatureOfNormalized(normalizedFirst) ==
            textNormalizer.signatureOfNormalized(normalizedSecond)
    }

    override fun findRecordedAnagramsOf(text: String): List<String> {
        val queried = textNormalizer.normalizeText(text)
        return textsBySignature[textNormalizer.signatureOfNormalized(queried)]
            .orEmpty()
            .filterNot { textNormalizer.normalizeText(it) == queried }
    }
}
