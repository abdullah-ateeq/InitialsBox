package com.example.initialsbox.engine

import com.example.initialsbox.model.CaseStyle
import com.example.initialsbox.model.CharacterPosition
import com.example.initialsbox.model.HyphenMode
import com.example.initialsbox.model.InitialsOptions
import com.example.initialsbox.model.InitialsResult
import com.example.initialsbox.model.InitialsSortMode
import com.example.initialsbox.model.InitialsStyle
import com.example.initialsbox.model.NumberMode
import com.example.initialsbox.model.SmallWordMode
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object InitialsGenerator {

    val BASE_SMALL_WORDS: Set<String> = setOf(
        "a", "an", "and", "as", "at", "by", "for", "from",
        "in", "into", "of", "on", "or", "the", "to", "with"
    )

    val NAME_PARTICLES: Set<String> = setOf(
        "de", "da", "del", "della", "di", "van", "von", "bin", "ibn", "al"
    )

    /**
     * Generates initials for a single input string.
     * In single mode, newlines are treated as whitespace.
     */
    fun generate(input: String, options: InitialsOptions): InitialsResult {
        if (input.isBlank()) {
            return InitialsResult(
                input = input,
                initials = "",
                originalWordCount = 0,
                usedWordCount = 0,
                characterCount = 0,
                wordTokens = emptyList()
            )
        }

        val allTokens = tokenize(input, options)
        val originalWordCount = allTokens.size

        if (allTokens.isEmpty()) {
            return InitialsResult(
                input = input,
                initials = "",
                originalWordCount = 0,
                usedWordCount = 0,
                characterCount = 0,
                wordTokens = emptyList()
            )
        }

        // Apply small-word filtering
        val filteredTokens = filterSmallWords(allTokens, options)

        // Select words based on InitialsStyle
        val selectedTokens = selectWordsByStyle(filteredTokens, options)

        // Extract initials from each selected token
        val extractedInitials = extractInitials(selectedTokens, options)

        // Format case
        val casedInitials = applyCase(extractedInitials, options)

        // Format separators and periods
        val formattedInitials = formatSeparatorsAndPeriods(casedInitials, options)

        // Apply prefix and suffix
        val finalInitials = if (formattedInitials.isNotEmpty()) {
            "${options.prefix}$formattedInitials${options.suffix}"
        } else {
            ""
        }

        val rawCharCount = casedInitials.sumOf { it.codePointCount(0, it.length) }

        return InitialsResult(
            input = input,
            initials = finalInitials,
            originalWordCount = originalWordCount,
            usedWordCount = selectedTokens.size,
            characterCount = rawCharCount,
            wordTokens = allTokens
        )
    }

    /**
     * Batch processing for multiple lines.
     */
    fun generateBatch(input: String, options: InitialsOptions): List<InitialsResult> {
        if (input.isBlank()) return emptyList()

        var rawLines = input.lines().map { it.trim() }

        if (options.ignoreEmptyLines) {
            rawLines = rawLines.filter { it.isNotBlank() }
        }

        if (options.removeDuplicateInputs) {
            val seenInputs = mutableSetOf<String>()
            val distinctLines = mutableListOf<String>()
            for (line in rawLines) {
                if (seenInputs.add(line)) {
                    distinctLines.add(line)
                }
            }
            rawLines = distinctLines
        }

        var results = rawLines.map { line ->
            generate(line, options)
        }

        if (options.removeDuplicateResults) {
            val seenResults = mutableSetOf<String>()
            results = results.filter { result ->
                seenResults.add(result.initials)
            }
        }

        return sortResults(results, options.sortMode)
    }

    fun sortResults(results: List<InitialsResult>, sortMode: InitialsSortMode): List<InitialsResult> {
        return when (sortMode) {
            InitialsSortMode.INPUT_ORDER -> results
            InitialsSortMode.INPUT_ALPHABETICAL -> results.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.input })
            InitialsSortMode.INITIALS_ALPHABETICAL -> results.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.initials })
            InitialsSortMode.REVERSE_ALPHABETICAL -> results.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.initials })
            InitialsSortMode.LONGEST_FIRST -> results.sortedWith(compareByDescending<InitialsResult> { it.input.length }.thenBy { it.input })
            InitialsSortMode.SHORTEST_FIRST -> results.sortedWith(compareBy<InitialsResult> { it.input.length }.thenBy { it.input })
        }
    }

    /**
     * Unicode and symbol-aware tokenization.
     */
    fun tokenize(text: String, options: InitialsOptions): List<String> {
        // Build character buffer / separator substitution
        val sb = StringBuilder()
        var i = 0
        val len = text.length

        while (i < len) {
            val cp = text.codePointAt(i)
            val charCount = Character.charCount(cp)

            // Check if code point is an emoji or symbol that shouldn't be initials
            val isEmoji = isEmojiOrPictograph(cp)

            if (isEmoji) {
                sb.append(' ')
                i += charCount
                continue
            }

            val ch = text[i]

            // Handle separators based on settings
            if (Character.isWhitespace(cp) || cp == 0x00A0 || (cp in 0x2000..0x200B) || cp == 0x202F || cp == 0x205F || cp == 0x3000) {
                sb.append(' ')
            } else if (options.hyphenMode == HyphenMode.SPLIT_WORDS && (ch == '-' || ch == '—' || ch == '–')) {
                sb.append(' ')
            } else if (options.symbolAsSeparator && (ch == '_' || ch == '/' || ch == '\\' || ch == '|' || ch == '~' || ch == '+' || ch == '=' || ch == '#' || ch == '^' || ch == '%' || ch == '$' || ch == '*')) {
                sb.append(' ')
            } else if (options.parseEmailComponents && (ch == '@' || ch == '.')) {
                sb.append(' ')
            } else {
                sb.appendCodePoint(cp)
            }

            i += charCount
        }

        // Split into candidate tokens
        val rawCandidates = sb.toString().split("\\s+".toRegex()).filter { it.isNotBlank() }

        // Strip surrounding punctuation for each candidate while preserving internal apostrophes or hyphens
        val tokens = mutableListOf<String>()
        for (candidate in rawCandidates) {
            val cleaned = stripSurroundingPunctuation(candidate)
            if (cleaned.isNotBlank()) {
                // If pure numbers and numbers are disabled, skip
                if (!options.includeNumbers && cleaned.all { it.isDigit() }) {
                    continue
                }
                tokens.add(cleaned)
            }
        }

        return tokens
    }

    private fun stripSurroundingPunctuation(token: String): String {
        var start = 0
        var end = token.length

        while (start < end) {
            val cp = token.codePointAt(start)
            if (isPunctuationBoundary(cp)) {
                start += Character.charCount(cp)
            } else {
                break
            }
        }

        while (end > start) {
            val cp = token.codePointBefore(end)
            if (isPunctuationBoundary(cp)) {
                end -= Character.charCount(cp)
            } else {
                break
            }
        }

        return if (start < end) token.substring(start, end) else ""
    }

    private fun isPunctuationBoundary(cp: Int): Boolean {
        // Strip punctuation like quotes, commas, brackets, exclamation marks, colons, dots at outer boundaries
        // Note: Do not strip apostrophes if internal, but outer quotes/apostrophes get stripped
        val ch = if (cp <= 0xFFFF) cp.toChar() else null
        return ch in listOf('"', '\'', '`', '‘', '’', '“', '”', ',', '.', ';', ':', '!', '?', '(', ')', '[', ']', '{', '}', '<', '>', '«', '»', '¿', '¡')
    }

    private fun isEmojiOrPictograph(cp: Int): Boolean {
        // Safe check for common emoji ranges
        return (cp in 0x1F600..0x1F64F) || // Emoticons
                (cp in 0x1F300..0x1F5FF) || // Misc Symbols and Pictographs
                (cp in 0x1F680..0x1F6FF) || // Transport and Map
                (cp in 0x1F1E0..0x1F1FF) || // Flags
                (cp in 0x2600..0x26FF) ||   // Misc symbols
                (cp in 0x2700..0x27BF) ||   // Dingbats
                (cp in 0xFE00..0xFE0F) ||   // Variation Selectors
                (cp in 0x1F900..0x1F9FF) || // Supplemental Symbols and Pictographs
                (cp in 0x1FA70..0x1FAFF)    // Symbols and Pictographs Extended-A
    }

    /**
     * Filters small words and name particles according to options.
     */
    private fun filterSmallWords(tokens: List<String>, options: InitialsOptions): List<String> {
        if (!options.ignoreSmallWords) return tokens
        if (tokens.size <= 1) return tokens

        val smallWords = mutableSetOf<String>().apply {
            addAll(BASE_SMALL_WORDS)
            if (options.treatNameParticlesAsSmallWords) {
                addAll(NAME_PARTICLES)
            }
        }

        val filtered = when (options.smallWordMode) {
            SmallWordMode.ALWAYS_INCLUDE -> tokens
            SmallWordMode.INCLUDE_FIRST_LAST -> {
                tokens.filterIndexed { index, token ->
                    index == 0 || index == tokens.size - 1 || !smallWords.contains(token.lowercase(Locale.ROOT))
                }
            }
            SmallWordMode.IGNORE -> {
                tokens.filter { token ->
                    !smallWords.contains(token.lowercase(Locale.ROOT))
                }
            }
        }

        // Fallback: If all words were filtered out, preserve original tokens
        return if (filtered.isEmpty()) tokens else filtered
    }

    /**
     * Selects tokens according to InitialsStyle.
     */
    private fun selectWordsByStyle(tokens: List<String>, options: InitialsOptions): List<String> {
        if (tokens.isEmpty()) return emptyList()

        return when (options.style) {
            InitialsStyle.FIRST_WORD -> {
                tokens.take(1)
            }
            InitialsStyle.FIRST_AND_LAST -> {
                if (tokens.size <= 1) tokens else listOf(tokens.first(), tokens.last())
            }
            InitialsStyle.ALL_WORDS -> {
                tokens
            }
            InitialsStyle.FIRST_N_WORDS -> {
                tokens.take(max(1, options.firstNWordsCount))
            }
            InitialsStyle.CUSTOM -> {
                val maxInitials = max(1, options.customMaxInitials)
                when {
                    options.customTakeFirst && options.customTakeLast -> {
                        if (tokens.size <= 1) {
                            tokens
                        } else if (tokens.size == 2 || maxInitials == 2) {
                            listOf(tokens.first(), tokens.last())
                        } else {
                            // Take first, then intermediates up to maxInitials - 1, then last
                            val intermediateCount = min(tokens.size - 2, maxInitials - 2)
                            val result = mutableListOf<String>()
                            result.add(tokens.first())
                            for (j in 1..intermediateCount) {
                                result.add(tokens[j])
                            }
                            result.add(tokens.last())
                            result
                        }
                    }
                    options.customTakeFirst -> tokens.take(maxInitials)
                    options.customTakeLast -> tokens.takeLast(maxInitials)
                    else -> tokens.take(maxInitials)
                }
            }
        }
    }

    /**
     * Extracts initial characters from each selected word token.
     */
    private fun extractInitials(tokens: List<String>, options: InitialsOptions): List<String> {
        val result = mutableListOf<String>()

        for (token in tokens) {
            val isPureNumber = token.all { it.isDigit() }

            if (isPureNumber) {
                if (!options.includeNumbers || options.numberMode == NumberMode.IGNORE) {
                    continue
                }
                when (options.numberMode) {
                    NumberMode.FIRST_DIGIT -> {
                        result.add(token.take(1))
                    }
                    NumberMode.FULL_NUMBER -> {
                        result.add(token)
                    }
                    NumberMode.IGNORE -> { /* Handled above */ }
                }
                continue
            }

            // Extract code points, skipping symbols or punctuation if needed
            val codePoints = mutableListOf<Int>()
            var i = 0
            while (i < token.length) {
                val cp = token.codePointAt(i)
                if (Character.isLetterOrDigit(cp)) {
                    codePoints.add(cp)
                }
                i += Character.charCount(cp)
            }

            if (codePoints.isEmpty()) {
                // If no letters/digits found, fallback to raw code points of the token
                var j = 0
                while (j < token.length) {
                    codePoints.add(token.codePointAt(j))
                    j += Character.charCount(token.codePointAt(j))
                }
            }

            if (codePoints.isEmpty()) continue

            val charsToTake = max(1, options.charactersPerWord)
            val selectedCodePoints = if (options.characterPosition == CharacterPosition.LAST) {
                codePoints.takeLast(charsToTake)
            } else {
                codePoints.take(charsToTake)
            }

            val sb = StringBuilder()
            for (cp in selectedCodePoints) {
                sb.appendCodePoint(cp)
            }
            result.add(sb.toString())
        }

        return result
    }

    /**
     * Applies UPPERCASE, LOWERCASE, or TITLE_CASE to extracted initials.
     */
    private fun applyCase(initials: List<String>, options: InitialsOptions): List<String> {
        val locale = Locale.getDefault()
        return when (options.caseStyle) {
            CaseStyle.UPPERCASE -> initials.map { it.uppercase(locale) }
            CaseStyle.LOWERCASE -> initials.map { it.lowercase(locale) }
            CaseStyle.TITLE_CASE -> {
                if (options.charactersPerWord > 1) {
                    // JoMiSm style: Capitalize each word's initials segment
                    initials.map { segment ->
                        val firstCp = segment.codePoints().findFirst()
                        if (firstCp.isPresent) {
                            val firstCharCount = Character.charCount(firstCp.asInt)
                            val firstLetter = segment.substring(0, firstCharCount).uppercase(locale)
                            val rest = segment.substring(firstCharCount).lowercase(locale)
                            firstLetter + rest
                        } else {
                            segment
                        }
                    }
                } else {
                    // For single letters: e.g. "Jms"
                    initials.mapIndexed { index, segment ->
                        if (index == 0) {
                            segment.uppercase(locale)
                        } else {
                            segment.lowercase(locale)
                        }
                    }
                }
            }
        }
    }

    /**
     * Formats initials with separators and periods.
     */
    private fun formatSeparatorsAndPeriods(initials: List<String>, options: InitialsOptions): String {
        if (initials.isEmpty()) return ""

        val separator = options.separator

        return if (options.addPeriods) {
            if (separator == ".") {
                // e.g. J.M.S.
                initials.joinToString(separator = ".") + "."
            } else if (separator.isEmpty()) {
                // e.g. J.M.S.
                initials.joinToString(separator = ".") + "."
            } else {
                // e.g. J. M. S.
                initials.joinToString(separator = separator) { "$it." }
            }
        } else {
            initials.joinToString(separator = separator)
        }
    }
}
