# Anagram CLI

An interactive command-line program that checks whether two texts are anagrams of each other and recalls previously entered anagrams of a text.

## Run it

```
./gradlew run
```

That starts the program interactively: it prints a `>` prompt and waits for you to type a command.

The block below is the real output of piping the four commands straight into that same program, byte-for-byte:

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

This is a piped run, so the commands themselves are never echoed — only their answers appear, each on the line after its `>` prompt. Type the same four commands interactively at `./gradlew run` instead, and each command echoes back after its prompt, with its answer on the line below it.

Quote a text that contains spaces, as `"dirty room"` above.

## Design

Four units, in a straight dependency line:

- `TextNormalizer.kt` — `TextNormalizer` reduces a text to the form comparison is defined on: `normalizeText(text)` gives the NFC-normalized text, lowercased with `Locale.ROOT`, letters and digits only, and `computeAnagramSignature(text)` gives those characters sorted.
- `AnagramService.kt` — `AnagramService` holds both features over one shared memory, and the program's only mutable state. `checkAnagrams(first, second)` answers whether two texts are anagrams (same signature, different normalized text) and records both; `findRecordedAnagramsOf(text)` answers which earlier texts are anagrams of it; `recordText(text)` remembers one. They are one class because the second feature is defined over the first feature's inputs — so recording is part of `checkAnagrams`, not something a caller can forget.
- `AnagramCommandLineRunner.kt` — the read-eval-print loop: parses command lines and calls the units above; no logic of its own.
- `AnagramsApplication.kt` — the entry point; wires the runner to `System.in`/`System.out`.

History is indexed by signature: a map from signature to an insertion-ordered set (`LinkedHashSet`) of the texts recorded under it. A lookup costs one hash lookup no matter how many texts were recorded, so `find` stays O(1) in history size rather than scanning every past entry. The signature is a plain string, so it doubles as a map key and is readable as-is in a debugger — no separate frequency map to decode.

## The anagram rule as implemented

- Case-insensitive.
- Punctuation and whitespace are ignored.
- Phrases are supported (`"dormitory"` vs. `"dirty room"`).
- Diacritics are not folded: `café` and `cafe` are different texts.
- A text is not an anagram of itself.

### Which characters count

Normalization does not blacklist unwanted characters; it whitelists the wanted ones. `normalize` composes to NFC, lowercases with `Locale.ROOT`, then keeps only what `Char.isLetterOrDigit` accepts. Everything else falls away without needing to be enumerated, which is why no dash, quote or bracket variant has to be listed anywhere in the code.

**Dropped.** Whitespace of every kind; all dash variants (`-` hyphen-minus, `—` em dash, `‑` non-breaking hyphen); both apostrophe forms (`'` and `’`); punctuation (`.,!?;:"()[]`); and symbols and connectors (`+`, `#`, `_`). So `mother-in-law` → `motherinlaw`, `dirty room` → `dirtyroom`, `don’t` → `dont`, `Hello, World!` → `helloworld`. This is what makes the classic phrase anagrams work: `dormitory` and `dirty room` match only because the space is not a character in the comparison, and once a space carries no weight there is no principled reason a hyphen should.

**Kept.** Letters of any script, and digits. Digits counting is a deliberate consequence of the whitelist rather than a requirement of the brief: `abc123` and `321cba` are anagrams of each other. Narrowing to `Char.isLetter` would be a one-word change if numerals should not participate.

**Kept as single letters: ligatures.** `ﬁ`, `æ`, `œ` and `ß` are each one letter to the filter and are never expanded, so `ﬁle` does not match `file`, `æon` does not match `aeon`, and `ß` does not match `ss`. This is the same choice as not folding diacritics — the character the text actually contains is the character that is compared — and it is what the Camden passage quoted in the Wikipedia article was already arguing about in the 17th century, when loose anagrammatists thought it "no injury to use E for Æ".

**Dropped despite being numeric: non-decimal number characters.** `Char.isLetterOrDigit` accepts only decimal digits (Unicode category `Nd`), so numeric characters in other categories are filtered out entirely: the Roman numeral `Ⅻ` (`Nl`) and the fraction `½` (`No`) both normalize to the empty string, and neither matches its spelled-out form.

**Partially kept: combining marks.** The claim that diacritics are not folded holds only for characters Unicode has assigned a precomposed form. NFC turns `e` + combining acute into the single character `é`, a letter, which survives the filter. But `q` + combining acute has no precomposed form, so the mark stays a separate character of category `Mn` — not a letter — and is dropped: `q́` → `q`, and `ą́` → `ą`. Accent handling is therefore uniform only across the precomposed range. Making it uniform everywhere would mean normalizing to NFD and admitting non-spacing marks to the filter, so that marks are consistently significant regardless of what Unicode happens to have precomposed.

## Trade-offs and what was left out

- **No persistence.** History lives only for the process's lifetime; the brief scopes the program to a single interactive session, so there is no file or database to load and save.
- **No thread safety.** `AnagramService` is a plain mutable map with no locking. The CLI is a single-threaded REPL, so nothing is ever accessed concurrently.
- **No third-party runtime dependencies.** Only the Kotlin and JVM standard libraries are used.
- **No accent folding.** Diacritics are preserved rather than stripped, so texts that a looser reading might treat as the same letters are kept distinct — with the precomposed-form caveat noted under "Which characters count" above.
- **No escaping inside quoted arguments.** A text containing a double quote cannot be entered; the tokenizer treats `"` purely as a toggle for whether whitespace is significant.
- **Basic Multilingual Plane only.** Normalization filters with `Char.isLetterOrDigit`, a UTF-16 code-unit predicate. A character outside the Basic Multilingual Plane (e.g. Gothic `𐌰`) is stored as a surrogate pair, and neither half is a letter or digit, so it is silently dropped. This never produces a false match — everything supplementary collapses to the same empty signature consistently — but such texts are not meaningfully compared.
