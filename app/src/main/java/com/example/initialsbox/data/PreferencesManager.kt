package com.example.initialsbox.data

import android.content.Context
import android.content.SharedPreferences
import com.example.initialsbox.model.AppSettings
import com.example.initialsbox.model.AppTheme
import com.example.initialsbox.model.CaseStyle
import com.example.initialsbox.model.CharacterPosition
import com.example.initialsbox.model.HyphenMode
import com.example.initialsbox.model.InitialsHistory
import com.example.initialsbox.model.InitialsOptions
import com.example.initialsbox.model.InitialsSortMode
import com.example.initialsbox.model.InitialsStyle
import com.example.initialsbox.model.NumberMode
import com.example.initialsbox.model.Preset
import com.example.initialsbox.model.SmallWordMode
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "initials_box_prefs"
        private const val KEY_HISTORY = "history_items"
        private const val KEY_CUSTOM_PRESETS = "custom_presets"
        private const val KEY_LAST_INPUT = "last_input_text"
        private const val KEY_LAST_MODE_IS_BATCH = "last_mode_is_batch"
        private const val KEY_SETTINGS_THEME = "settings_theme"
        private const val KEY_SETTINGS_DYNAMIC_COLOR = "settings_dynamic_color"
        private const val KEY_SETTINGS_LIVE_GEN = "settings_live_generation"
        private const val KEY_SETTINGS_HAPTICS = "settings_haptics"
        private const val KEY_SETTINGS_ANIMATIONS = "settings_animations"
        private const val KEY_SETTINGS_CONFIRM_CLEAR = "settings_confirm_clear"
        private const val KEY_SETTINGS_CONFIRM_DELETE_HIST = "settings_confirm_delete_history"
        private const val KEY_SETTINGS_RESTORE_LAST = "settings_restore_last_text"
        private const val MAX_HISTORY_COUNT = 10
    }

    // --- App Settings ---

    fun getAppSettings(): AppSettings {
        val themeName = prefs.getString(KEY_SETTINGS_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val theme = runCatching { AppTheme.valueOf(themeName) }.getOrDefault(AppTheme.SYSTEM)

        return AppSettings(
            theme = theme,
            dynamicColor = prefs.getBoolean(KEY_SETTINGS_DYNAMIC_COLOR, true),
            liveGeneration = prefs.getBoolean(KEY_SETTINGS_LIVE_GEN, true),
            haptics = prefs.getBoolean(KEY_SETTINGS_HAPTICS, true),
            animations = prefs.getBoolean(KEY_SETTINGS_ANIMATIONS, true),
            confirmClear = prefs.getBoolean(KEY_SETTINGS_CONFIRM_CLEAR, true),
            confirmDeleteHistory = prefs.getBoolean(KEY_SETTINGS_CONFIRM_DELETE_HIST, true),
            restoreLastText = prefs.getBoolean(KEY_SETTINGS_RESTORE_LAST, true)
        )
    }

    fun saveAppSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_SETTINGS_THEME, settings.theme.name)
            .putBoolean(KEY_SETTINGS_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_SETTINGS_LIVE_GEN, settings.liveGeneration)
            .putBoolean(KEY_SETTINGS_HAPTICS, settings.haptics)
            .putBoolean(KEY_SETTINGS_ANIMATIONS, settings.animations)
            .putBoolean(KEY_SETTINGS_CONFIRM_CLEAR, settings.confirmClear)
            .putBoolean(KEY_SETTINGS_CONFIRM_DELETE_HIST, settings.confirmDeleteHistory)
            .putBoolean(KEY_SETTINGS_RESTORE_LAST, settings.restoreLastText)
            .apply()
    }

    // --- Last Session State ---

    fun saveLastSession(input: String, isBatch: Boolean) {
        prefs.edit()
            .putString(KEY_LAST_INPUT, input)
            .putBoolean(KEY_LAST_MODE_IS_BATCH, isBatch)
            .apply()
    }

    fun getLastInput(): String = prefs.getString(KEY_LAST_INPUT, "") ?: ""
    fun getLastModeIsBatch(): Boolean = prefs.getBoolean(KEY_LAST_MODE_IS_BATCH, false)

    // --- History (Max 10, explicit save, duplicate prevention) ---

    fun getHistory(): List<InitialsHistory> {
        val jsonString = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<InitialsHistory>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(historyFromJson(obj))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addHistory(item: InitialsHistory): Boolean {
        val current = getHistory().toMutableList()

        // Check immediate duplicate: identical input, identical mode, identical options
        val isDuplicate = current.firstOrNull()?.let { last ->
            last.inputText == item.inputText &&
                    last.mode == item.mode &&
                    last.settings == item.settings
        } ?: false

        if (isDuplicate) {
            return false // Already saved
        }

        // Add to front
        current.add(0, item)

        // Keep maximum 10 items (remove oldest)
        val trimmed = if (current.size > MAX_HISTORY_COUNT) {
            current.take(MAX_HISTORY_COUNT)
        } else {
            current
        }

        val jsonArray = JSONArray()
        for (h in trimmed) {
            jsonArray.put(historyToJson(h))
        }

        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
        return true
    }

    fun deleteHistoryItem(id: String) {
        val current = getHistory().filterNot { it.id == id }
        val jsonArray = JSONArray()
        for (h in current) {
            jsonArray.put(historyToJson(h))
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    // --- Custom Presets ---

    fun getCustomPresets(): List<Preset> {
        val jsonString = prefs.getString(KEY_CUSTOM_PRESETS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Preset>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(presetFromJson(obj))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCustomPreset(preset: Preset) {
        val current = getCustomPresets().filterNot { it.id == preset.id }.toMutableList()
        current.add(preset)
        saveCustomPresetsList(current)
    }

    fun deleteCustomPreset(presetId: String) {
        val current = getCustomPresets().filterNot { it.id == presetId }
        saveCustomPresetsList(current)
    }

    private fun saveCustomPresetsList(presets: List<Preset>) {
        val jsonArray = JSONArray()
        for (p in presets) {
            jsonArray.put(presetToJson(p))
        }
        prefs.edit().putString(KEY_CUSTOM_PRESETS, jsonArray.toString()).apply()
    }

    // --- JSON Serialization Helpers ---

    private fun historyToJson(h: InitialsHistory): JSONObject {
        return JSONObject().apply {
            put("id", h.id)
            put("inputText", h.inputText)
            put("mode", h.mode)
            put("savedAt", h.savedAt)
            put("settings", optionsToJson(h.settings))
        }
    }

    private fun historyFromJson(obj: JSONObject): InitialsHistory {
        return InitialsHistory(
            id = obj.getString("id"),
            inputText = obj.getString("inputText"),
            mode = obj.getString("mode"),
            savedAt = obj.getLong("savedAt"),
            settings = optionsFromJson(obj.getJSONObject("settings"))
        )
    }

    private fun presetToJson(p: Preset): JSONObject {
        return JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("isBuiltIn", p.isBuiltIn)
            put("settings", optionsToJson(p.options))
        }
    }

    private fun presetFromJson(obj: JSONObject): Preset {
        return Preset(
            id = obj.getString("id"),
            name = obj.getString("name"),
            isBuiltIn = obj.optBoolean("isBuiltIn", false),
            options = optionsFromJson(obj.getJSONObject("settings"))
        )
    }

    fun optionsToJson(o: InitialsOptions): JSONObject {
        return JSONObject().apply {
            put("style", o.style.name)
            put("caseStyle", o.caseStyle.name)
            put("separator", o.separator)
            put("addPeriods", o.addPeriods)
            put("prefix", o.prefix)
            put("suffix", o.suffix)
            put("ignoreSmallWords", o.ignoreSmallWords)
            put("smallWordMode", o.smallWordMode.name)
            put("treatNameParticlesAsSmallWords", o.treatNameParticlesAsSmallWords)
            put("hyphenMode", o.hyphenMode.name)
            put("symbolAsSeparator", o.symbolAsSeparator)
            put("includeNumbers", o.includeNumbers)
            put("numberMode", o.numberMode.name)
            put("characterPosition", o.characterPosition.name)
            put("charactersPerWord", o.charactersPerWord)
            put("removeDuplicateResults", o.removeDuplicateResults)
            put("removeDuplicateInputs", o.removeDuplicateInputs)
            put("sortMode", o.sortMode.name)
            put("firstNWordsCount", o.firstNWordsCount)
            put("parseEmailComponents", o.parseEmailComponents)
            put("numberBatchResults", o.numberBatchResults)
            put("ignoreEmptyLines", o.ignoreEmptyLines)
            put("customTakeFirst", o.customTakeFirst)
            put("customTakeLast", o.customTakeLast)
            put("customMaxInitials", o.customMaxInitials)
        }
    }

    fun optionsFromJson(obj: JSONObject): InitialsOptions {
        return InitialsOptions(
            style = runCatching { InitialsStyle.valueOf(obj.getString("style")) }.getOrDefault(InitialsStyle.ALL_WORDS),
            caseStyle = runCatching { CaseStyle.valueOf(obj.getString("caseStyle")) }.getOrDefault(CaseStyle.UPPERCASE),
            separator = obj.optString("separator", ""),
            addPeriods = obj.optBoolean("addPeriods", false),
            prefix = obj.optString("prefix", ""),
            suffix = obj.optString("suffix", ""),
            ignoreSmallWords = obj.optBoolean("ignoreSmallWords", true),
            smallWordMode = runCatching { SmallWordMode.valueOf(obj.getString("smallWordMode")) }.getOrDefault(SmallWordMode.IGNORE),
            treatNameParticlesAsSmallWords = obj.optBoolean("treatNameParticlesAsSmallWords", false),
            hyphenMode = runCatching { HyphenMode.valueOf(obj.getString("hyphenMode")) }.getOrDefault(HyphenMode.ONE_WORD),
            symbolAsSeparator = obj.optBoolean("symbolAsSeparator", true),
            includeNumbers = obj.optBoolean("includeNumbers", true),
            numberMode = runCatching { NumberMode.valueOf(obj.getString("numberMode")) }.getOrDefault(NumberMode.FIRST_DIGIT),
            characterPosition = runCatching { CharacterPosition.valueOf(obj.getString("characterPosition")) }.getOrDefault(CharacterPosition.FIRST),
            charactersPerWord = obj.optInt("charactersPerWord", 1),
            removeDuplicateResults = obj.optBoolean("removeDuplicateResults", false),
            removeDuplicateInputs = obj.optBoolean("removeDuplicateInputs", false),
            sortMode = runCatching { InitialsSortMode.valueOf(obj.getString("sortMode")) }.getOrDefault(InitialsSortMode.INPUT_ORDER),
            firstNWordsCount = obj.optInt("firstNWordsCount", 2),
            parseEmailComponents = obj.optBoolean("parseEmailComponents", false),
            numberBatchResults = obj.optBoolean("numberBatchResults", false),
            ignoreEmptyLines = obj.optBoolean("ignoreEmptyLines", true),
            customTakeFirst = obj.optBoolean("customTakeFirst", true),
            customTakeLast = obj.optBoolean("customTakeLast", true),
            customMaxInitials = obj.optInt("customMaxInitials", 3)
        )
    }
}
