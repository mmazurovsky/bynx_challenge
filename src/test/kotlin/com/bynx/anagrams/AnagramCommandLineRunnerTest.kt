package com.bynx.anagrams

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AnagramCommandLineRunnerTest {

    /** Feeds [commands] to the CLI and returns the lines it printed, prompts stripped. */
    private fun run(vararg commands: String): List<String> =
        runWith(AnagramServiceImpl(), *commands)

    @Test
    fun `greets and exits`() {
        assertEquals(listOf("Anagrams. Type 'help' for commands."), run("exit"))
    }

    @Test
    fun `stops at end of input without an exit command`() {
        assertEquals(listOf("Anagrams. Type 'help' for commands."), run())
    }

    @Test
    fun `check reports true for anagrams`() {
        assertEquals("true", run("check listen silent", "exit").last())
    }

    @Test
    fun `check reports false for non anagrams`() {
        assertEquals("false", run("check hello world", "exit").last())
    }

    @Test
    fun `check accepts quoted phrases`() {
        assertEquals("true", run("""check "dormitory" "dirty room"""", "exit").last())
    }

    @Test
    fun `find reports an empty list when nothing matches`() {
        assertEquals("[]", run("find listen", "exit").last())
    }

    @Test
    fun `implements the scenario from the brief`() {
        val output = run(
            "check listen silent",
            "check listen hello",
            "check listen enlist",
            "find listen",
            "find silent",
            "find hello",
            "exit",
        )
        assertEquals(listOf("[silent, enlist]", "[listen, enlist]", "[]"), output.takeLast(3))
    }

    @Test
    fun `find does not record its own argument`() {
        val output = run("find listen", "find silent", "exit")
        assertEquals(listOf("[]", "[]"), output.takeLast(2))
    }

    @Test
    fun `rejects an unknown command`() {
        assertEquals(
            "Unknown command 'frobnicate'. Type 'help' for commands.",
            run("frobnicate a b", "exit").last(),
        )
    }

    @Test
    fun `rejects the wrong number of arguments`() {
        assertEquals("Usage: check <text> <text>", run("check listen", "exit").last())
        assertEquals("Usage: find <text>", run("find a b", "exit").last())
    }

    @Test
    fun `rejects a blank argument`() {
        assertEquals("""Arguments must not be blank.""", run("""check "" listen""", "exit").last())
    }

    @Test
    fun `rejects an unterminated quote`() {
        assertEquals("Unterminated quote.", run("""check "listen silent""", "exit").last())
    }

    @Test
    fun `ignores blank input lines`() {
        assertEquals(listOf("Anagrams. Type 'help' for commands."), run("", "   ", "exit"))
    }

    @Test
    fun `help lists every command`() {
        val output = run("help", "exit").joinToString("\n")
        assertTrue("check" in output)
        assertTrue("find" in output)
        assertTrue("help                 show this help" in output)
        assertTrue("exit" in output)
    }

    @Test
    fun `a rejected command records nothing`() {
        val output = run("check listen", "check listen silent", "find silent", "exit")
        assertEquals("[listen]", output.last())
    }

    @Test
    fun `prompts before every read`() {
        val output = StringBuilder()
        AnagramCommandLineRunner().run("\n   \nexit".reader().buffered(), output)
        assertEquals("Anagrams. Type 'help' for commands.\n> > > ", output.toString())
    }

    private class FailingAnagramService(private val failure: () -> Throwable) : AnagramService {
        override fun checkAnagrams(first: String, second: String): Boolean = throw failure()
        override fun findRecordedAnagramsOf(text: String): List<String> = throw failure()
    }

    private fun runWith(anagramService: AnagramService, vararg commands: String): List<String> {
        val output = StringBuilder()
        AnagramCommandLineRunner(anagramService)
            .run(commands.joinToString("\n").reader().buffered(), output)
        return output.toString()
            .split("\n")
            .map { it.replace(Regex("^(> )+"), "").trim() }
            .filter { it.isNotEmpty() }
    }

    @Test
    fun `a failing command is reported and the loop carries on`() {
        val output = runWith(
            FailingAnagramService { IllegalStateException("index unavailable") },
            "check listen silent",
            "help",
            "exit",
        )
        assertTrue("Something went wrong with that command. It has been skipped." in output)
        assertTrue("Detail: index unavailable" in output)
        assertTrue(
            "check <text> <text>  check whether two texts are anagrams" in output,
            "the loop kept running and printed the help text",
        )
    }

    @Test
    fun `a failure without a message falls back to the exception name`() {
        val output = runWith(
            FailingAnagramService { IllegalStateException() },
            "find listen",
            "exit",
        )
        assertTrue("Detail: IllegalStateException" in output)
        assertTrue(output.none { "null" in it }, "no 'Detail: null' is printed")
    }

    @Test
    fun `an Error is not swallowed by the per command guard`() {
        assertFailsWith<UnrecoverableTestError> {
            runWith(FailingAnagramService { UnrecoverableTestError() }, "check listen silent", "exit")
        }
    }
}
