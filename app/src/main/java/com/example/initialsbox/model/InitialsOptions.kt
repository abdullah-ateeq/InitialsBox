package com.example.initialsbox.model

data class InitialsOptions(
    val style: InitialsStyle = InitialsStyle.ALL_WORDS,
    val caseStyle: CaseStyle = CaseStyle.UPPERCASE,
    val separator: String = "",
    val addPeriods: Boolean = false,
    val prefix: String = "",
    val suffix: String = "",
    val ignoreSmallWords: Boolean = true,
    val smallWordMode: SmallWordMode = SmallWordMode.INCLUDE_FIRST_LAST,
    val treatNameParticlesAsSmallWords: Boolean = false,
    val hyphenMode: HyphenMode = HyphenMode.ONE_WORD,
    val symbolAsSeparator: Boolean = true,
    val includeNumbers: Boolean = true,
    val numberMode: NumberMode = NumberMode.FIRST_DIGIT,
    val characterPosition: CharacterPosition = CharacterPosition.FIRST,
    val charactersPerWord: Int = 1,
    val removeDuplicateResults: Boolean = false,
    val removeDuplicateInputs: Boolean = false,
    val sortMode: InitialsSortMode = InitialsSortMode.INPUT_ORDER,
    val firstNWordsCount: Int = 2,
    val parseEmailComponents: Boolean = false,
    val numberBatchResults: Boolean = false,
    val ignoreEmptyLines: Boolean = true,
    val customTakeFirst: Boolean = true,
    val customTakeLast: Boolean = true,
    val customMaxInitials: Int = 3
)
