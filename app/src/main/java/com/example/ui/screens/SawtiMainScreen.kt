package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddEditPhraseDialog
import com.example.ui.components.EditableTextView
import com.example.ui.components.EmergencyButton
import com.example.ui.components.EmergencyConfirmationDialog
import com.example.ui.components.EmergencyScreen
import com.example.ui.components.HandwritingCanvas
import com.example.ui.components.LanguageSelector
import com.example.ui.components.QuickPhrasesSection
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SpeechControls
import com.example.ui.viewmodel.SawtiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SawtiMainScreen(
    viewModel: SawtiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickPhrases by viewModel.quickPhrases.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    // Determine layout direction (RTL for Arabic, LTR for English/Chinese)
    val layoutDirection = if (uiState.selectedLanguage == "ar") {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                Surface(
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.RecordVoiceOver,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (uiState.selectedLanguage == "ar") "صوتي (Sawti)" else "Sawti (صوتي)",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = when (uiState.selectedLanguage) {
                                                "ar" -> "تطبيق التواصل الميسر لكبار السن"
                                                "zh" -> "老年人与失语者辅助沟通应用"
                                                else -> "Accessibility Communication App"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Dark Mode Toggle Button
                                IconButton(
                                    onClick = { viewModel.toggleDarkMode() },
                                    modifier = Modifier.testTag("dark_mode_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Dark Mode",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Settings Button
                                IconButton(
                                    onClick = { viewModel.toggleSettings() },
                                    modifier = Modifier.testTag("settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // Language Selector Bar
                        LanguageSelector(
                            selectedLanguage = uiState.selectedLanguage,
                            onLanguageSelected = { viewModel.setLanguage(it) }
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 700.dp) // Adaptive layout constraint for tablets
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // 1. Emergency Quick Callout Button (Visible for immediate critical accessibility)
                    EmergencyButton(
                        language = uiState.selectedLanguage,
                        onRequestEmergency = { viewModel.requestEmergencyMode() }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Handwriting Canvas Area (Write with finger or stylus)
                    HandwritingCanvas(
                        strokes = uiState.strokes,
                        currentStrokePoints = uiState.currentStrokePoints,
                        isRecognizing = uiState.isRecognizing,
                        isDarkMode = uiState.isDarkMode,
                        isOnline = uiState.isOnline,
                        language = uiState.selectedLanguage,
                        onStrokeStart = { x, y -> viewModel.onStrokeStart(x, y) },
                        onStrokeMove = { x, y -> viewModel.onStrokeMove(x, y) },
                        onStrokeEnd = { viewModel.onStrokeEnd() },
                        onClear = { viewModel.clearCanvas() },
                        onUndo = { viewModel.undoLastStroke() },
                        onRecognize = { viewModel.recognizeHandwriting() }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. Editable Text Field with Smart Correction Banner
                    EditableTextView(
                        currentText = uiState.currentText,
                        suggestedCorrection = uiState.suggestedCorrection,
                        language = uiState.selectedLanguage,
                        fontScale = uiState.fontScale,
                        statusMessage = uiState.statusMessage,
                        onTextChange = { viewModel.onTextChange(it) },
                        onClearText = { viewModel.clearText() },
                        onCheckCorrection = { viewModel.triggerSmartCorrectionCheck() },
                        onApplyCorrection = { viewModel.applySuggestedCorrection() },
                        onDismissCorrection = { viewModel.dismissCorrection() }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4. Large SPEAK Button and Secondary Speech Controls
                    SpeechControls(
                        isSpeaking = isSpeaking,
                        hasText = uiState.currentText.isNotBlank(),
                        speechSpeed = uiState.speechSpeed,
                        autoSpeakEnabled = uiState.autoSpeakEnabled,
                        language = uiState.selectedLanguage,
                        onSpeak = { viewModel.speakText() },
                        onStop = { viewModel.stopSpeech() },
                        onRepeat = { viewModel.repeatSpeech() },
                        onSpeedChange = { viewModel.setSpeechSpeed(it) },
                        onToggleAutoSpeak = { viewModel.toggleAutoSpeak() }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 5. Large Communication Quick Phrases
                    QuickPhrasesSection(
                        phrases = quickPhrases,
                        language = uiState.selectedLanguage,
                        fontScale = uiState.fontScale,
                        onPhraseClick = { viewModel.onPhraseTapped(it) },
                        onAddPhraseClick = { viewModel.openAddPhraseDialog() },
                        onEditPhraseClick = { viewModel.openAddPhraseDialog(it) },
                        onDeletePhraseClick = { viewModel.deletePhrase(it) },
                        onMovePhrase = { phrase, moveUp -> viewModel.movePhrase(phrase, moveUp) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Emergency Confirmation Dialog
            EmergencyConfirmationDialog(
                isOpen = uiState.isConfirmingEmergency,
                language = uiState.selectedLanguage,
                onConfirm = { viewModel.confirmEmergencyMode() },
                onDismiss = { viewModel.cancelEmergencyMode() }
            )

            // Emergency Active Full Screen Alert
            EmergencyScreen(
                isOpen = uiState.isEmergencyModeActive,
                language = uiState.selectedLanguage,
                onSpeakEmergencyPhrase = { phrase ->
                    viewModel.speakText(phrase)
                },
                onCloseEmergency = { viewModel.cancelEmergencyMode() }
            )

            // Add/Edit Phrase Dialog
            AddEditPhraseDialog(
                isOpen = uiState.isAddPhraseDialogOpen,
                editingPhrase = uiState.editingPhrase,
                language = uiState.selectedLanguage,
                onSave = { text -> viewModel.savePhrase(text) },
                onDismiss = { viewModel.closeAddPhraseDialog() }
            )

            // Accessibility Settings Dialog
            SettingsDialog(
                isOpen = uiState.isSettingsOpen,
                language = uiState.selectedLanguage,
                autoSpeakEnabled = uiState.autoSpeakEnabled,
                isDarkMode = uiState.isDarkMode,
                vibrationEnabled = uiState.vibrationEnabled,
                fontScale = uiState.fontScale,
                onToggleAutoSpeak = { viewModel.toggleAutoSpeak() },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onToggleVibration = { viewModel.toggleVibration() },
                onFontScaleChange = { viewModel.setFontScale(it) },
                onDismiss = { viewModel.toggleSettings() }
            )
        }
    }
}
