package com.example.initialsbox.model

data class InitialsHistory(
    val id: String,
    val inputText: String,
    val mode: String, // "SINGLE" or "BATCH"
    val settings: InitialsOptions,
    val savedAt: Long
)
