package com.example.initialsbox.model

data class Preset(
    val id: String,
    val name: String,
    val isBuiltIn: Boolean = false,
    val options: InitialsOptions
) {
    companion object {
        val STANDARD = Preset(
            id = "preset_standard",
            name = "Standard",
            isBuiltIn = true,
            options = InitialsOptions(
                style = InitialsStyle.ALL_WORDS,
                caseStyle = CaseStyle.UPPERCASE,
                separator = "",
                addPeriods = false,
                ignoreSmallWords = true
            )
        )

        val FIRST_AND_LAST = Preset(
            id = "preset_first_last",
            name = "First + Last",
            isBuiltIn = true,
            options = InitialsOptions(
                style = InitialsStyle.FIRST_AND_LAST,
                caseStyle = CaseStyle.UPPERCASE,
                separator = "",
                addPeriods = false,
                ignoreSmallWords = false
            )
        )

        val PROFESSIONAL = Preset(
            id = "preset_professional",
            name = "Professional",
            isBuiltIn = true,
            options = InitialsOptions(
                style = InitialsStyle.FIRST_AND_LAST,
                caseStyle = CaseStyle.UPPERCASE,
                separator = "",
                addPeriods = true,
                ignoreSmallWords = false
            )
        )

        val COMPACT = Preset(
            id = "preset_compact",
            name = "Compact",
            isBuiltIn = true,
            options = InitialsOptions(
                style = InitialsStyle.FIRST_N_WORDS,
                firstNWordsCount = 2,
                caseStyle = CaseStyle.UPPERCASE,
                separator = "",
                addPeriods = false,
                ignoreSmallWords = true
            )
        )

        val PHRASE = Preset(
            id = "preset_phrase",
            name = "Phrase",
            isBuiltIn = true,
            options = InitialsOptions(
                style = InitialsStyle.ALL_WORDS,
                caseStyle = CaseStyle.UPPERCASE,
                separator = "",
                addPeriods = false,
                ignoreSmallWords = true,
                smallWordMode = SmallWordMode.IGNORE
            )
        )

        val BUILT_IN_PRESETS = listOf(
            STANDARD,
            FIRST_AND_LAST,
            PROFESSIONAL,
            COMPACT,
            PHRASE
        )
    }
}
