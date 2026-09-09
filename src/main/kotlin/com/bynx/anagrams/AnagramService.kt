package com.bynx.anagrams

/**
 * Both features of the program, over one shared memory.
 *
 * [checkAnagrams] answers whether two texts are anagrams, and
 * [findRecordedAnagramsOf] answers which earlier texts are anagrams of a given
 * one. They are one class because they are one thing: the second is defined
 * over the inputs of the first, so [checkAnagrams] records what it is asked
 * about and no caller can forget to.
 *
 * Texts are bucketed by their anagram signature, so a lookup costs one hash
 * lookup no matter how many texts were recorded, and each bucket already
 * contains exactly the candidates. Buckets preserve insertion order, so
 * results are reported in the order the texts were entered.
 *
 * The map is the program's only mutable state, and it lives only as long as
 * the instance — nothing is persisted between runs. Not thread-safe: the
 * program is a single-threaded REPL.
 */
class AnagramService(
    private val textNormalizer: TextNormalizer = TextNormalizer(),
) {

    private val textsBySignature = mutableMapOf<String, MutableSet<String>>()

    /**
     * Remembers [text]. Recording a byte-identical text again changes nothing;
     * a different spelling of the same text (by the program's normalized
     * comparison) is kept as a separate entry.
     */
    fun recordText(text: String) {
        textsBySignature
            .getOrPut(textNormalizer.computeAnagramSignature(text), ::linkedSetOf)
            .add(text)
    }

    /**
     * True when [first] and [second] use exactly the same letters and are not
     * the same text. Both are recorded, whatever the answer.
     *
     * The English Wikipedia article defines an anagram as a rearrangement
     * forming "a different word or phrase", so a text is not an anagram of
     * itself. Equality is judged after normalization, making "Listen" and
     * "listen!" the same text.
     */
    fun checkAnagrams(first: String, second: String): Boolean {
        recordText(first)
        recordText(second)
        return textNormalizer.normalizeText(first) != textNormalizer.normalizeText(second) &&
            textNormalizer.computeAnagramSignature(first) ==
            textNormalizer.computeAnagramSignature(second)
    }

    /**
     * The recorded texts that are anagrams of [text], oldest first.
     *
     * The text itself is excluded — an anagram is a *different* word or phrase
     * — and so is any recorded text that differs from it only in case or
     * punctuation, since normalization makes those the same text.
     *
     * This is a query: it does not record [text].
     */
    fun findRecordedAnagramsOf(text: String): List<String> {
        val queried = textNormalizer.normalizeText(text)
        return textsBySignature[textNormalizer.computeAnagramSignature(text)]
            .orEmpty()
            .filterNot { textNormalizer.normalizeText(it) == queried }
    }
}
