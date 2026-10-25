package com.example.initialsbox

import com.example.initialsbox.engine.ExportFormatter
import com.example.initialsbox.engine.InitialsGenerator
import com.example.initialsbox.model.CaseStyle
import com.example.initialsbox.model.CharacterPosition
import com.example.initialsbox.model.HyphenMode
import com.example.initialsbox.model.InitialsOptions
import com.example.initialsbox.model.InitialsStyle
import com.example.initialsbox.model.NumberMode
import com.example.initialsbox.model.SmallWordMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InitialsGeneratorTest {

    @Test
    fun testBasicName() {
        val input = "John Michael Smith"
        val options = InitialsOptions(
            style = InitialsStyle.ALL_WORDS,
            caseStyle = CaseStyle.UPPERCASE,
            ignoreSmallWords = false
        )
        val result = InitialsGenerator.generate(input, options)
        assertEquals("JMS", result.initials)
    }

    @Test
    fun testFirstAndLast() {
        val input = "John Michael Smith"
        val options = InitialsOptions(style = InitialsStyle.FIRST_AND_LAST)
        val result = InitialsGenerator.generate(input, options)
        assertEquals("JS", result.initials)

        val singleWord = "John"
        val singleResult = InitialsGenerator.generate(singleWord, options)
        assertEquals("J", singleResult.initials)
    }

    @Test
    fun testSmallWordsIgnored() {
        val input = "The Lord of the Rings"
        val options = InitialsOptions(
            style = InitialsStyle.ALL_WORDS,
            ignoreSmallWords = true
        )
        val result = InitialsGenerator.generate(input, options)
        assertEquals("TLR", result.initials)
    }

    @Test
    fun testSmallWordsIgnoreCompletely() {
        val input = "The Lord of the Rings"
        val options = InitialsOptions(
            style = InitialsStyle.ALL_WORDS,
            ignoreSmallWords = true,
            smallWordMode = SmallWordMode.IGNORE
        )
        val result = InitialsGenerator.generate(input, options)
        assertEquals("LR", result.initials)
    }

    @Test
    fun testAllWordsSmallWordsDisabled() {
        val input = "The Lord of the Rings"
        val options = InitialsOptions(
            style = InitialsStyle.ALL_WORDS,
            ignoreSmallWords = false
        )
        val result = InitialsGenerator.generate(input, options)
        assertEquals("TLOTR", result.initials)
    }

    @Test
    fun testCaseVariations() {
        val input = "John Michael Smith"
        val lowerOptions = InitialsOptions(caseStyle = CaseStyle.LOWERCASE, ignoreSmallWords = false)
        assertEquals("jms", InitialsGenerator.generate(input, lowerOptions).initials)

        val titleOptions = InitialsOptions(caseStyle = CaseStyle.TITLE_CASE, ignoreSmallWords = false)
        assertEquals("Jms", InitialsGenerator.generate(input, titleOptions).initials)
    }

    @Test
    fun testSeparator() {
        val input = "John Michael Smith"
        val dotOptions = InitialsOptions(separator = ".", addPeriods = false, ignoreSmallWords = false)
        assertEquals("J.M.S", InitialsGenerator.generate(input, dotOptions).initials)

        val spaceOptions = InitialsOptions(separator = " ", ignoreSmallWords = false)
        assertEquals("J M S", InitialsGenerator.generate(input, spaceOptions).initials)
    }

    @Test
    fun testPeriods() {
        val input = "John Michael Smith"
        val periodOptions = InitialsOptions(addPeriods = true, ignoreSmallWords = false)
        assertEquals("J.M.S.", InitialsGenerator.generate(input, periodOptions).initials)
    }

    @Test
    fun testHyphenatedName() {
        val input = "Mary-Jane Watson"

        val oneWordOptions = InitialsOptions(hyphenMode = HyphenMode.ONE_WORD)
        assertEquals("MW", InitialsGenerator.generate(input, oneWordOptions).initials)

        val splitOptions = InitialsOptions(hyphenMode = HyphenMode.SPLIT_WORDS)
        assertEquals("MJW", InitialsGenerator.generate(input, splitOptions).initials)
    }

    @Test
    fun testApostrophe() {
        val input = "John O'Connor"
        val options = InitialsOptions()
        assertEquals("JO", InitialsGenerator.generate(input, options).initials)
    }

    @Test
    fun testUnderscore() {
        val input = "john_smith"
        val options = InitialsOptions(symbolAsSeparator = true)
        assertEquals("JS", InitialsGenerator.generate(input, options).initials)
    }

    @Test
    fun testNumbers() {
        val input = "Project 2026"

        val firstDigitOptions = InitialsOptions(includeNumbers = true, numberMode = NumberMode.FIRST_DIGIT)
        assertEquals("P2", InitialsGenerator.generate(input, firstDigitOptions).initials)

        val fullNumberOptions = InitialsOptions(includeNumbers = true, numberMode = NumberMode.FULL_NUMBER)
        assertEquals("P2026", InitialsGenerator.generate(input, fullNumberOptions).initials)

        val ignoreNumberOptions = InitialsOptions(includeNumbers = true, numberMode = NumberMode.IGNORE)
        assertEquals("P", InitialsGenerator.generate(input, ignoreNumberOptions).initials)
    }

    @Test
    fun testUnicodeAndAccents() {
        val input = "José García"
        val options = InitialsOptions()
        assertEquals("JG", InitialsGenerator.generate(input, options).initials)
    }

    @Test
    fun testArabicAndRTL() {
        val input = "محمد علی"
        val options = InitialsOptions(separator = " ")
        val result = InitialsGenerator.generate(input, options)
        assertEquals("م ع", result.initials)
        assertEquals("محمد علی", result.input)
    }

    @Test
    fun testPunctuation() {
        val input = "\"John,\" Michael! Smith."
        val options = InitialsOptions(ignoreSmallWords = false)
        assertEquals("JMS", InitialsGenerator.generate(input, options).initials)
    }

    @Test
    fun testMultipleSpaces() {
        val input = "John     Michael      Smith"
        val options = InitialsOptions(ignoreSmallWords = false)
        assertEquals("JMS", InitialsGenerator.generate(input, options).initials)
    }

    @Test
    fun testMultilineSingleMode() {
        val input = "John Michael\nSmith"
        val options = InitialsOptions(ignoreSmallWords = false)
        assertEquals("JMS", InitialsGenerator.generate(input, options).initials)
    }

    @Test
    fun testBatchMode() {
        val batchInput = "John Michael Smith\nSarah Jane Wilson\nAli Hassan"
        val options = InitialsOptions(ignoreSmallWords = false)
        val results = InitialsGenerator.generateBatch(batchInput, options)
        assertEquals(3, results.size)
        assertEquals("JMS", results[0].initials)
        assertEquals("SJW", results[1].initials)
        assertEquals("AH", results[2].initials)
    }

    @Test
    fun testEmptyLinesInBatch() {
        val batchInput = "John Smith\n\nJane Smith"
        val options = InitialsOptions(ignoreEmptyLines = true)
        val results = InitialsGenerator.generateBatch(batchInput, options)
        assertEquals(2, results.size)
        assertEquals("JS", results[0].initials)
        assertEquals("JS", results[1].initials)
    }

    @Test
    fun testDuplicateResultsRemoval() {
        val batchInput = "John Smith\nJane Smith\nJack Stone"
        val options = InitialsOptions(removeDuplicateResults = true)
        val results = InitialsGenerator.generateBatch(batchInput, options)
        assertEquals(1, results.size)
        assertEquals("JS", results[0].initials)
    }

    @Test
    fun testDuplicateInputsRemoval() {
        val batchInput = "John Smith\nJohn Smith\nJane Smith"
        val options = InitialsOptions(removeDuplicateInputs = true)
        val results = InitialsGenerator.generateBatch(batchInput, options)
        assertEquals(2, results.size)
        assertEquals("John Smith", results[0].input)
        assertEquals("Jane Smith", results[1].input)
    }

    @Test
    fun testFirstNWords() {
        val input = "John Michael Robert Smith"
        val first2Options = InitialsOptions(
            style = InitialsStyle.FIRST_N_WORDS,
            firstNWordsCount = 2,
            ignoreSmallWords = false
        )
        assertEquals("JM", InitialsGenerator.generate(input, first2Options).initials)

        val first3Options = InitialsOptions(
            style = InitialsStyle.FIRST_N_WORDS,
            firstNWordsCount = 3,
            ignoreSmallWords = false
        )
        assertEquals("JMR", InitialsGenerator.generate(input, first3Options).initials)
    }

    @Test
    fun testLastCharacterPosition() {
        val input = "John Michael Smith"
        val options = InitialsOptions(
            characterPosition = CharacterPosition.LAST,
            ignoreSmallWords = false,
            separator = " "
        )
        // John -> n, Michael -> l, Smith -> h -> in UPPERCASE with space separator: N L H
        assertEquals("N L H", InitialsGenerator.generate(input, options).initials)
    }

    @Test
    fun testCharactersPerWord() {
        val input = "John Michael Smith"
        val twoCharsOptions = InitialsOptions(
            charactersPerWord = 2,
            caseStyle = CaseStyle.TITLE_CASE,
            ignoreSmallWords = false
        )
        assertEquals("JoMiSm", InitialsGenerator.generate(input, twoCharsOptions).initials)

        val threeCharsOptions = InitialsOptions(
            charactersPerWord = 3,
            caseStyle = CaseStyle.TITLE_CASE,
            ignoreSmallWords = false
        )
        assertEquals("JohMicSmi", InitialsGenerator.generate(input, threeCharsOptions).initials)
    }

    @Test
    fun testSmallWordFirstAndLastMode() {
        val input = "The Lord of the Rings"
        val options = InitialsOptions(
            style = InitialsStyle.ALL_WORDS,
            ignoreSmallWords = true,
            smallWordMode = SmallWordMode.INCLUDE_FIRST_LAST
        )
        val result = InitialsGenerator.generate(input, options)
        assertEquals("TLR", result.initials)
    }

    @Test
    fun testNameParticles() {
        val input = "Ludwig van Beethoven"

        val particlesOff = InitialsOptions(
            treatNameParticlesAsSmallWords = false,
            ignoreSmallWords = true
        )
        assertEquals("LVB", InitialsGenerator.generate(input, particlesOff).initials)

        val particlesOn = InitialsOptions(
            treatNameParticlesAsSmallWords = true,
            ignoreSmallWords = true
        )
        assertEquals("LB", InitialsGenerator.generate(input, particlesOn).initials)
    }

    @Test
    fun testEmojiHandling() {
        val input = "John 👋 Smith"
        val options = InitialsOptions(ignoreSmallWords = false)
        assertEquals("JS", InitialsGenerator.generate(input, options).initials)
    }

    @Test
    fun testLargeInputPerformance() {
        val sb = StringBuilder()
        for (i in 1..20000) {
            sb.append("Word ")
        }
        val text = sb.toString()
        assertTrue(text.length >= 100000)

        val result = InitialsGenerator.generate(text, InitialsOptions())
        assertFalse(result.initials.isEmpty())
    }

    @Test
    fun testLargeBatchPerformance() {
        val sb = StringBuilder()
        for (i in 1..1500) {
            sb.append("Entry Number $i\n")
        }
        val batchText = sb.toString()

        val results = InitialsGenerator.generateBatch(batchText, InitialsOptions())
        assertEquals(1500, results.size)
    }

    @Test
    fun testJsonExportFormat() {
        val results = listOf(
            InitialsGenerator.generate("John Michael Smith", InitialsOptions()),
            InitialsGenerator.generate("Sarah Jane Wilson", InitialsOptions())
        )
        val json = ExportFormatter.toJson(results)
        assertTrue(json.contains("\"input\": \"John Michael Smith\""))
        assertTrue(json.contains("\"initials\": \"JMS\""))
        assertTrue(json.contains("\"results\": ["))
    }

    @Test
    fun testCsvExportFormat() {
        val results = listOf(
            InitialsGenerator.generate("John, \"Johnny\" Smith", InitialsOptions()),
            InitialsGenerator.generate("Sarah Jane", InitialsOptions())
        )
        val csv = ExportFormatter.toCsv(results)
        assertTrue(csv.startsWith("\"Input\",\"Initials\",\"Word Count\",\"Character Count\""))
        // Quotes inside CSV must be escaped as double quotes
        assertTrue(csv.contains("\"\"Johnny\"\""))
    }

    @Test
    fun testSortingModes() {
        val batchInput = "Charlie Brown\nAlice Smith\nBob White"
        val options = InitialsOptions(ignoreSmallWords = false)

        val alphabetical = InitialsGenerator.sortResults(
            InitialsGenerator.generateBatch(batchInput, options),
            com.example.initialsbox.model.InitialsSortMode.INPUT_ALPHABETICAL
        )
        assertEquals("Alice Smith", alphabetical[0].input)
        assertEquals("Bob White", alphabetical[1].input)
        assertEquals("Charlie Brown", alphabetical[2].input)

        val initialsAlpha = InitialsGenerator.sortResults(
            InitialsGenerator.generateBatch(batchInput, options),
            com.example.initialsbox.model.InitialsSortMode.INITIALS_ALPHABETICAL
        )
        assertEquals("AS", initialsAlpha[0].initials)
        assertEquals("BW", initialsAlpha[1].initials)
        assertEquals("CB", initialsAlpha[2].initials)
    }

    @Test
    fun testCustomStyleMode() {
        val input = "John Michael Robert Smith"
        val firstAndLastCustom = InitialsOptions(
            style = InitialsStyle.CUSTOM,
            customTakeFirst = true,
            customTakeLast = true,
            customMaxInitials = 2,
            ignoreSmallWords = false
        )
        assertEquals("JS", InitialsGenerator.generate(input, firstAndLastCustom).initials)

        val threeInitialsCustom = InitialsOptions(
            style = InitialsStyle.CUSTOM,
            customTakeFirst = true,
            customTakeLast = true,
            customMaxInitials = 3,
            ignoreSmallWords = false
        )
        assertEquals("JMS", InitialsGenerator.generate(input, threeInitialsCustom).initials)
    }

    @Test
    fun testEmailParsingOption() {
        val input = "john@example.com"
        val defaultOptions = InitialsOptions(parseEmailComponents = false)
        assertEquals("J", InitialsGenerator.generate(input, defaultOptions).initials)

        val parsedEmailOptions = InitialsOptions(parseEmailComponents = true, ignoreSmallWords = false)
        val result = InitialsGenerator.generate(input, parsedEmailOptions)
        assertTrue(result.initials.startsWith("JE"))
    }
}
