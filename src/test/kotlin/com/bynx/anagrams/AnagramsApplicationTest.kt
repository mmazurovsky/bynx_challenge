package com.bynx.anagrams

import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnagramsApplicationTest {

    /** A throwable that is an [Error] rather than an [Exception]. */
    private class UnrecoverableTestError : Error()

    /** A reader that fails the moment the CLI tries to read a command. */
    private class FailingReader(private val failure: () -> Throwable) : Reader() {
        override fun read(buffer: CharArray, offset: Int, length: Int): Int = throw failure()
        override fun close() = Unit
    }

    private fun guard(input: BufferedReader): Triple<Int, String, String> {
        val output = StringBuilder()
        val errorOutput = StringBuilder()
        val status = runGuarded(input, output, errorOutput)
        return Triple(status, output.toString(), errorOutput.toString())
    }

    @Test
    fun `a healthy session exits successfully and prints nothing to the error output`() {
        val (status, output, errorOutput) = guard("check listen silent\nexit".reader().buffered())
        assertEquals(0, status)
        assertTrue("true" in output)
        assertEquals("", errorOutput)
    }

    @Test
    fun `a broken input stream stops with a friendly message instead of a stack trace`() {
        val (status, _, errorOutput) =
            guard(BufferedReader(FailingReader { IOException("Stream closed") }))
        assertEquals(1, status)
        assertTrue("Something went wrong and Anagrams has to stop." in errorOutput)
        assertTrue("Detail: Stream closed" in errorOutput)
        assertTrue("com.bynx" !in errorOutput, "no stack trace reaches the user")
    }

    @Test
    fun `an Error also stops with the friendly message`() {
        val (status, _, errorOutput) = guard(BufferedReader(FailingReader { UnrecoverableTestError() }))
        assertEquals(1, status)
        assertTrue("Something went wrong and Anagrams has to stop." in errorOutput)
        assertTrue("Detail: UnrecoverableTestError" in errorOutput)
    }
}
