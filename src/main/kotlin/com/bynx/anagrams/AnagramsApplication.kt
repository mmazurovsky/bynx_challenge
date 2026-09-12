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

/** Runs the CLI and returns the process exit status: 0 normally, 1 if the session failed. */
fun runGuarded(input: BufferedReader, output: Appendable, errorOutput: Appendable): Int =
    try {
        AnagramCommandLineRunner().run(input, output)
        0
    } catch (throwable: Throwable) {
        errorOutput.appendLine("Something went wrong and Anagrams has to stop.")
        errorOutput.appendLine("Detail: ${describeFailure(throwable)}")
        1
    }
