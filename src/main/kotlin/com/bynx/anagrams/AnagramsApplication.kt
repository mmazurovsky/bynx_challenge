package com.bynx.anagrams

import java.io.BufferedReader
import kotlin.system.exitProcess

fun main() {
    exitProcess(
        runGuarded(
            input = System.`in`.bufferedReader(),
            output = System.out,
            errorOutput = System.err,
        ),
    )
}

/**
 * Runs the CLI and returns the process exit status: 0 normally, 1 if the
 * session ended in a failure.
 *
 * This is the last line of defence. [AnagramCommandLineRunner] already reports
 * and skips a command that fails, so anything arriving here has broken the
 * loop itself — a dead input stream, or an [Error] the loop deliberately did
 * not catch. There is no working session left to return to, so the program
 * says so in one plain line and stops instead of printing a stack trace.
 *
 * The streams are parameters so that this guard is testable.
 */
fun runGuarded(input: BufferedReader, output: Appendable, errorOutput: Appendable): Int =
    try {
        AnagramCommandLineRunner().run(input, output)
        0
    } catch (throwable: Throwable) {
        errorOutput.appendLine("Something went wrong and Anagrams has to stop.")
        errorOutput.appendLine("Detail: ${describeFailure(throwable)}")
        1
    }
