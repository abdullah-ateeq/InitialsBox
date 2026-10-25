package com.example.initialsbox.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.initialsbox.data.PreferencesManager
import com.example.initialsbox.engine.ExportFormatter
import com.example.initialsbox.engine.InitialsGenerator
import com.example.initialsbox.model.AppSettings
import com.example.initialsbox.model.AppTheme
import com.example.initialsbox.model.InitialsHistory
import com.example.initialsbox.model.InitialsOptions
import com.example.initialsbox.model.InitialsResult
import com.example.initialsbox.model.InitialsSortMode
import com.example.initialsbox.model.Preset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class InitialsBoxViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val _uiState = MutableStateFlow(InitialsBoxUiState())
    val uiState: StateFlow<InitialsBoxUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val appSettings = prefs.getAppSettings()
        val customPresets = prefs.getCustomPresets()
        val historyList = prefs.getHistory()
        val allPresets = Preset.BUILT_IN_PRESETS + customPresets

        val initialInput = if (appSettings.restoreLastText) prefs.getLastInput() else ""
        val initialBatch = if (appSettings.restoreLastText) prefs.getLastModeIsBatch() else false

        _uiState.update {
            it.copy(
                appSettings = appSettings,
                presets = allPresets,
                history = historyList,
                inputText = initialInput,
                batchMode = initialBatch
            )
        }

        if (initialInput.isNotBlank()) {
            triggerGeneration(debounceMs = 0)
        }
    }

    // --- Input & Mode Management ---

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText, errorMessage = null) }
        prefs.saveLastSession(newText, _uiState.value.batchMode)

        if (_uiState.value.appSettings.liveGeneration) {
            triggerGeneration(debounceMs = 250)
        }
    }

    fun setBatchMode(isBatch: Boolean) {
        if (_uiState.value.batchMode != isBatch) {
            _uiState.update {
                it.copy(
                    batchMode = isBatch,
                    selectedIds = emptySet(),
                    searchQuery = ""
                )
            }
            prefs.saveLastSession(_uiState.value.inputText, isBatch)
            triggerGeneration(debounceMs = 0)
        }
    }

    fun clearInput() {
        _uiState.update {
            it.copy(
                inputText = "",
                results = emptyList(),
                selectedIds = emptySet(),
                searchQuery = "",
                errorMessage = null
            )
        }
        prefs.saveLastSession("", _uiState.value.batchMode)
    }

    // --- Options & Presets Management ---

    fun updateOptions(newOptions: InitialsOptions) {
        _uiState.update {
            it.copy(
                options = newOptions,
                activePresetId = null // custom change removes active preset selection
            )
        }
        if (_uiState.value.appSettings.liveGeneration) {
            triggerGeneration(debounceMs = 150)
        } else {
            triggerGeneration(debounceMs = 0)
        }
    }

    fun applyPreset(preset: Preset) {
        _uiState.update {
            it.copy(
                options = preset.options,
                activePresetId = preset.id
            )
        }
        triggerGeneration(debounceMs = 0)
        showFeedback("Preset '${preset.name}' applied")
    }

    fun resetToStandard() {
        applyPreset(Preset.STANDARD)
    }

    fun saveCurrentStyleAsPreset(name: String) {
        if (name.isBlank()) return
        val newPreset = Preset(
            id = "custom_${UUID.randomUUID()}",
            name = name.trim(),
            isBuiltIn = false,
            options = _uiState.value.options
        )
        prefs.saveCustomPreset(newPreset)
        val updatedCustom = prefs.getCustomPresets()
        _uiState.update {
            it.copy(
                presets = Preset.BUILT_IN_PRESETS + updatedCustom,
                activePresetId = newPreset.id
            )
        }
        showFeedback("Preset '$name' saved")
    }

    fun deleteCustomPreset(presetId: String) {
        prefs.deleteCustomPreset(presetId)
        val updatedCustom = prefs.getCustomPresets()
        _uiState.update {
            it.copy(
                presets = Preset.BUILT_IN_PRESETS + updatedCustom,
                activePresetId = if (it.activePresetId == presetId) null else it.activePresetId
            )
        }
        showFeedback("Preset deleted")
    }

    // --- Search, Selection & Sort in Batch Mode ---

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSelectResult(id: String) {
        _uiState.update { state ->
            val updated = state.selectedIds.toMutableSet()
            if (updated.contains(id)) {
                updated.remove(id)
            } else {
                updated.add(id)
            }
            state.copy(selectedIds = updated)
        }
    }

    fun selectAllFilteredResults(filteredResults: List<InitialsResult>) {
        val allIds = filteredResults.map { it.id }.toSet()
        _uiState.update { it.copy(selectedIds = allIds) }
    }

    fun deselectAll() {
        _uiState.update { it.copy(selectedIds = emptySet()) }
    }

    fun setSortMode(sortMode: InitialsSortMode) {
        updateOptions(_uiState.value.options.copy(sortMode = sortMode))
    }

    // --- Generation Engine (Background Thread + Debounce) ---

    fun triggerGeneration(debounceMs: Long = 0) {
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            if (debounceMs > 0) {
                delay(debounceMs)
            }

            val text = _uiState.value.inputText
            val isBatch = _uiState.value.batchMode
            val options = _uiState.value.options

            if (text.isBlank()) {
                _uiState.update {
                    it.copy(
                        results = emptyList(),
                        isGenerating = false,
                        errorMessage = null
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isGenerating = true) }

            try {
                val computedResults = withContext(Dispatchers.Default) {
                    if (isBatch) {
                        InitialsGenerator.generateBatch(text, options)
                    } else {
                        listOf(InitialsGenerator.generate(text, options))
                    }
                }

                _uiState.update {
                    it.copy(
                        results = computedResults,
                        isGenerating = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "Generation error: ${e.localizedMessage ?: "Unknown"}"
                    )
                }
            }
        }
    }

    // --- History Management (Limit 10, explicit save) ---

    fun saveCurrentSession(): Boolean {
        val state = _uiState.value
        if (state.inputText.isBlank() || state.results.isEmpty()) {
            showFeedback("Nothing to save")
            return false
        }

        val historyItem = InitialsHistory(
            id = UUID.randomUUID().toString(),
            inputText = state.inputText,
            mode = if (state.batchMode) "BATCH" else "SINGLE",
            settings = state.options,
            savedAt = System.currentTimeMillis()
        )

        val saved = prefs.addHistory(historyItem)
        if (saved) {
            _uiState.update { it.copy(history = prefs.getHistory()) }
            showFeedback("Session saved to History")
            return true
        } else {
            showFeedback("Session is already in History")
            return false
        }
    }

    fun restoreHistory(item: InitialsHistory) {
        val isBatch = item.mode == "BATCH"
        _uiState.update {
            it.copy(
                inputText = item.inputText,
                batchMode = isBatch,
                options = item.settings,
                activePresetId = null,
                selectedIds = emptySet(),
                searchQuery = ""
            )
        }
        prefs.saveLastSession(item.inputText, isBatch)
        triggerGeneration(debounceMs = 0)
        showFeedback("History session restored")
    }

    fun deleteHistoryItem(id: String) {
        prefs.deleteHistoryItem(id)
        _uiState.update { it.copy(history = prefs.getHistory()) }
        showFeedback("History item removed")
    }

    fun clearAllHistory() {
        prefs.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
        showFeedback("History cleared")
    }

    // --- Clipboard Operations ---

    fun copyToClipboard(label: String, content: String, feedbackMessage: String = "Initials copied.") {
        if (content.isBlank()) return
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        showFeedback(feedbackMessage)
    }

    fun copySingleInitials() {
        val firstResult = _uiState.value.results.firstOrNull() ?: return
        copyToClipboard("Initials", firstResult.initials, "Initials copied.")
    }

    fun copyAllInitials(selectedOnly: Boolean = false) {
        val items = getTargetResults(selectedOnly)
        if (items.isEmpty()) return
        val content = ExportFormatter.toInitialsOnly(items, _uiState.value.options.numberBatchResults)
        copyToClipboard("Initials", content, "${items.size} initials copied.")
    }

    fun copyInputWithInitials(selectedOnly: Boolean = false) {
        val items = getTargetResults(selectedOnly)
        if (items.isEmpty()) return
        val content = ExportFormatter.toInputAndInitials(items, _uiState.value.options.numberBatchResults)
        copyToClipboard("Input and Initials", content, "Copied with input.")
    }

    fun copyAsJson(selectedOnly: Boolean = false) {
        val items = getTargetResults(selectedOnly)
        if (items.isEmpty()) return
        val json = ExportFormatter.toJson(items)
        copyToClipboard("Initials JSON", json, "JSON copied to clipboard.")
    }

    // --- Share Intent (Offline Plain Text) ---

    fun shareResults(context: Context, selectedOnly: Boolean = false) {
        val items = getTargetResults(selectedOnly)
        if (items.isEmpty()) return

        val textToShare = if (_uiState.value.batchMode) {
            ExportFormatter.toTxt(items, _uiState.value.options.numberBatchResults)
        } else {
            items.first().initials
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textToShare)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Initials")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // --- Export Utilities ---

    fun getExportContent(format: String, selectedOnly: Boolean = false): String {
        val items = getTargetResults(selectedOnly)
        return when (format.uppercase()) {
            "CSV" -> ExportFormatter.toCsv(items)
            "JSON" -> ExportFormatter.toJson(items)
            else -> ExportFormatter.toTxt(items, _uiState.value.options.numberBatchResults)
        }
    }

    private fun getTargetResults(selectedOnly: Boolean): List<InitialsResult> {
        val state = _uiState.value
        val allResults = state.results
        if (!selectedOnly || state.selectedIds.isEmpty()) {
            return allResults
        }
        return allResults.filter { state.selectedIds.contains(it.id) }
    }

    // --- App Settings ---

    fun updateTheme(theme: AppTheme) {
        val updated = _uiState.value.appSettings.copy(theme = theme)
        prefs.saveAppSettings(updated)
        _uiState.update { it.copy(appSettings = updated) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        val updated = _uiState.value.appSettings.copy(dynamicColor = enabled)
        prefs.saveAppSettings(updated)
        _uiState.update { it.copy(appSettings = updated) }
    }

    fun updateSetting(
        liveGen: Boolean? = null,
        haptics: Boolean? = null,
        animations: Boolean? = null,
        confirmClear: Boolean? = null,
        confirmDeleteHistory: Boolean? = null,
        restoreLastText: Boolean? = null
    ) {
        val current = _uiState.value.appSettings
        val updated = current.copy(
            liveGeneration = liveGen ?: current.liveGeneration,
            haptics = haptics ?: current.haptics,
            animations = animations ?: current.animations,
            confirmClear = confirmClear ?: current.confirmClear,
            confirmDeleteHistory = confirmDeleteHistory ?: current.confirmDeleteHistory,
            restoreLastText = restoreLastText ?: current.restoreLastText
        )
        prefs.saveAppSettings(updated)
        _uiState.update { it.copy(appSettings = updated) }
    }

    // --- User Feedback ---

    fun showFeedback(message: String) {
        _uiState.update { it.copy(userFeedbackMessage = message) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(userFeedbackMessage = null) }
    }
}
