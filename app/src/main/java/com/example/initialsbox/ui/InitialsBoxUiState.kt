package com.example.initialsbox.ui

import com.example.initialsbox.model.AppSettings
import com.example.initialsbox.model.InitialsHistory
import com.example.initialsbox.model.InitialsOptions
import com.example.initialsbox.model.InitialsResult
import com.example.initialsbox.model.Preset

data class InitialsBoxUiState(
    val inputText: String = "",
    val batchMode: Boolean = false,
    val options: InitialsOptions = InitialsOptions(),
    val results: List<InitialsResult> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    // Extensions for state management
    val history: List<InitialsHistory> = emptyList(),
    val presets: List<Preset> = Preset.BUILT_IN_PRESETS,
    val activePresetId: String? = Preset.STANDARD.id,
    val appSettings: AppSettings = AppSettings(),
    val userFeedbackMessage: String? = null
)
