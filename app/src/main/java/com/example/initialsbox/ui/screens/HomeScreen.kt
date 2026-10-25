package com.example.initialsbox.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.initialsbox.model.CaseStyle
import com.example.initialsbox.model.CharacterPosition
import com.example.initialsbox.model.HyphenMode
import com.example.initialsbox.model.InitialsOptions
import com.example.initialsbox.model.InitialsResult
import com.example.initialsbox.model.InitialsSortMode
import com.example.initialsbox.model.InitialsStyle
import com.example.initialsbox.model.NumberMode
import com.example.initialsbox.model.SmallWordMode
import com.example.initialsbox.ui.InitialsBoxViewModel
import com.example.initialsbox.ui.components.ExportDialog
import com.example.initialsbox.ui.components.SavePresetDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: InitialsBoxViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportSelectedOnly by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showConfirmClearDialog by remember { mutableStateOf(false) }
    var showAdvancedOptions by remember { mutableStateOf(false) }
    var showCustomSeparatorField by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Display feedback messages via Snackbar
    LaunchedEffect(state.userFeedbackMessage) {
        state.userFeedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    if (showExportDialog) {
        ExportDialog(
            viewModel = viewModel,
            selectedOnly = exportSelectedOnly,
            onDismiss = { showExportDialog = false }
        )
    }

    if (showSavePresetDialog) {
        SavePresetDialog(
            onSave = { name -> viewModel.saveCurrentStyleAsPreset(name) },
            onDismiss = { showSavePresetDialog = false }
        )
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("Clear Input?") },
            text = { Text("This will clear the current text input.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearInput()
                        showConfirmClearDialog = false
                    }
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "InitialsBox",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generate clean initials from names and phrases.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // --- Mode Selector (Single vs Batch) ---
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                SegmentedButton(
                    selected = !state.batchMode,
                    onClick = { viewModel.setBatchMode(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("SINGLE", fontWeight = FontWeight.SemiBold)
                }
                SegmentedButton(
                    selected = state.batchMode,
                    onClick = { viewModel.setBatchMode(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("BATCH", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Presets Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PRESETS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    TextButton(onClick = { viewModel.resetToStandard() }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset to Standard", fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = { showSavePresetDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Save Preset", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.presets.forEach { preset ->
                    FilterChip(
                        selected = state.activePresetId == preset.id,
                        onClick = { viewModel.applyPreset(preset) },
                        label = { Text(preset.name) },
                        leadingIcon = if (!preset.isBuiltIn) {
                            { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Input Area ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TEXT INPUT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (state.inputText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            if (state.appSettings.confirmClear) {
                                showConfirmClearDialog = true
                            } else {
                                viewModel.clearInput()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Input", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = state.inputText,
                onValueChange = { viewModel.onInputTextChanged(it) },
                placeholder = {
                    Text(
                        if (state.batchMode) {
                            "Enter one name/phrase per line...\n\nExample:\nJohn Michael Smith\nSarah Jane Wilson\nAli Hassan"
                        } else {
                            "Enter a name or phrase...\n\nExample:\nJohn Michael Smith"
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (state.batchMode) 150.dp else 120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Dynamic Input Statistics
            Spacer(modifier = Modifier.height(6.dp))
            val inputStats = remember(state.inputText, state.batchMode) {
                if (state.batchMode) {
                    val lines = state.inputText.lines().filter { it.isNotBlank() }
                    val words = state.inputText.split("\\s+".toRegex()).count { it.isNotBlank() }
                    "${lines.size} entries · $words words · ${state.inputText.length} characters"
                } else {
                    val words = state.inputText.split("\\s+".toRegex()).count { it.isNotBlank() }
                    val lines = if (state.inputText.isEmpty()) 0 else state.inputText.lines().size
                    "Characters: ${state.inputText.length}  |  Words: $words  |  Lines: $lines"
                }
            }
            Text(
                text = inputStats,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Initials Style ---
            Text(
                text = "INITIAL STYLE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = state.options.style == InitialsStyle.FIRST_WORD,
                    onClick = { viewModel.updateOptions(state.options.copy(style = InitialsStyle.FIRST_WORD)) },
                    label = { Text("First Word") }
                )
                FilterChip(
                    selected = state.options.style == InitialsStyle.FIRST_AND_LAST,
                    onClick = { viewModel.updateOptions(state.options.copy(style = InitialsStyle.FIRST_AND_LAST)) },
                    label = { Text("First + Last") }
                )
                FilterChip(
                    selected = state.options.style == InitialsStyle.ALL_WORDS,
                    onClick = { viewModel.updateOptions(state.options.copy(style = InitialsStyle.ALL_WORDS)) },
                    label = { Text("All Words") }
                )
                FilterChip(
                    selected = state.options.style == InitialsStyle.FIRST_N_WORDS,
                    onClick = { viewModel.updateOptions(state.options.copy(style = InitialsStyle.FIRST_N_WORDS)) },
                    label = { Text("First Letters (${state.options.firstNWordsCount})") }
                )
                FilterChip(
                    selected = state.options.style == InitialsStyle.CUSTOM,
                    onClick = { viewModel.updateOptions(state.options.copy(style = InitialsStyle.CUSTOM)) },
                    label = { Text("Custom") }
                )
            }

            // Sub-config for First Letters mode
            if (state.options.style == InitialsStyle.FIRST_N_WORDS) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Maximum words to take:", style = MaterialTheme.typography.bodySmall)
                    listOf(2, 3, 4, 5).forEach { count ->
                        FilterChip(
                            selected = state.options.firstNWordsCount == count,
                            onClick = { viewModel.updateOptions(state.options.copy(firstNWordsCount = count)) },
                            label = { Text("$count") }
                        )
                    }
                }
            }

            // Sub-config for Custom mode
            if (state.options.style == InitialsStyle.CUSTOM) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Take words:", style = MaterialTheme.typography.bodySmall)
                            FilterChip(
                                selected = state.options.customTakeFirst,
                                onClick = { viewModel.updateOptions(state.options.copy(customTakeFirst = !state.options.customTakeFirst)) },
                                label = { Text("First") }
                            )
                            FilterChip(
                                selected = state.options.customTakeLast,
                                onClick = { viewModel.updateOptions(state.options.copy(customTakeLast = !state.options.customTakeLast)) },
                                label = { Text("Last") }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Max initials:", style = MaterialTheme.typography.bodySmall)
                            listOf(2, 3, 4, 5).forEach { count ->
                                FilterChip(
                                    selected = state.options.customMaxInitials == count,
                                    onClick = { viewModel.updateOptions(state.options.copy(customMaxInitials = count)) },
                                    label = { Text("$count") }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Letter Case ---
            Text(
                text = "LETTER CASE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.options.caseStyle == CaseStyle.UPPERCASE,
                    onClick = { viewModel.updateOptions(state.options.copy(caseStyle = CaseStyle.UPPERCASE)) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) { Text("UPPERCASE") }

                SegmentedButton(
                    selected = state.options.caseStyle == CaseStyle.LOWERCASE,
                    onClick = { viewModel.updateOptions(state.options.copy(caseStyle = CaseStyle.LOWERCASE)) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) { Text("lowercase") }

                SegmentedButton(
                    selected = state.options.caseStyle == CaseStyle.TITLE_CASE,
                    onClick = { viewModel.updateOptions(state.options.copy(caseStyle = CaseStyle.TITLE_CASE)) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) { Text("Title Case") }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Initial Separator ---
            Text(
                text = "INITIAL SEPARATOR",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = state.options.separator == "" && !showCustomSeparatorField,
                    onClick = {
                        showCustomSeparatorField = false
                        viewModel.updateOptions(state.options.copy(separator = ""))
                    },
                    label = { Text("None") }
                )
                FilterChip(
                    selected = state.options.separator == " " && !showCustomSeparatorField,
                    onClick = {
                        showCustomSeparatorField = false
                        viewModel.updateOptions(state.options.copy(separator = " "))
                    },
                    label = { Text("Space ( )") }
                )
                FilterChip(
                    selected = state.options.separator == "." && !showCustomSeparatorField,
                    onClick = {
                        showCustomSeparatorField = false
                        viewModel.updateOptions(state.options.copy(separator = "."))
                    },
                    label = { Text("Dot (.)") }
                )
                FilterChip(
                    selected = state.options.separator == "-" && !showCustomSeparatorField,
                    onClick = {
                        showCustomSeparatorField = false
                        viewModel.updateOptions(state.options.copy(separator = "-"))
                    },
                    label = { Text("Hyphen (-)") }
                )
                FilterChip(
                    selected = state.options.separator == "/" && !showCustomSeparatorField,
                    onClick = {
                        showCustomSeparatorField = false
                        viewModel.updateOptions(state.options.copy(separator = "/"))
                    },
                    label = { Text("Slash (/)") }
                )
                FilterChip(
                    selected = showCustomSeparatorField,
                    onClick = { showCustomSeparatorField = true },
                    label = { Text("Custom") }
                )
            }

            if (showCustomSeparatorField) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.options.separator,
                    onValueChange = {
                        if (it.length <= 5) {
                            viewModel.updateOptions(state.options.copy(separator = it))
                        }
                    },
                    label = { Text("Custom separator (e.g. ' • ', '_')") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Quick Common Toggles ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.updateOptions(state.options.copy(ignoreSmallWords = !state.options.ignoreSmallWords))
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.options.ignoreSmallWords,
                    onCheckedChange = { viewModel.updateOptions(state.options.copy(ignoreSmallWords = it)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Ignore Common Small Words", style = MaterialTheme.typography.bodyMedium)
                    Text("e.g. of, the, and, in, for", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.updateOptions(state.options.copy(addPeriods = !state.options.addPeriods))
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.options.addPeriods,
                    onCheckedChange = { viewModel.updateOptions(state.options.copy(addPeriods = it)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Periods (e.g. J.M.S.)", style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.updateOptions(state.options.copy(includeNumbers = !state.options.includeNumbers))
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.options.includeNumbers,
                    onCheckedChange = { viewModel.updateOptions(state.options.copy(includeNumbers = it)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Include Numbers as Initials", style = MaterialTheme.typography.bodyMedium)
            }

            // --- Advanced Options Accordion ---
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvancedOptions = !showAdvancedOptions },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ADVANCED SETTINGS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = showAdvancedOptions) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            // Small word behavior
                            Text("Small Word Behavior:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = state.options.smallWordMode == SmallWordMode.IGNORE,
                                    onClick = { viewModel.updateOptions(state.options.copy(smallWordMode = SmallWordMode.IGNORE)) },
                                    label = { Text("Ignore completely") }
                                )
                                FilterChip(
                                    selected = state.options.smallWordMode == SmallWordMode.INCLUDE_FIRST_LAST,
                                    onClick = { viewModel.updateOptions(state.options.copy(smallWordMode = SmallWordMode.INCLUDE_FIRST_LAST)) },
                                    label = { Text("Include first/last only") }
                                )
                                FilterChip(
                                    selected = state.options.smallWordMode == SmallWordMode.ALWAYS_INCLUDE,
                                    onClick = { viewModel.updateOptions(state.options.copy(smallWordMode = SmallWordMode.ALWAYS_INCLUDE)) },
                                    label = { Text("Always include") }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Name Particles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Treat Name Particles as Small Words", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text("de, da, del, van, von, bin, ibn, al", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = state.options.treatNameParticlesAsSmallWords,
                                    onCheckedChange = { viewModel.updateOptions(state.options.copy(treatNameParticlesAsSmallWords = it)) }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Hyphenated names
                            Text("Hyphenated Names:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = state.options.hyphenMode == HyphenMode.ONE_WORD,
                                    onClick = { viewModel.updateOptions(state.options.copy(hyphenMode = HyphenMode.ONE_WORD)) },
                                    label = { Text("One Word (MW)") }
                                )
                                FilterChip(
                                    selected = state.options.hyphenMode == HyphenMode.SPLIT_WORDS,
                                    onClick = { viewModel.updateOptions(state.options.copy(hyphenMode = HyphenMode.SPLIT_WORDS)) },
                                    label = { Text("Split Parts (MJW)") }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Underscores & symbols
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Treat Symbols as Separators (_ / \\ |)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = state.options.symbolAsSeparator,
                                    onCheckedChange = { viewModel.updateOptions(state.options.copy(symbolAsSeparator = it)) }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Number initial style
                            Text("Numeric Initial Style:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = state.options.numberMode == NumberMode.FIRST_DIGIT,
                                    onClick = { viewModel.updateOptions(state.options.copy(numberMode = NumberMode.FIRST_DIGIT)) },
                                    label = { Text("First digit (P2)") }
                                )
                                FilterChip(
                                    selected = state.options.numberMode == NumberMode.FULL_NUMBER,
                                    onClick = { viewModel.updateOptions(state.options.copy(numberMode = NumberMode.FULL_NUMBER)) },
                                    label = { Text("Full number (P2026)") }
                                )
                                FilterChip(
                                    selected = state.options.numberMode == NumberMode.IGNORE,
                                    onClick = { viewModel.updateOptions(state.options.copy(numberMode = NumberMode.IGNORE)) },
                                    label = { Text("Ignore") }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Letter position (First / Last)
                            Text("Character From Each Word:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = state.options.characterPosition == CharacterPosition.FIRST,
                                    onClick = { viewModel.updateOptions(state.options.copy(characterPosition = CharacterPosition.FIRST)) },
                                    label = { Text("First character (JMS)") }
                                )
                                FilterChip(
                                    selected = state.options.characterPosition == CharacterPosition.LAST,
                                    onClick = { viewModel.updateOptions(state.options.copy(characterPosition = CharacterPosition.LAST)) },
                                    label = { Text("Last character (NLH)") }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Characters per word
                            Text("Characters per word:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 2, 3).forEach { count ->
                                    FilterChip(
                                        selected = state.options.charactersPerWord == count,
                                        onClick = { viewModel.updateOptions(state.options.copy(charactersPerWord = count)) },
                                        label = { Text("$count ${if (count > 1) "(${if (count == 2) "JoMiSm" else "JohMicSmi"})" else ""}") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Prefix and Suffix
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = state.options.prefix,
                                    onValueChange = {
                                        if (it.length <= 4) viewModel.updateOptions(state.options.copy(prefix = it))
                                    },
                                    label = { Text("Prefix (e.g. [)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.options.suffix,
                                    onValueChange = {
                                        if (it.length <= 4) viewModel.updateOptions(state.options.copy(suffix = it))
                                    },
                                    label = { Text("Suffix (e.g. ])") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Batch duplicates & numbering
                            if (state.batchMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Remove Duplicate Inputs", style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = state.options.removeDuplicateInputs,
                                        onCheckedChange = { viewModel.updateOptions(state.options.copy(removeDuplicateInputs = it)) }
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Remove Duplicate Results", style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = state.options.removeDuplicateResults,
                                        onCheckedChange = { viewModel.updateOptions(state.options.copy(removeDuplicateResults = it)) }
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Number Batch Results (1. JMS)", style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = state.options.numberBatchResults,
                                        onCheckedChange = { viewModel.updateOptions(state.options.copy(numberBatchResults = it)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // RESULTS AREA
            // ==========================================
            if (state.isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else if (state.results.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Enter text above to generate initials.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                if (!state.batchMode) {
                    // --- SINGLE MODE RESULT PREVIEW ---
                    val result = state.results.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GENERATED INITIALS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = result.initials.ifEmpty { "—" },
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = result.input,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${result.characterCount} letters · ${result.usedWordCount} of ${result.originalWordCount} words used",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { viewModel.copySingleInitials() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("COPY INITIALS", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.saveCurrentSession() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("SAVE")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.shareResults(context) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("SHARE")
                                }
                                OutlinedButton(
                                    onClick = {
                                        exportSelectedOnly = false
                                        showExportDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("EXPORT")
                                }
                            }
                        }
                    }
                } else {
                    // --- BATCH MODE RESULTS VIEW ---
                    val allResults = state.results
                    val filteredResults = remember(allResults, state.searchQuery) {
                        if (state.searchQuery.isBlank()) {
                            allResults
                        } else {
                            val q = state.searchQuery.trim().lowercase()
                            allResults.filter {
                                it.input.lowercase().contains(q) || it.initials.lowercase().contains(q)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header with title and Sort dropdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "RESULTS (${filteredResults.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Box {
                                    OutlinedButton(
                                        onClick = { showSortMenu = true },
                                        contentPadding = ButtonDefaults.ContentPadding
                                    ) {
                                        Text("Sort: ${state.options.sortMode.name.replace("_", " ")}", fontSize = 11.sp)
                                    }
                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false }
                                    ) {
                                        InitialsSortMode.values().forEach { mode ->
                                            DropdownMenuItem(
                                                text = { Text(mode.name.replace("_", " ")) },
                                                onClick = {
                                                    viewModel.setSortMode(mode)
                                                    showSortMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Search bar
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search results...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Selection tools
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (state.selectedIds.isNotEmpty()) "${state.selectedIds.size} selected" else "Select results",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row {
                                    TextButton(onClick = { viewModel.selectAllFilteredResults(filteredResults) }) {
                                        Text("Select All", fontSize = 12.sp)
                                    }
                                    if (state.selectedIds.isNotEmpty()) {
                                        TextButton(onClick = { viewModel.deselectAll() }) {
                                            Text("Deselect", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Table header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.width(36.dp))
                                Text(
                                    text = "INPUT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "INITIALS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(80.dp)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(bottom = 6.dp))

                            // List of entries
                            filteredResults.forEachIndexed { index, item ->
                                val isSelected = state.selectedIds.contains(item.id)
                                val numberPrefix = if (state.options.numberBatchResults) "${index + 1}. " else ""
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { viewModel.toggleSelectResult(item.id) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.toggleSelectResult(item.id) },
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$numberPrefix${item.input}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = item.initials,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(80.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Batch Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.copyAllInitials(selectedOnly = state.selectedIds.isNotEmpty())
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (state.selectedIds.isNotEmpty()) "COPY SELECTED" else "COPY ALL")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.copyInputWithInitials(selectedOnly = state.selectedIds.isNotEmpty())
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("COPY + INPUT")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.copyAsJson(selectedOnly = state.selectedIds.isNotEmpty()) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("AS JSON")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.saveCurrentSession() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("SAVE")
                                }
                                OutlinedButton(
                                    onClick = {
                                        exportSelectedOnly = state.selectedIds.isNotEmpty()
                                        showExportDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("EXPORT")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
