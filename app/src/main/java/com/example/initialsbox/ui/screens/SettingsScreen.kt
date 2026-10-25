package com.example.initialsbox.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.initialsbox.model.AppTheme
import com.example.initialsbox.ui.InitialsBoxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: InitialsBoxViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val settings = state.appSettings

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Appearance ---
            Text(
                text = "APPEARANCE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.theme == AppTheme.SYSTEM,
                            onClick = { viewModel.updateTheme(AppTheme.SYSTEM) },
                            label = { Text("System") }
                        )
                        FilterChip(
                            selected = settings.theme == AppTheme.LIGHT,
                            onClick = { viewModel.updateTheme(AppTheme.LIGHT) },
                            label = { Text("Light") }
                        )
                        FilterChip(
                            selected = settings.theme == AppTheme.DARK,
                            onClick = { viewModel.updateTheme(AppTheme.DARK) },
                            label = { Text("Dark") }
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dynamic Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("Use Material You system wallpaper palette", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.dynamicColor,
                                onCheckedChange = { viewModel.updateDynamicColor(it) }
                            )
                        }
                    }
                }
            }

            // --- Behavior Settings ---
            Text(
                text = "BEHAVIOR",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSwitchRow(
                        title = "Live Generation",
                        subtitle = "Generate initials automatically while typing (250ms debounce)",
                        checked = settings.liveGeneration,
                        onCheckedChange = { viewModel.updateSetting(liveGen = it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SettingsSwitchRow(
                        title = "Haptics",
                        subtitle = "Provide tactile feedback on actions",
                        checked = settings.haptics,
                        onCheckedChange = { viewModel.updateSetting(haptics = it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SettingsSwitchRow(
                        title = "Animations",
                        subtitle = "Enable smooth UI transitions and transforms",
                        checked = settings.animations,
                        onCheckedChange = { viewModel.updateSetting(animations = it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SettingsSwitchRow(
                        title = "Confirm Clear",
                        subtitle = "Prompt before clearing the text input",
                        checked = settings.confirmClear,
                        onCheckedChange = { viewModel.updateSetting(confirmClear = it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SettingsSwitchRow(
                        title = "Confirm Delete History",
                        subtitle = "Prompt before clearing all saved history",
                        checked = settings.confirmDeleteHistory,
                        onCheckedChange = { viewModel.updateSetting(confirmDeleteHistory = it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SettingsSwitchRow(
                        title = "Restore Last Text",
                        subtitle = "Resume where you left off when opening the app",
                        checked = settings.restoreLastText,
                        onCheckedChange = { viewModel.updateSetting(restoreLastText = it) }
                    )
                }
            }

            // --- Default Configuration Reference ---
            Text(
                text = "STANDARD CONFIGURATION DEFAULTS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DefaultConfigItem("Style", "All Meaningful Words")
                    DefaultConfigItem("Case", "UPPERCASE")
                    DefaultConfigItem("Separator", "None")
                    DefaultConfigItem("Periods", "OFF")
                    DefaultConfigItem("Ignore Small Words", "ON")
                    DefaultConfigItem("Name Particles as Small Words", "OFF")
                    DefaultConfigItem("Hyphenated Words", "One Word")
                    DefaultConfigItem("Apostrophes", "One Word")
                    DefaultConfigItem("Symbols as Separators", "ON")
                    DefaultConfigItem("Include Numbers", "ON (First Digit)")
                    DefaultConfigItem("Character Position", "First Character")
                    DefaultConfigItem("Characters Per Word", "1")
                    DefaultConfigItem("Duplicates Removal", "OFF")
                    DefaultConfigItem("Sort Order", "Input Order")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun DefaultConfigItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
