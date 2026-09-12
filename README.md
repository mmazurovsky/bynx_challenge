# Anagram CLI

An interactive command-line program that checks whether two texts are anagrams of
each other and recalls previously entered anagrams of a text.

## Run it

```
./gradlew run
```

That starts the program interactively: it prints a `>` prompt and waits for you to type a command.

Piping commands in instead of typing them gives:

```
printf 'check listen silent\ncheck "dormitory" "dirty room"\nfind listen\nexit\n' | ./gradlew --quiet --console=plain run
```

```
Anagrams. Type 'help' for commands.
> true
> true
> [silent]
> 
```

In a piped run the commands are not echoed, so only the answers appear.

Quote a text that contains spaces, as `"dirty room"` above.

## Design

Four units, in a straight dependency line:

- `TextNormalizer.kt` — reduces a text to the form comparison is defined on: NFC,
  lowercased with `Locale.ROOT`, letters and digits only, plus the sorted signature of
  that form.
- `AnagramService.kt` — both features over one shared map, the program's only mutable
  state. They live behind one type because recording is reachable only through
  `checkAnagrams`, so a caller that checks two texts cannot forget to record them.
- `AnagramCommandLineRunner.kt` — the read-eval-print loop: tokenizes command lines
  (honouring double quotes), validates arguments, dispatches to the units above and
  reports failures. It holds no anagram logic of its own.
- `AnagramsApplication.kt` — the entry point; wires the runner to `System.in`,
  `System.out` and `System.err`, and wraps it in `runGuarded`, the top-level guard that
  reports a fatal failure on `System.err` and maps it to exit status 1.

History is indexed by signature: a map from signature to an insertion-ordered set
(`LinkedHashSet`) of the texts recorded under it. The signature is a plain string, so it
doubles as the map key, and `find` costs one hash lookup — O(1) in history size rather
than a scan of every past entry.

## The anagram rule as implemented

- Case-insensitive.
- Punctuation and whitespace are ignored.
- Phrases are supported (`"dormitory"` vs. `"dirty room"`).
- Diacritics are not folded: `café` and `cafe` are different texts. This holds only
  across the precomposed range — a combining mark with no precomposed form is dropped
  (`q́` → `q`).
- A text is not an anagram of itself.
- Letters and decimal digits count, so `abc123` and `321cba` are anagrams.

## Trade-offs and what was left out

- **No persistence.** History lives only for the process's lifetime; the brief scopes
  the program to a single interactive session, so there is no file or database to load
  and save.
- **No thread safety.** `AnagramServiceImpl` holds a plain mutable map with no locking.
  The CLI is a single-threaded REPL, so nothing is ever accessed concurrently.
- **No third-party runtime dependencies.** Only the Kotlin and JVM standard libraries
  are used.
- **No escaping inside quoted arguments.** A text containing a double quote cannot be
  entered; the tokenizer treats `"` purely as a toggle for whether whitespace is
  significant.
