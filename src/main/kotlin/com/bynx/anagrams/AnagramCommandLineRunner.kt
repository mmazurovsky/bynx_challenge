package com.bynx.anagrams

import java.io.BufferedReader

private const val BANNER = "Anagrams. Type 'help' for commands."

private val HELP = """
    check <text> <text>  check whether two texts are anagrams
    find <text>          list previously entered anagrams of a text
    help                 show this help
    exit                 quit

    Quote texts that contain spaces: check "dormitory" "dirty room"
""".trimIndent()

/**
 * The read-eval-print loop: reads commands, dispatches them to
 * [AnagramService] and prints the answers.
 */
class AnagramCommandLineRunner(
    private val anagramService: AnagramService = AnagramService(),
) {

    /**
     * Runs the loop until [input] is exhausted or the user exits.
     *
     * Input and output are parameters rather than `System.in`/`System.out` so
     * the whole loop can be exercised in tests.
     *
     * A command that fails unexpectedly is reported and skipped, leaving the
     * session and its history intact. Only [Exception] is caught: an [Error]
     * means the JVM itself is in trouble, and answering further questions
     * would be dishonest, so it is left to the guard in [runGuarded].
     */
    fun run(input: BufferedReader, output: Appendable) {
        output.appendLine(BANNER)

        while (true) {
            output.append("> ")
            val line = input.readLine() ?: return
            if (line.isBlank()) continue

            val arguments = try {
                parseCommandLine(line)
            } catch (_: UnterminatedQuoteException) {
                output.appendLine("Unterminated quote.")
                continue
            }

            val command = arguments.first()
            val operands = arguments.drop(1)

            try {
                when (command) {
                    "exit" -> return
                    "help" -> output.appendLine(HELP)
                    "check" -> output.appendLine(handleCheckCommand(operands))
                    "find" -> output.appendLine(handleFindCommand(operands))
                    else -> output.appendLine("Unknown command '$command'. Type 'help' for commands.")
                }
            } catch (exception: Exception) {
                output.appendLine("Something went wrong with that command. It has been skipped.")
                output.appendLine("Detail: ${describeFailure(exception)}")
            }
        }
    }

    private fun handleCheckCommand(operands: List<String>): String {
        if (operands.size != 2) return "Usage: check <text> <text>"
        val (first, second) = operands
        if (first.isBlank() || second.isBlank()) return "Arguments must not be blank."

        return anagramService.checkAnagrams(first, second).toString()
    }

    private fun handleFindCommand(operands: List<String>): String {
        if (operands.size != 1) return "Usage: find <text>"
        val text = operands.single()
        if (text.isBlank()) return "Arguments must not be blank."

        return anagramService.findRecordedAnagramsOf(text).joinToString(prefix = "[", postfix = "]")
    }
}

private class UnterminatedQuoteException : Exception()

/**
 * A single plain line describing [throwable], for showing to the user.
 *
 * Exception messages are often absent, so the class name is used as a
 * fallback rather than printing "null". Stack traces are never shown: they
 * mean nothing to someone typing words into a prompt.
 */
internal fun describeFailure(throwable: Throwable): String =
    throwable.message?.takeIf(String::isNotBlank) ?: throwable::class.simpleName ?: "unknown error"

/**
 * Splits a command line into a command and its arguments.
 *
 * Tokens are separated by whitespace; a token may instead be wrapped in double
 * quotes so that it can contain spaces. Escapes are not supported — a text
 * containing a double quote cannot be entered, which is an acceptable limit
 * for a REPL about letters.
 */
private fun parseCommandLine(line: String): List<String> {
    val tokens = mutableListOf<String>()
    val token = StringBuilder()
    var quoted = false
    var started = false

    for (character in line) {
        when {
            character == '"' -> {
                quoted = !quoted
                started = true
            }
            quoted || !character.isWhitespace() -> {
                token.append(character)
                started = true
            }
            started -> {
                tokens.add(token.toString())
                token.clear()
                started = false
            }
        }
    }
    if (quoted) throw UnterminatedQuoteException()
    if (started) tokens.add(token.toString())

    return tokens
}
