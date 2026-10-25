package com.example.initialsbox.model

enum class InitialsStyle {
    FIRST_WORD,
    FIRST_AND_LAST,
    ALL_WORDS,
    FIRST_N_WORDS,
    CUSTOM
}

enum class CaseStyle {
    UPPERCASE,
    LOWERCASE,
    TITLE_CASE
}

enum class SmallWordMode {
    IGNORE,
    INCLUDE_FIRST_LAST,
    ALWAYS_INCLUDE
}

enum class HyphenMode {
    ONE_WORD,
    SPLIT_WORDS
}

enum class NumberMode {
    FIRST_DIGIT,
    FULL_NUMBER,
    IGNORE
}

enum class CharacterPosition {
    FIRST,
    LAST
}

enum class InitialsSortMode {
    INPUT_ORDER,
    INPUT_ALPHABETICAL,
    INITIALS_ALPHABETICAL,
    REVERSE_ALPHABETICAL,
    LONGEST_FIRST,
    SHORTEST_FIRST
}
