package com.example.initialsbox.model

import java.util.UUID

data class InitialsResult(
    val id: String = UUID.randomUUID().toString(),
    val input: String,
    val initials: String,
    val originalWordCount: Int,
    val usedWordCount: Int,
    val characterCount: Int,
    val wordTokens: List<String> = emptyList()
)
